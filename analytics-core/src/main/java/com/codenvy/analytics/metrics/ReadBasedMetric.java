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
import com.codenvy.analytics.persistent.DataLoader;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * It is supposed to load calculated value {@link com.codenvy.analytics.datamodel.ValueData} from the storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ReadBasedMetric extends AbstractMetric implements Expandable {

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

    @Override
    public ListValueData getExpandedValue(Context context) throws IOException {
        context = modifyContext(context);
        validateRestrictions(context);

        return dataLoader.loadExpandedValue(this, context);
    }
    
    /**
     * Validates restriction before data loading.
     *
     * @param context
     *         the execution context
     */
    protected void validateRestrictions(Context context) {
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
        return MetricFactory.getMetric(metricType).getName().toLowerCase();
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
            String value = clauses.get(Parameters.EXPANDED_METRIC_NAME);
            MetricType expandedMetricType = MetricType.valueOf(value.toUpperCase());
            
            Metric expandedMetric = MetricFactory.getMetric(expandedMetricType);
            
            if (expandedMetric instanceof Expandable) {
                String[] filteringValues = getExpandedMetricValues(expandedMetricType, clauses);
                String filteringField = ((ReadBasedMetric) expandedMetric).getExpandedValueField();
                match.put(filteringField, new BasicDBObject("$in", filteringValues));                
            }
        }
        
        for (MetricFilter filter : clauses.getFilters()) {
            String value = clauses.get(filter);
                                
            if (filter == MetricFilter.USER_COMPANY
                || filter == MetricFilter.USER_FIRST_NAME
                || filter == MetricFilter.USER_LAST_NAME) {

                String[] values = getUsers(filter, value);
                match.put(MetricFilter.USER.name().toLowerCase(), new BasicDBObject("$in", values));

            } else if (filter == MetricFilter.DOMAIN) {
                Pattern usersInDomains = getUsersInDomains(value.split(SEPARATOR));
                match.put(MetricFilter.USER.name().toLowerCase(), usersInDomains);

            } else if (filter == MetricFilter.USER) {
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
            } else {
                String[] values = value.split(SEPARATOR);
                match.put(filter.name().toLowerCase(), processExclusiveValues(values, filter.isNumericType()));
            }
        }

        return new BasicDBObject("$match", match);
    }

    private Object processExclusiveValues(String[] values, boolean isNumericType)
            throws IOException, ParseException {

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

        String fromDate = clauses.get(Parameters.FROM_DATE);
        if (fromDate != null) {
            if (Utils.isDateFormat(fromDate)) {
                dateFilter.put("$gte", clauses.getAsDate(Parameters.FROM_DATE).getTimeInMillis());
            } else {
                dateFilter.put("$gte", clauses.getAsLong(Parameters.FROM_DATE));
            }
        } else {
            dateFilter.put("$gte", 0);
        }

        String toDate = clauses.get(Parameters.TO_DATE);
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

    private Pattern getUsersInDomains(String[] domains) {
        StringBuilder builder = new StringBuilder();
        for (String domain : domains) {
            if (builder.length() != 0) {
                builder.append("|");
            }

            builder.append(".*");
            if (!domain.startsWith("@")) {
                builder.append("@");
            }
            builder.append(domain);
        }

        return Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
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
     * @param usePagination
     * @return {@link DBObject}
     */
    public final DBObject[] getDBOperations(Context clauses, boolean usePagination) {
        DBObject[] dbOps = getSpecificDBOperations(clauses);
        dbOps = unionDBOperations(dbOps, getSortingDBOperations(clauses));  // sort before pagination
        
        if (usePagination) {
            dbOps = unionDBOperations(dbOps, getPaginationDBOperations(clauses)); 
        }
        
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
            String sortCondition = clauses.get(Parameters.SORT);

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
        
    public String[] getExpandedMetricValues(MetricType metric, Context context) throws ParseException, IOException {
        // unlink context from caller method
        Context.Builder builder = new Context.Builder(context);
        builder.remove(Parameters.EXPANDED_METRIC_NAME);
        context = builder.build();

        context = initializeFirstInterval(context);
        Expandable expandableMetric = (Expandable)MetricFactory.getMetric(metric);
        ListValueData metricValue = expandableMetric.getExpandedValue(context);

        List<ValueData> allMetricValues = metricValue.getAll();
        
        // return empty view data if there is empty metricValue
        if (allMetricValues.size() == 0) {
            return null;
        }
        
        List<String> values = new ArrayList<>(allMetricValues.size());

        for (ValueData rowValue: allMetricValues) {
            MapValueData row = (MapValueData) rowValue;
            for (Entry<String, ValueData> entry: row.getAll().entrySet()) {
                values.add(entry.getValue().getAsString());
            }
        }
        
        return values.toArray(new String[0]);
    }
    
    
    private Context initializeFirstInterval(Context context) throws ParseException {
        Context.Builder builder = new Context.Builder(context);

        if (!context.exists(Parameters.TO_DATE)) {
            builder.putDefaultValue(Parameters.TO_DATE);
            builder.putDefaultValue(Parameters.FROM_DATE);
            builder.put(Parameters.REPORT_DATE, builder.get(Parameters.TO_DATE));
        } else {
            builder.put(Parameters.REPORT_DATE, context.get(Parameters.TO_DATE));
        }

        if (context.exists(Parameters.TIME_UNIT)) {
            Parameters.TimeUnit timeUnit = builder.getTimeUnit();
            if (context.exists(Parameters.TIME_INTERVAL)) {
                int timeShift = (int) -context.getAsLong(Parameters.TIME_INTERVAL);
                return Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), timeUnit, timeShift, builder);                
            } else {
                return Utils.initDateInterval(builder.getAsDate(Parameters.TO_DATE), timeUnit, builder);
            }
        } else {
            return builder.build();
        }
    }
    
    public boolean isExpandable() {
        return false;
    }
}