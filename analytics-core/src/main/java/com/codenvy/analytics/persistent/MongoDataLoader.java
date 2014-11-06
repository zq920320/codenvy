/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataFactory;
import com.codenvy.analytics.metrics.AbstractCount;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.CumulativeMetric;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedExpandable;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.codenvy.analytics.metrics.WithoutFromDateParam;
import com.codenvy.analytics.metrics.users.AbstractUsersProfile;
import com.codenvy.analytics.metrics.users.NonActiveUsers;
import com.codenvy.analytics.metrics.workspaces.AbstractWorkspacesProfile;
import com.codenvy.analytics.metrics.workspaces.NonActiveWorkspaces;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.codenvy.analytics.Utils.getFilterAsSet;
import static com.codenvy.analytics.Utils.getFilterAsString;
import static com.codenvy.analytics.Utils.isUserID;
import static com.codenvy.analytics.Utils.isWorkspaceID;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static com.codenvy.analytics.metrics.AbstractMetric.ID;

/**
 * @author Anatoliy Bazko
 */
public class MongoDataLoader implements DataLoader {

    public static final String ASC_SORT_SIGN       = "+";
    public static final String EXCLUDE_SIGN        = "~ ";
    public static final String SEPARATOR           = " OR ";
    public static final long   DAY_IN_MILLISECONDS = 86400000L;

    private final DB db;

    MongoDataLoader(DB db) {
        this.db = db;
    }

    @Override
    public ValueData loadValue(ReadBasedMetric metric, Context clauses) throws IOException {
        if (clauses.exists(Parameters.FROM_DATE) && clauses.isDefaultValue(Parameters.FROM_DATE)) {
            clauses = clauses.cloneAndRemove(Parameters.FROM_DATE);
        }
        if (clauses.exists(Parameters.TO_DATE) && clauses.isDefaultValue(Parameters.TO_DATE)) {
            clauses = clauses.cloneAndRemove(Parameters.TO_DATE);
        }

        if (metric instanceof AbstractCount) {
            return doLoadValue(metric, clauses, new LoadValueAction() {
                @Override
                public ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
                    return MongoDataLoader.this.createdValueData(metric, iterator);
                }

                @Override
                public Iterator<DBObject> iterator(ReadBasedMetric metric, Context clauses, DBCollection dbCollection, DBObject filter) {
                    long count = dbCollection.count((DBObject)filter.get("$match"));

                    DBObject result = new BasicDBObject();
                    result.put(metric.getTrackedFields()[0], count);

                    return Arrays.asList(result).iterator();
                }
            });
        }

