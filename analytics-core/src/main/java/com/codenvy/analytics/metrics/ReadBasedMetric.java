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
import com.codenvy.analytics.persistent.DataLoader;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    public static final String EXCLUDE_SIGN        = "~";
    public static final String SEPARATOR           = ",";
    public static final long   DAY_IN_MILLISECONDS = 86400000L;

    public static final Pattern REGISTERED_USER =
            Pattern.compile("^(?!(ANONYMOUSUSER_|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern ANONYMOUS_USER  =
            Pattern.compile("^(ANONYMOUSUSER_).*", Pattern.CASE_INSENSITIVE);

    public static final Pattern NON_DEFAULT_WS = Pattern.compile("^(?!DEFAULT).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern PERSISTENT_WS  = Pattern.compile("^(?!(TMP-|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern TEMPORARY_WS   = Pattern.compile("^(TMP-).*", Pattern.CASE_INSENSITIVE);

    public static final String ASC_SORT_SIGN = "+";


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
        context = modifyContext(context);
        validateRestrictions(context);

        ValueData valueData = dataLoader.loadValue(this, context);

        return postEvaluation(valueData, context);
    }

    /**
     * Validates restriction before data loading.
     *
     * @param context
     *         the execution context
     */
    private void validateRestrictions(Context context) {
        if (getClass().isAnnotationPresent(FilterRequired.class)) {
            MetricFilter requiredFilter = getClass().getAnnotation(FilterRequired.class).value();
            if (!context.exists(requiredFilter)) {
                throw new MetricRestrictionException(
                        "Parameter " + requiredFilter + " required to be passed to get the value of the metric");
            }
        }
    }

    public ValueData postEvaluation(ValueData valueData, Context clauses) throws IOException {
        return valueData;
    }

    /** Allows modify context before evaluation if necessary. */
    protected Context modifyContext(Context context) throws IOException {
        return context;
    }

    // --------------------------------------------- storage related methods -------------

    /**
     * @return the fields are interested in by given metric. In other words, they are valuable for given metric. It
     * might returns empty array to read all available fields
     */
    public abstract String[] getTrackedFields();

    public String getStorageCollectionName() {
        return getName().toLowerCase();
    }

    public String getStorageCollectionName(MetricType metricType) {
        return metricType.toString().toLowerCase();
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
    public DBObject getFilter(Context clauses) throws IOException, ParseException {
        BasicDBObject match = new BasicDBObject();
        setDateFilter(clauses, match);

        for (MetricFilter filter : clauses.getFilters()) {
            if (filter == MetricFilter.USER_COMPANY
                || filter == MetricFilter.USER_FIRST_NAME
                || filter == MetricFilter.USER_LAST_NAME) {

                String value = clauses.getAsString(filter);
                String[] users = getUsers(filter, value);
                match.put(MetricFilter.USER.name().toLowerCase(), new BasicDBObject("$in", users));

            } else if (filter == MetricFilter.USER) {
                String value = clauses.getAsString(filter);
                Object users;

                if (value.equalsIgnoreCase(Parameters.USER_TYPES.REGISTERED.name())) {
                    users = REGISTERED_USER;
                } else if (value.equalsIgnoreCase(Parameters.USER_TYPES.ANTONYMOUS.name())) {
                    users = ANONYMOUS_USER;
                } else if (value.equalsIgnoreCase(Parameters.USER_TYPES.ANY.name())) {
                    continue;
                } else {
                    String[] values = value.split(SEPARATOR);
                    users = processExclusiveValues(values, filter.isNumericType());
                }

                match.put(filter.name().toLowerCase(), users);

            } else if (filter == MetricFilter.WS) {
                String value = clauses.getAsString(filter);
                Object ws;

                if (value.equalsIgnoreCase(Parameters.WS_TYPES.PERSISTENT.name())) {
                    ws = PERSISTENT_WS;
                } else if (value.equalsIgnoreCase(Parameters.WS_TYPES.TEMPORARY.name())) {
                    ws = TEMPORARY_WS;
                } else if (value.equalsIgnoreCase(Parameters.WS_TYPES.ANY.name())) {
                    continue;
                } else {
                    String[] values = value.split(SEPARATOR);
                    ws = processExclusiveValues(values, filter.isNumericType());
                }

                match.put(filter.name().toLowerCase(), ws);

            } else if (filter == MetricFilter.ENCODED_PAIRS) {
                match.putAll(Utils.fetchEncodedPairs(clauses.getAsString(filter)));

            } else {
                Object value = clauses.get(filter);

                if (value instanceof String) {
                    String[] values = ((String)value).split(SEPARATOR);
                    match.put(filter.name().toLowerCase(), processExclusiveValues(values, filter.isNumericType()));
                } else if (value.getClass().isArray()) {
                    match.put(filter.name().toLowerCase(), new BasicDBObject("$in", value));
                } else {
                    throw new IllegalStateException("Unsupported filter value class " + value.getClass());
                }
            }
        }

        return new BasicDBObject("$match", match);
    }

    private Object processExclusiveValues(String[] values, boolean isNumericType) throws IOException, ParseException {
        StringBuilder exclusiveValues = new StringBuilder();
        StringBuilder inclusiveValues = new StringBuilder();

        for (String value : values) {
            if (value.startsWith(EXCLUDE_SIGN)) {
                if (exclusiveValues.length() != 0) {
                    exclusiveValues.append(SEPARATOR);
                }
                exclusiveValues.append(value.substring(1));

            } else {
                if (inclusiveValues.length() != 0) {
                    inclusiveValues.append(SEPARATOR);
                }
                inclusiveValues.append(value);
            }
        }

        if (inclusiveValues.length() != 0) {
            values = inclusiveValues.toString().split(SEPARATOR);
            if (values.length == 1) {
                return isNumericType ? Long.parseLong(values[0]) : values[0];
            } else {
                return new BasicDBObject("$in", isNumericType ? convertToNumericFormat(values) : values);
            }
        } else {
            values = exclusiveValues.toString().split(SEPARATOR);
            return new BasicDBObject("$nin", isNumericType ? convertToNumericFormat(values) : values);
        }
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
        match.put(DATE, dateFilter);

        String fromDate = clauses.getAsString(Parameters.FROM_DATE);
        if (fromDate != null) {
            if (Utils.isDateFormat(fromDate)) {
                dateFilter.put("$gte", clauses.getAsDate(Parameters.FROM_DATE).getTimeInMillis());
            } else {
                dateFilter.put("$gte", clauses.getAsLong(Parameters.FROM_DATE));
            }
        } else {
            dateFilter.put("$gte", 0);
        }

        String toDate = clauses.getAsString(Parameters.TO_DATE);
        if (toDate != null) {
            if (Utils.isDateFormat(toDate)) {
                dateFilter.put("$lt", clauses.getAsDate(Parameters.TO_DATE).getTimeInMillis() + DAY_IN_MILLISECONDS);
            } else {
                dateFilter.put("$lte", clauses.getAsLong(Parameters.TO_DATE));
            }
        } else {
            dateFilter.put("$lte", Long.MAX_VALUE);
        }
    }

    private String[] getUsers(MetricFilter filter, String pattern) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(filter, pattern);
        Context context = builder.build();

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        List<ValueData> users = ValueDataUtil.getAsList(metric, context).getAll();

        String[] result = new String[users.size()];

        for (int i = 0; i < users.size(); i++) {
            MapValueData user = (MapValueData)users.get(i);
            Map<String, ValueData> profile = user.getAll();

            result[i] = profile.get(ID).getAsString();
        }

        return result;
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

