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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.users.AbstractUsersProfile;
import com.codenvy.analytics.persistent.DataLoader;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric {

    public static final String EXCLUDE_SIGN        = "~ ";
    public static final String SEPARATOR           = " OR ";
    public static final long   DAY_IN_MILLISECONDS = 86400000L;

    public static final Pattern REGISTERED_USER =
            Pattern.compile("^(?!(ANONYMOUSUSER_|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern ANONYMOUS_USER  =
            Pattern.compile("^(ANONYMOUSUSER_).*", Pattern.CASE_INSENSITIVE);

    public static final Pattern PERSISTENT_WS = Pattern.compile("^(?!(TMP-|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    public static final Pattern TEMPORARY_WS  = Pattern.compile("^(TMP-).*", Pattern.CASE_INSENSITIVE);

    public static final String ASC_SORT_SIGN = "+";
    public static final String PRECOMPUTED   = "_precomputed";


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
    }

    /**
     * Returns an expanded list of documents used to calculate numeric value returned by getValue() method.
     *
     * @param context
     *         the execution context, for the most cases it isn't needed to modify it. It is used as a parameter to get
     *         value of other metrics
     * @throws IOException
     *         if any errors are occurred 
     */
    public ListValueData getExpandedValue(Context context) throws IOException {
        validateRestrictions(context);

        return dataLoader.loadExpandedValue(this, context);
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

    /**
     * Provides ability to modify the result by adding new fields or changing existed ones.
     */
    public ValueData postComputation(ValueData valueData, Context clauses) throws IOException {
        return valueData;
    }

    /** Allows modify context before evaluation if necessary. */
    public Context applySpecificFilter(Context context) throws IOException {
        return context;
    }

    private boolean canReadPrecomputedData(Context context) {
        return false;  // TODO un-comment in time of merging into the master branch 
//        return !context.exists(Parameters.DATA_COMPUTATION_PROCESS)
//               && MetricFactory.exists(getName() + PRECOMPUTED)
//               && (!context.exists(Parameters.FROM_DATE) || context.isDefaultValue(Parameters.FROM_DATE))
//               && (!context.exists(Parameters.TO_DATE) || context.isDefaultValue(Parameters.TO_DATE));
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

        // check if we need to filter list valued metric by another expanded metric
        if (clauses.hasFilterByExpandedMetric()) {
            String value = clauses.getAsString(Parameters.EXPANDED_METRIC_NAME);
            MetricType expandedMetricType = MetricType.valueOf(value.toUpperCase());
            
            Metric expandedMetric = MetricFactory.getMetric(expandedMetricType);
            
            if (expandedMetric instanceof Expandable) {
                String[] filteringValues = getExpandedMetricValues(expandedMetricType, clauses);
                String filteringField = ((ReadBasedMetric) expandedMetric).getExpandedValueField();
                match.put(filteringField, new BasicDBObject("$in", filteringValues));                
            }
        }
        
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

            } else if (!(this instanceof AbstractUsersProfile)
                       && filter == MetricFilter.USER) {
                Object users;

                if (value.equals(Parameters.USER_TYPES.REGISTERED.name())) {
                    users = REGISTERED_USER;
                } else if (value.equals(Parameters.USER_TYPES.ANTONYMOUS.name())) {
                    users = ANONYMOUS_USER;
                } else if (value.equals(Parameters.USER_TYPES.ANY.name())) {
                    continue;
                } else {
                    users = processValue(value, filter.isNumericType());
                }

                match.put(field, users);

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
            return new BasicDBObject("$in", value);

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
            return new BasicDBObject("$nin", isNumericType ? convertToNumericFormat(values) : values);
        } else {
            if (values.length == 1) {
                return isNumericType ? Long.parseLong(values[0]) : values[0];
            } else {
                return new BasicDBObject("$in", isNumericType ? convertToNumericFormat(values) : values);
            }
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
     * @param usePagination
     * @return {@link DBObject}
     */
    public final DBObject[] getDBOperations(Context clauses) {
        DBObject[] dbOps = getSpecificDBOperations(clauses);
        
        dbOps = unionDBOperations(dbOps, getSortingDBOperations(clauses));  // sort before pagination
        dbOps = unionDBOperations(dbOps, getPaginationDBOperations(clauses)); 
        
        return dbOps;
    }

    /**
     * Returns the sequences of operations to get expanded metric value upon data have been retrieved out of storage.
     *
     * @param clauses
     *         the execution context
     * @return {@link DBObject}
     */
    public final DBObject[] getExpandedDBOperations(Context clauses) {
        DBObject[] dbOps = getSpecificExpandedDBOperations(clauses);
        
        dbOps = unionDBOperations(dbOps, getSortingDBOperations(clauses));  // sort before pagination
        dbOps = unionDBOperations(dbOps, getPaginationDBOperations(clauses)); 
        
        return dbOps;
    }
    
    /**
     * @return the field which consists of values of expanded metric.
     */
    public String getExpandedValueField() {
        return null;
    }
    
    /** Provides basic DB pagination operations. */
    private DBObject[] getPaginationDBOperations(Context clauses) {
        boolean pageExists = clauses.exists(Parameters.PAGE);

        DBObject[] dbOp = new DBObject[pageExists ? 2 : 0];
        if (pageExists) {            
            long page = clauses.getAsLong(Parameters.PAGE);
            long perPage = clauses.getAsLong(Parameters.PER_PAGE);

            dbOp[0] = new BasicDBObject("$skip", (page - 1) * perPage);
            dbOp[1] = new BasicDBObject("$limit", perPage);
        }

        return dbOp;
    }

    /** Provides basic DB sorting operation. */
    private DBObject[] getSortingDBOperations(Context clauses) {
        boolean sortExists = clauses.exists(Parameters.SORT);

        DBObject[] dbOp = new DBObject[sortExists ? 1 : 0];

        if (sortExists) {
            String sortCondition = clauses.getAsString(Parameters.SORT);

            String field = sortCondition.substring(1);
            boolean asc = sortCondition.substring(0, 1).equals(ASC_SORT_SIGN);

            dbOp[0] = new BasicDBObject("$sort", new BasicDBObject(field, asc ? 1 : -1));
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

    /** @return DB operations specific for given expanded metric 
     * TODO make abstract
     * */
    public DBObject[] getSpecificExpandedDBOperations(Context clauses) {
        return new DBObject[0];
    }
    
    /**
     * @return expanded metric values array with size limited to 100000. 
     * Array size limitation needs to avoid reaching of limit=16MB of BSON Document Size used for $in operator. 
     * @see http://stackoverflow.com/questions/5331549/what-is-the-maximum-number-of-parameters-passed-to-in-query-in-mongodb
     */
    public String[] getExpandedMetricValues(MetricType metric, Context context) throws ParseException, IOException {
        Context.Builder builder = new Context.Builder(context);  // unlink context from caller method
        builder.remove(Parameters.EXPANDED_METRIC_NAME);
        
        // get all data without pagination
        builder.remove(Parameters.PAGE);
        builder.remove(Parameters.PER_PAGE);

        // remove already useless time parameters
        builder.remove(Parameters.TIME_INTERVAL);        
        builder.remove(Parameters.TIME_UNIT);
        
        context = builder.build();
        
        ReadBasedMetric expandableMetric = (ReadBasedMetric)MetricFactory.getMetric(metric);
        ListValueData metricValue = expandableMetric.getExpandedValue(context);

        List<ValueData> allMetricValues = metricValue.getAll();
        
        // return empty view data if there is empty metricValue
        if (allMetricValues.size() == 0) {
            return new String[0];  // return empty array
        }
        
        List<String> values = new ArrayList<>(allMetricValues.size());

        outer:
        for (ValueData rowValue: allMetricValues) {
            MapValueData row = (MapValueData) rowValue;
            for (Entry<String, ValueData> entry: row.getAll().entrySet()) {
                values.add(entry.getValue().getAsString());
                
                // limit values size to 100000  
                // TODO make this code more readable 
                if (values.size() > 100000) {
                    break outer;
                }
            }
        }
        
        return values.toArray(new String[0]);
    }
}