        return doLoadValue(metric, clauses, new LoadValueAction() {
            @Override
            public ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
                return MongoDataLoader.this.createdValueData(metric, iterator);
            }

            @Override
            public Iterator<DBObject> iterator(ReadBasedMetric metric, Context clauses, DBCollection dbCollection, DBObject filter) {
                DBObject[] dbOperations = MongoDataLoader.this.getDBOperations(metric, clauses);
                AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperations);
                return aggregation.results().iterator();
            }
        });
    }

    @Override
    public ValueData loadExpandedValue(ReadBasedMetric metric, Context clauses) throws IOException {
        return doLoadValue(metric, clauses, new LoadValueAction() {
            @Override
            public ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
                return createListValueData(iterator, new String[]{((ReadBasedExpandable)metric).getExpandedField()});
            }

            @Override
            public Iterator<DBObject> iterator(ReadBasedMetric metric, Context clauses, DBCollection dbCollection, DBObject filter) {
                DBObject[] dbOperations = getExpandedDBOperations((ReadBasedExpandable)metric, clauses);
                AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperations);
                return aggregation.results().iterator();
            }

        });
    }

    @Override
    public ValueData loadSummarizedValue(ReadBasedMetric metric, Context clauses) throws IOException {
        return doLoadValue(metric, clauses, new LoadValueAction() {
            @Override
            public ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
                return MongoDataLoader.this.createdValueData(metric, iterator);
            }

            @Override
            public Iterator<DBObject> iterator(ReadBasedMetric metric, Context clauses, DBCollection dbCollection, DBObject filter) {
                DBObject[] dbOperations = ((ReadBasedSummariziable)metric).getSpecificSummarizedDBOperations(clauses);
                AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperations);
                return aggregation.results().iterator();
            }

        });
    }

    protected ValueData doLoadValue(ReadBasedMetric metric, Context clauses, LoadValueAction action) throws IOException {
        DBCollection dbCollection = db.getCollection(metric.getStorageCollectionName());

        try {
            clauses = metric.applySpecificFilter(clauses);
            DBObject filter = getFilter(metric, clauses);

            Iterator<DBObject> iterator = action.iterator(metric, clauses, dbCollection, filter);
            return action.createdValueData(metric, iterator);
        } catch (Exception e) {
            throw new IOException("Metric computation error: " + metric.getName(), e);
        }
    }

    /**
     * Returns the sequences of operations upon data have been retrieved out of storage.
     * See mongoDB documentation for more information.
     *
     * @return {@link DBObject}
     */
    private DBObject[] getDBOperations(ReadBasedMetric metric, Context clauses) {
        DBObject[] dbOps = metric.getSpecificDBOperations(clauses);

        dbOps = unionDBOperations(dbOps, getSortingDBOperations(clauses));
        dbOps = unionDBOperations(dbOps, getPaginationDBOperations(clauses));
        return dbOps;
    }

    /**
     * Returns the sequences of operations to get expanded metric value upon data have been retrieved out of storage.
     *
     * @return {@link DBObject}
     */
    private DBObject[] getExpandedDBOperations(ReadBasedExpandable metric, Context clauses) {
        DBObject[] dbOps = metric.getSpecificExpandedDBOperations(clauses);

        dbOps = unionDBOperations(dbOps, getSortingDBOperations(clauses));
        dbOps = unionDBOperations(dbOps, getPaginationDBOperations(clauses));
        return dbOps;
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

    private DBObject[] unionDBOperations(DBObject[] dbOp1, DBObject[] dbOp2) {
        DBObject[] result = new DBObject[dbOp1.length + dbOp2.length];

        System.arraycopy(dbOp1, 0, result, 0, dbOp1.length);
        System.arraycopy(dbOp2, 0, result, dbOp1.length, dbOp2.length);

        return result;
    }

    /**
     * Returns 'matcher' in term of MongoDB. Basically, it can be treated as 'WHERE' clause in SQL queries.
     * See mongoDB related documentation for more details.
     *
     * @param clauses
     *         the execution context
     * @return {@link DBObject}
     */
    private DBObject getFilter(Metric metric, Context clauses) throws IOException, ParseException {
        BasicDBObject match = new BasicDBObject();

        String[] filteringValues = null;
        String filteringField = null;

        // check if we need to filter list valued metric by another expanded metric
        if (clauses.exists(Parameters.EXPANDED_METRIC_NAME)) {
            Metric expandable = clauses.getExpandedMetric();

            if (expandable != null && !(expandable instanceof CumulativeMetric)) {
                filteringValues = getExpandedMetricValues(clauses, expandable);
                filteringField = ((Expandable)expandable).getExpandedField();
                match.put(filteringField, new BasicDBObject("$in", filteringValues));

                // remove already used by expandable metric and so redundant parameters
                clauses = clauses.cloneAndRemove(MetricFilter.FACTORY);
                clauses = clauses.cloneAndRemove(MetricFilter.REFERRER);
            }

            clauses = fixDateParametersDueToExpandedMetric(clauses, expandable);
        }

        setDateFilter(clauses, match);

        List<Object> userFilters = new ArrayList<>();
        List<Object> wsFilters = new ArrayList<>();

        for (MetricFilter filter : clauses.getFilters()) {
            String field = filter.toString().toLowerCase();
            Object value = clauses.get(filter);
            if (isNullOrEmpty(value)) {
                continue;
            }

            if (filter == MetricFilter.USER_COMPANY || filter == MetricFilter.USER_FIRST_NAME || filter == MetricFilter.USER_LAST_NAME) {
                if (metric instanceof AbstractUsersProfile) {
                    match.put(field, convertToPattern(value));
                } else {
                    String userFieldName = MetricFilter.USER_ID.toString().toLowerCase();
                    DBObject usersToFilter = (userFieldName.equals(filteringField))
                                             ? getUsers(filter, value, filteringValues)
                                             : getUsers(filter, value, null);
                    userFilters.add(usersToFilter);
                }

            } else if (filter == MetricFilter.USER_ID || (filter == MetricFilter._ID && metric instanceof AbstractUsersProfile)) {
                userFilters.add(processFilter(value, false));

            } else if (filter == MetricFilter.USER) {
                if (value.equals(Parameters.USER_TYPES.REGISTERED.name())) {
                    match.put(MetricFilter.REGISTERED_USER.toString().toLowerCase(), 1);
                } else if (value.equals(Parameters.USER_TYPES.ANONYMOUS.name())) {
                    match.put(MetricFilter.REGISTERED_USER.toString().toLowerCase(), 0);
                } else if (!value.equals(Parameters.USER_TYPES.ANY.name())) {
                    String[] userIds = getUsersIDs(value);
                    userFilters.add(processFilter(userIds, false));
                }

            } else if (filter == MetricFilter.WS_ID || (filter == MetricFilter._ID && metric instanceof AbstractWorkspacesProfile)) {
                wsFilters.add(processFilter(value, false));

            } else if (filter == MetricFilter.WS) {
                if (value.equals(Parameters.WS_TYPES.PERSISTENT.name())) {
                    match.put(MetricFilter.PERSISTENT_WS.toString().toLowerCase(), 1);
                } else if (value.equals(Parameters.WS_TYPES.TEMPORARY.name())) {
                    match.put(MetricFilter.PERSISTENT_WS.toString().toLowerCase(), 0);
                } else if (!value.equals(Parameters.WS_TYPES.ANY.name())) {
                    String[] wsIds = getWorkspaceIDs(value);
                    wsFilters.add(processFilter(wsIds, false));
                }

            } else if (filter == MetricFilter.PARAMETERS) {
                match.putAll(Utils.fetchEncodedPairs(clauses.getAsString(filter), true));

            } else {
                match.put(field, processFilter(value, filter.isNumericType()));
            }
        }

        mergerFilter(userFilters, match, metric instanceof AbstractUsersProfile ? MetricFilter._ID.toString().toLowerCase()
                                                                                : MetricFilter.USER.toString().toLowerCase());

        mergerFilter(wsFilters, match, metric instanceof AbstractWorkspacesProfile ? MetricFilter._ID.toString().toLowerCase()
                                                                                   : MetricFilter.WS.toString().toLowerCase());

        return new BasicDBObject("$match", match);
    }

    /** Add either {key: value} or {$and: [{key:value1, key:value2, ...}]} */
    private void mergerFilter(List<Object> values, BasicDBObject match, String fieldName) {
        if (values.size() == 1) {
            match.put(fieldName, values.get(0));
        } else if (values.size() > 1) {
            Object[] objects = new Object[values.size()];
            for (int i = 0; i < values.size(); i++) {
                objects[i] = new BasicDBObject(fieldName, values.get(i));
            }
            match.put("$and", objects);
        }
    }

    /**
     * @return expanded metric values array with size limited to 100000.
     * Array size limitation needs to avoid reaching of limit=16MB of BSON Document Size used for $in operator.
     * @see http://stackoverflow.com/questions/5331549/what-is-the-maximum-number-of-parameters-passed-to-in-query-in-mongodb
     */
    @SuppressWarnings("JavadocReference")
    public String[] getExpandedMetricValues(Context context, Metric expandable) throws ParseException, IOException {
        Context.Builder builder = new Context.Builder(context);  // unlink context from caller method
        builder.remove(Parameters.EXPANDED_METRIC_NAME);

        // get all data without pagination
        builder.remove(Parameters.PAGE);
        builder.remove(Parameters.PER_PAGE);

        // remove already useless time parameters
        builder.remove(Parameters.TIME_INTERVAL);
        builder.remove(Parameters.TIME_UNIT);

        context = builder.build();

        ValueData metricValue = ((Expandable)expandable).getExpandedValue(context);

        List<ValueData> allMetricValues = treatAsList(metricValue);

        // return empty view data if there is empty metricValue
        if (allMetricValues.size() == 0) {
            return new String[0];  // return empty array
        }

        List<String> values = new ArrayList<>(allMetricValues.size());

        outer:
        for (ValueData rowValue : allMetricValues) {
            MapValueData row = (MapValueData)rowValue;
            for (Map.Entry<String, ValueData> entry : row.getAll().entrySet()) {
                values.add(entry.getValue().getAsString());

                if (values.size() > Expandable.LIMIT) {
                    break outer;
                }
            }
        }

        return values.toArray(new String[values.size()]);
    }

    /**
     * Fix date parameters due to specific expanded metric filter
     */
    private Context fixDateParametersDueToExpandedMetric(Context clauses, Metric expandedMetric) throws ParseException {
        // fix date clauses to display non-active users or workspaces
        if (expandedMetric instanceof NonActiveUsers || expandedMetric instanceof NonActiveWorkspaces) {
            if (clauses.exists(Parameters.FROM_DATE)) {
                Calendar fromDate = clauses.getAsDate(Parameters.FROM_DATE);
                clauses = new Context.Builder(clauses)
                        .put(Parameters.TO_DATE, fromDate)  // set to_date = from_date
                        .remove(Parameters.FROM_DATE)       // remove from_date
                        .build();
            }

            // remove from_date clause to display all documents to_date
        } else if (expandedMetric instanceof WithoutFromDateParam) {
            clauses = clauses.cloneAndRemove(Parameters.FROM_DATE);
        }

        return clauses;
    }


    private boolean isNullOrEmpty(Object value) {
        return value == null || (value instanceof String && ((String)value).isEmpty());
    }

    public static Object processFilter(Object value, boolean isNumericType) throws IOException {
        if (value.getClass().isArray()) {
            return new BasicDBObject("$in", value);
        } else if (value instanceof DBObject || value instanceof Pattern) {
            return value;
        } else {
            return processStringValue((String)value, isNumericType);
        }
    }

    private static Object processStringValue(String value, boolean isNumericType) {
        boolean processExclusiveValues = value.startsWith(EXCLUDE_SIGN);
        if (processExclusiveValues) {
            value = value.substring(EXCLUDE_SIGN.length());
        }

        String[] values = value.split(SEPARATOR);
        if (processExclusiveValues) {
            return new BasicDBObject("$nin", isNumericType ? convertToNumericFormat(values) : values);
        } else {
            return new BasicDBObject("$in", isNumericType ? convertToNumericFormat(values) : values);
        }
    }

    private static long[] convertToNumericFormat(String[] values) {
        long[] result = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Long.parseLong(values[i]);
        }

        return result;
    }

    private Object convertToPattern(Object value) throws IOException {
        if (value instanceof Pattern) {
            return value;

        } else if (value instanceof Pattern[]) {
            return new BasicDBObject("$in", value);

        } else if (value instanceof String) {
            return stringFilterToPattern((String)value);

        } else if (value instanceof String[]) {
            return new BasicDBObject("$in", getPatterns((String[])value));

        } else {
            throw new IllegalArgumentException("Unsupported type " + value.getClass());
        }
    }

    protected Object stringFilterToPattern(String value) {
        boolean processExclusiveValues = value.startsWith(MongoDataLoader.EXCLUDE_SIGN);
        if (processExclusiveValues) {
            value = value.substring(MongoDataLoader.EXCLUDE_SIGN.length());
        }

        Pattern[] patterns = getPatterns(value.split(MongoDataLoader.SEPARATOR));
        return new BasicDBObject(processExclusiveValues ? "$nin" : "$in", patterns);
    }

    private Pattern[] getPatterns(String[] values) {
        Pattern[] patterns = new Pattern[values.length];
        for (int i = 0; i < values.length; i++) {
            patterns[i] = Pattern.compile(Pattern.quote(values[i]), Pattern.CASE_INSENSITIVE);
        }
        return patterns;
    }

    /**
     * @return if usersToFilter != null: (usersToFilter INTERSECT usersFromFilter)
     * otherwise: usersFromFilter
     */
    private DBObject getUsers(MetricFilter filter, Object value, String[] usersToFilter) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(filter, value);
        Context context = builder.build();

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        List<ValueData> users = getAsList(metric, context).getAll();

        String[] result = new String[users.size()];

        for (int i = 0; i < users.size(); i++) {
            MapValueData user = (MapValueData)users.get(i);
            Map<String, ValueData> profile = user.getAll();

            result[i] = profile.get(ID).getAsString();
        }

        if (usersToFilter != null) {
            result = Utils.arrayIntersect(result, usersToFilter);
        }

        return new BasicDBObject("$in", result);
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
            match.put(AbstractMetric.DATE, dateFilter);
        }
    }

    private String[] getUsersIDs(Object aliases) throws IOException {
        return getIDsByNames(MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST), aliases, MetricFilter.USER_ID, MetricFilter.ALIASES);
    }

    private String[] getWorkspaceIDs(Object names) throws IOException {
        return getIDsByNames(MetricFactory.getMetric(MetricType.WORKSPACES_PROFILES_LIST), names, MetricFilter.WS_ID, MetricFilter.WS_NAME);
    }

    private String[] getIDsByNames(Metric metric, Object filterValue, MetricFilter idFilter, MetricFilter nameFilter) throws IOException {
        if (!(filterValue instanceof String)) {
            throw new IllegalStateException("Only string filter is supported");
        }

        String value = (String)filterValue;

        boolean processExclusiveValues = value.startsWith(EXCLUDE_SIGN);
        if (processExclusiveValues) {
            value = value.substring(EXCLUDE_SIGN.length());
        }

        Set<String> entityIds = getFilterAsSet(value);
        Set<String> entityNames = new HashSet<>();

        Iterator<String> iter = entityIds.iterator();
        while (iter.hasNext()) {
            String next = iter.next();

            if (!isWorkspaceID(next) && !isUserID(next)) {
                entityNames.add(next);
                iter.remove();
            }
        }

        // convert names into ids
        if (!entityNames.isEmpty()) {
            String[] ids = doGetIDs(metric, nameFilter, entityNames.toArray(new String[entityNames.size()]));
            entityIds.addAll(Arrays.asList(ids));
        }

        if (processExclusiveValues) {
            return doGetIDs(metric, idFilter, EXCLUDE_SIGN + getFilterAsString(entityIds));
        } else {
            return entityIds.toArray(new String[entityIds.size()]);
        }
    }

    private String[] doGetIDs(Metric metric, MetricFilter filterName, Object filterValue) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(filterName, filterValue);
        Context context = builder.build();

        List<ValueData> rows = getAsList(metric, context).getAll();

        String[] ids = new String[rows.size()];
        for (int i = 0; i < rows.size(); i++) {
            ids[i] = treatAsMap(rows.get(i)).get(ID).getAsString();
        }

        return ids;
    }

    private ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
        Class<? extends ValueData> clazz = metric.getValueDataClass();

        if (clazz == LongValueData.class) {
            return createLongValueData(iterator, metric.getTrackedFields());

        } else if (clazz == DoubleValueData.class) {
            return createDoubleValueData(iterator, metric.getTrackedFields());

        } else if (clazz == SetValueData.class) {
            return createSetValueData(iterator, metric.getTrackedFields());

        } else if (clazz == ListValueData.class) {
            return createListValueData(iterator, metric.getTrackedFields());
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private ValueData createListValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, ListValueData.class, new CreateValueAction() {
            Map<String, ValueData> values = new LinkedHashMap<>();

            @Override
            public void accumulate(String key, Object value) {
                this.values.put(key, ValueDataFactory.createValueData(value));
            }

            @Override
            public ValueData pull() {
                try {
                    return new ListValueData(Arrays.asList(new ValueData[]{new MapValueData(values)}));
                } finally {
                    values.clear();
                }
            }
        });
    }

    private ValueData createSetValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, SetValueData.class, new CreateValueAction() {
            Set<ValueData> values = new HashSet<>();

            @Override
            public void accumulate(String key, Object value) {
                this.values.add(ValueDataFactory.createValueData(value));
            }

            @Override
            public ValueData pull() {
                try {
                    return new SetValueData(values);
                } finally {
                    values.clear();
                }
            }
        });
    }

    private ValueData createLongValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, LongValueData.class, new CreateValueAction() {
            long value = 0;

            @Override
            public void accumulate(String key, Object value) {
                this.value += ((Number)value).longValue();
            }

            @Override
            public ValueData pull() {
                try {
                    return new LongValueData(value);
                } finally {
                    value = 0;
                }
            }
        });
    }

    private ValueData createDoubleValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, DoubleValueData.class, new CreateValueAction() {
            double value = 0;

            @Override
            public void accumulate(String key, Object value) {
                this.value += ((Number)value).doubleValue();
            }

            @Override
            public ValueData pull() {
                try {
                    return new DoubleValueData(value);
                } finally {
                    value = 0;
                }
            }
        });
    }

    /**
     * @param iterator
     *         the iterator over result set
     * @param trackedFields
     *         the list of trackedFields indicate which data to read from resulted items
     * @param clazz
     *         the resulted class of {@link ValueData}
     * @param action
     *         the delegated action, contains behavior how to created needed result depending on given clazz
     */
    private ValueData doCreateValueData(Iterator<DBObject> iterator,
                                        String[] trackedFields,
                                        Class<? extends ValueData> clazz,
                                        CreateValueAction action) {

        ValueData result = ValueDataFactory.createDefaultValue(clazz);

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();

            for (String key : trackedFields) {
                if (dbObject.containsField(key) && dbObject.get(key) != null) {
                    action.accumulate(key, dbObject.get(key));
                }
            }

            result = result.add(action.pull());
        }

        return result;
    }

    /**
     * Create value action.
     */
    private interface CreateValueAction {

        /**
         * Accumulates every key-value pair over every entry for single resulted item
         */
        void accumulate(String key, Object value);

        /**
         * Creates a {@link ValueData}.
         */
        ValueData pull();
    }

    /**
     * Load value from storage action.
     */
    private interface LoadValueAction {
        ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator);

        Iterator<DBObject> iterator(ReadBasedMetric metric, Context clauses, DBCollection dbCollection, DBObject filter);
    }
}
