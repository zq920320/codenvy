/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.users.AbstractUsersProfile;
import com.codenvy.analytics.persistent.DataLoader;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.codenvy.analytics.Utils.*;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    private static final Logger LOG = LoggerFactory.getLogger(ReadBasedMetric.class);

    public static final String ASC_SORT_SIGN       = "+";
    public static final String EXCLUDE_SIGN        = "~ ";
    public static final String SEPARATOR           = " OR ";
    public static final String PRECOMPUTED         = "_precomputed";
    public static final long   DAY_IN_MILLISECONDS = 86400000L;

    public static final Pattern REGISTERED_USER = Pattern.compile("^(?!(ANONYMOUSUSER_|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern ANONYMOUS_USER  = Pattern.compile("^(ANONYMOUSUSER_).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern PERSISTENT_WS   = Pattern.compile("^(?!(TMP-|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern TEMPORARY_WS    = Pattern.compile("^(TMP-).*", Pattern.CASE_INSENSITIVE);

    public final DataLoader dataLoader;

    public ReadBasedMetric(String metricName) {
        super(metricName);

        MongoDataStorage mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
        this.dataLoader = mongoDataStorage.createdDataLoader();
    }

    public ReadBasedMetric(MetricType metricType) {
        this(metricType.toString());
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        long start = System.currentTimeMillis();

        try {
            context = omitFilters(context);
            validateRestrictions(context);

            if (canReadPrecomputedData(context)) {
                Metric metric = MetricFactory.getMetric(getName() + PRECOMPUTED);

                Context.Builder builder = new Context.Builder(context);
                builder.remove(Parameters.FROM_DATE);
                builder.remove(Parameters.TO_DATE);
                return metric.getValue(builder.build());
            } else {
                ValueData valueData = dataLoader.loadValue(this, context);
                return postComputation(valueData, context);
            }
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.info("Metric computation " + getName() + " is finished with context " + context + " in " +
                         ((System.currentTimeMillis() - start) / 1000) + " sec.");
            }
        }
    }

    private Context omitFilters(Context context) {
        if (getClass().isAnnotationPresent(OmitFilters.class)) {
            Context.Builder builder = new Context.Builder(context);
            for (MetricFilter filter : getClass().getAnnotation(OmitFilters.class).value()) {
                builder.remove(filter);
            }

            return builder.build();
        }

        return context;
    }

    /**
     * Validates restriction before data loading.
     *
     * @param context
     *         the execution context
     */
    private void validateRestrictions(Context context) {
        if (getClass().isAnnotationPresent(RequiredFilter.class)) {
            MetricFilter requiredFilter = getClass().getAnnotation(RequiredFilter.class).value();
            if (!context.exists(requiredFilter)) {
                throw new MetricRestrictionException("Parameter " + requiredFilter + " required to be passed to get the value of the metric");
            }
        }

        String allowedUsers = context.getAsString(Parameters.ORIGINAL_USER);
        String allowedWorkspaces = context.getAsString(Parameters.ORIGINAL_WS);

        String ws = context.getAsString(MetricFilter.WS);
        Object user = context.get(MetricFilter.USER);

        if (isAnonymousUser(user) && isTemporaryWorkspace(ws)) {
            throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
        } else if (isAnonymousUser(user)) {
            if (!isAllowedEntities(ws, allowedWorkspaces)) {
                throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
            }
        } else if (isTemporaryWorkspace(ws)) {
            if (!isAllowedEntities(user, allowedUsers)) {
                throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
            }
        } else {
            if (!isAllowedEntities(user, allowedUsers) || !isAllowedEntities(ws, allowedWorkspaces)) {
                throw new MetricRestrictionException("Security violation. Probably user hasn't access to the data");
            }
        }
    }

    /**
     * Provides ability to modify the result by adding new fields or changing existed ones.
     */
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        return valueData;
    }

    /**
     * Allows to modify context before evaluation if necessary.
     */
    public Context applySpecificFilter(Context context) throws IOException {
        return context;
    }

    private boolean canReadPrecomputedData(Context context) {
        String precomputedMetricName = getName() + PRECOMPUTED;
        return !context.exists(Parameters.DATA_COMPUTATION_PROCESS)
               && MetricFactory.exists(precomputedMetricName)
               && ((PrecomputedMetric)MetricFactory.getMetric(precomputedMetricName)).canReadPrecomputedData(context)
               && context.getFilters().isEmpty()
               && (!context.exists(Parameters.FROM_DATE) || context.isDefaultValue(Parameters.FROM_DATE))
               && (!context.exists(Parameters.TO_DATE) || context.isDefaultValue(Parameters.TO_DATE));
    }

    // --------------------------------------------- storage related methods -------------

    /**
     * @return the fields are interested in by given metric. In other words, they are valuable for given metric. It
     * might returns empty array to read all available fields
     */
    public abstract String[] getTrackedFields();

    public String getStorageCollectionName() {
        return getStorageCollectionName(getName());
    }

    public String getStorageCollectionName(MetricType metricType) {
        return getStorageCollectionName(metricType.toString());
    }

    public String getStorageCollectionName(String metricName) {
        return metricName.toLowerCase();
    }

    /**
     * Returns 'matcher' in term of MongoDB. Basically, it can be treated as 'WHERE' clause in SQL queries.
     * See mongoDB related documentation for more details.
     *
     * @param clauses
     *         the execution context
     * @return {@link DBObject}
     */
    public final DBObject getFilter(Context clauses) throws IOException, ParseException {
        BasicDBObject match = new BasicDBObject();
        setDateFilter(clauses, match);

        for (MetricFilter filter : clauses.getFilters()) {
            String field = filter.toString().toLowerCase();
            Object value = clauses.get(filter);
            if (isNullOrEmpty(value)) {
                continue;
            }

            if (!(this instanceof AbstractUsersProfile)
                && (filter == MetricFilter.USER_COMPANY
                    || filter == MetricFilter.USER_FIRST_NAME
                    || filter == MetricFilter.USER_LAST_NAME)) {

                match.put(MetricFilter.USER.name().toLowerCase(), getUsers(filter, value));

            } else if (!(this instanceof AbstractUsersProfile) && filter == MetricFilter.USER) {
                if (!value.equals(Parameters.USER_TYPES.ANY.name())) {
                    Object users = processValue(value, filter.isNumericType());
                    match.put(field, users);
                }

            } else if (filter == MetricFilter.WS) {
                Object ws;

                if (value.equals(Parameters.WS_TYPES.PERSISTENT.name())) {
                    ws = PERSISTENT_WS;
                } else if (value.equals(Parameters.WS_TYPES.TEMPORARY.name())) {
                    ws = TEMPORARY_WS;
                } else if (value.equals(Parameters.WS_TYPES.ANY.name())) {
                    continue;
                } else {
                    ws = processValue(value, filter.isNumericType());
                }

                match.put(field, ws);
            } else if (filter == MetricFilter.PARAMETERS) {
                match.putAll(Utils.fetchEncodedPairs(clauses.getAsString(filter)));

            } else {
                match.put(field, processValue(value, filter.isNumericType()));
            }
        }

        return new BasicDBObject("$match", match);
    }

    private boolean isNullOrEmpty(Object value) {
        return value == null || (value instanceof String && ((String)value).isEmpty());
    }

    protected Object processValue(Object value, boolean isNumericType) throws IOException {
        if (value.getClass().isArray()) {
            return new BasicDBObject("$in", processArray((Object[])value));

        } else if (value instanceof DBObject || value instanceof Pattern) {
            return value;

        } else {
            return processStringValue((String)value, isNumericType);
        }
    }

    protected Object processStringValue(String value, boolean isNumericType) {
        boolean processExclusiveValues = value.startsWith(EXCLUDE_SIGN);
        if (processExclusiveValues) {
            value = value.substring(EXCLUDE_SIGN.length());
        }

        String[] values = value.split(SEPARATOR);
        if (processExclusiveValues) {
            return new BasicDBObject("$nin", isNumericType ? convertToNumericFormat(values) : processArray(values));
        } else {
            if (values.length == 1) {
                return isNumericType ? Long.parseLong(values[0]) : processArray(values)[0];
            } else {
                return new BasicDBObject("$in", isNumericType ? convertToNumericFormat(values) : processArray(values));
            }
        }
    }

    private Object[] processArray(Object[] values) {
        Object[] result = new Object[values.length];

        for (int i = 0; i < result.length; i++) {
            Object value = values[i];

            if (value.equals(Parameters.WS_TYPES.TEMPORARY.toString())) {
                result[i] = TEMPORARY_WS;
            } else if (value.equals(Parameters.WS_TYPES.PERSISTENT.toString())) {
                result[i] = PERSISTENT_WS;
            } else if (value.equals(Parameters.USER_TYPES.ANONYMOUS.toString())) {
                result[i] = ANONYMOUS_USER;
            } else if (value.equals(Parameters.USER_TYPES.REGISTERED.toString())) {
                result[i] = REGISTERED_USER;
            } else {
                result[i] = value;
            }
        }

        return result;
    }

    private long[] convertToNumericFormat(String[] values) {
        long[] result = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Long.parseLong(values[i]);
        }

        return result;
    }

    /** The date field contains the date of the event. */
    private void setDateFilter(Context clauses, BasicDBObject match) throws ParseException {
        DBObject dateFilter = new BasicDBObject();

        String fromDate = clauses.getAsString(Parameters.FROM_DATE);
        if (fromDate != null) {
            if (Utils.isDateFormat(fromDate)) {
                dateFilter.put("$gte", clauses.getAsDate(Parameters.FROM_DATE).getTimeInMillis());
            } else {
                dateFilter.put("$gte", clauses.getAsLong(Parameters.FROM_DATE));
            }
        }

        String toDate = clauses.getAsString(Parameters.TO_DATE);
        if (toDate != null) {
            if (Utils.isDateFormat(toDate)) {
                dateFilter.put("$lt", clauses.getAsDate(Parameters.TO_DATE).getTimeInMillis() + DAY_IN_MILLISECONDS);
            } else {
                dateFilter.put("$lte", clauses.getAsLong(Parameters.TO_DATE));
            }
        }

        if (dateFilter.keySet().size() > 0) {
            match.put(DATE, dateFilter);
        }
    }

    private DBObject getUsers(MetricFilter filter, Object value) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(filter, value);
        Context context = builder.build();

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        List<ValueData> users = ValueDataUtil.getAsList(metric, context).getAll();

        String[] result = new String[users.size()];

        for (int i = 0; i < users.size(); i++) {
            MapValueData user = (MapValueData)users.get(i);
            Map<String, ValueData> profile = user.getAll();

            result[i] = profile.get(ID).getAsString();
        }

        return new BasicDBObject("$in", result);
    }

    /**
     * Returns the sequences of operations upon data have been retrieved out of storage.
     * See mongoDB documentation for more information.
     *
     * @param clauses
     *         the execution context
     * @return {@link DBObject}
     */
    public final DBObject[] getDBOperations(Context clauses) {
        return unionDBOperations(getSpecificDBOperations(clauses),
                                 getPaginationDBOperations(clauses));
    }

    /** Provides basic DB operations: sorting and pagination. */
    private DBObject[] getPaginationDBOperations(Context clauses) {
        boolean sortExists = clauses.exists(Parameters.SORT);
        boolean pageExists = clauses.exists(Parameters.PAGE);

        DBObject[] dbOp = new DBObject[(sortExists ? 1 : 0) + (pageExists ? 2 : 0)];

        if (sortExists) {
            String sortCondition = clauses.getAsString(Parameters.SORT);

            String field = sortCondition.substring(1);
            boolean asc = sortCondition.substring(0, 1).equals(ASC_SORT_SIGN);

            dbOp[0] = new BasicDBObject("$sort", new BasicDBObject(field, asc ? 1 : -1));
        }

        if (pageExists) {
            long page = clauses.getAsLong(Parameters.PAGE);
            long perPage = clauses.getAsLong(Parameters.PER_PAGE);

            dbOp[sortExists ? 1 : 0] = new BasicDBObject("$skip", (page - 1) * perPage);
            dbOp[sortExists ? 2 : 1] = new BasicDBObject("$limit", perPage);
        }

        return dbOp;
    }

    protected DBObject[] unionDBOperations(DBObject[] dbOp1, DBObject[] dbOp2) {
        DBObject[] result = new DBObject[dbOp1.length + dbOp2.length];

        System.arraycopy(dbOp1, 0, result, 0, dbOp1.length);
        System.arraycopy(dbOp2, 0, result, dbOp1.length, dbOp2.length);

        return result;
    }

    /** @return DB operations specific for given metric */
    public abstract DBObject[] getSpecificDBOperations(Context clauses);

}

