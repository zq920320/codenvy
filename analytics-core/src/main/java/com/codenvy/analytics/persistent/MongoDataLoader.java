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
package com.codenvy.analytics.persistent;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.*;
import com.codenvy.analytics.metrics.users.AbstractUsersProfile;
import com.codenvy.analytics.metrics.users.NonActiveUsers;
import com.codenvy.analytics.metrics.workspaces.NonActiveWorkspaces;
import com.mongodb.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;

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
        return doLoadValue(metric, clauses, new LoadValueAction() {
            @Override
            public DBObject[] getDBOperations(ReadBasedMetric metric, Context clauses) {
                return MongoDataLoader.this.getDBOperations(metric, clauses);
            }

            @Override
            public ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
                return MongoDataLoader.this.createdValueData(metric, iterator);
            }
        });
    }

    @Override
    public ValueData loadExpandedValue(ReadBasedMetric metric, Context clauses) throws IOException {
        return doLoadValue(metric, clauses, new LoadValueAction() {
            @Override
            public DBObject[] getDBOperations(ReadBasedMetric metric, Context clauses) {
                return getExpandedDBOperations((ReadBasedExpandable)metric, clauses);
            }

            @Override
            public ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
                return createListValueData(iterator, new String[]{((ReadBasedExpandable)metric).getExpandedField()});
            }
        });
    }

    protected ValueData doLoadValue(ReadBasedMetric metric, Context clauses, LoadValueAction action) throws IOException {
        DBCollection dbCollection = db.getCollection(metric.getStorageCollectionName());

        try {
            clauses = metric.applySpecificFilter(clauses);
            DBObject filter = getFilter(metric, clauses);

            DBObject[] dbOperations = action.getDBOperations(metric, clauses);
            AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperations);
            Iterator<DBObject> iterator = aggregation.results().iterator();

            return action.createdValueData(metric, iterator);
        } catch (ParseException e) {
            throw new IOException(e);
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

        // check if we need to filter list valued metric by another expanded metric
        if (clauses.exists(Parameters.EXPANDED_METRIC_NAME)) {
            Metric expandable = clauses.getExpandedMetric();

            if (expandable != null) {
                String[] filteringValues = getExpandedMetricValues(clauses, expandable);
                String filteringField = ((Expandable)expandable).getExpandedField();
                match.put(filteringField, new BasicDBObject("$in", filteringValues));
            }

            clauses = fixDateParametersDueToExpandedMetric(clauses, expandable);
        }

        setDateFilter(clauses, match);

        for (MetricFilter filter : clauses.getFilters()) {
            String field = filter.toString().toLowerCase();
            Object value = clauses.get(filter);
            if (isNullOrEmpty(value)) {
                continue;
            }

            if (!(metric instanceof AbstractUsersProfile)
                && (filter == MetricFilter.USER_COMPANY
                    || filter == MetricFilter.USER_FIRST_NAME
                    || filter == MetricFilter.USER_LAST_NAME)) {

                match.put(MetricFilter.USER.name().toLowerCase(), getUsers(filter, value));

            } else if (!(metric instanceof AbstractUsersProfile) && filter == MetricFilter.USER) {
                if (!value.equals(Parameters.USER_TYPES.ANY.name())) {
                    Object users = processFilter(value, filter.isNumericType());
                    match.put(field, users);
                }
            } else if (filter == MetricFilter.WS) {
                if (!value.equals(Parameters.WS_TYPES.ANY.name())) {
                    Object users = processFilter(value, filter.isNumericType());
                    match.put(field, users);
                }
            } else if (filter == MetricFilter.PARAMETERS) {
                match.putAll(Utils.fetchEncodedPairs(clauses.getAsString(filter)));

            } else {
                match.put(field, processFilter(value, filter.isNumericType()));
            }
        }

        return new BasicDBObject("$match", match);
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
            return new BasicDBObject("$in", processArray((Object[])value));
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
            return new BasicDBObject("$nin", isNumericType ? convertToNumericFormat(values) : processArray(values));
        } else {
            if (values.length == 1) {
                return isNumericType ? Long.parseLong(values[0]) : processArray(values)[0];
            } else {
                return new BasicDBObject("$in", isNumericType ? convertToNumericFormat(values) : processArray(values));
            }
        }
    }

    private static Object[] processArray(Object[] values) {
        Object[] result = new Object[values.length];

        for (int i = 0; i < result.length; i++) {
            Object value = values[i];

            if (value.equals(Parameters.WS_TYPES.TEMPORARY.toString())) {
                result[i] = Metric.TEMPORARY_WS;
            } else if (value.equals(Parameters.WS_TYPES.PERSISTENT.toString())) {
                result[i] = Metric.PERSISTENT_WS;
            } else if (value.equals(Parameters.USER_TYPES.ANONYMOUS.toString())) {
                result[i] = Metric.ANONYMOUS_USER;
            } else if (value.equals(Parameters.USER_TYPES.REGISTERED.toString())) {
                result[i] = Metric.REGISTERED_USER;
            } else {
                result[i] = value;
            }
        }
        return result;
    }


    private static long[] convertToNumericFormat(String[] values) {
        long[] result = new long[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Long.parseLong(values[i]);
        }

        return result;
    }

    private DBObject getUsers(MetricFilter filter, Object value) throws IOException {
        Context.Builder builder = new Context.Builder();
        builder.put(filter, value);
        Context context = builder.build();

        Metric metric = MetricFactory.getMetric(MetricType.USERS_PROFILES_LIST);
        List<ValueData> users = getAsList(metric, context).getAll();

        String[] result = new String[users.size()];

        for (int i = 0; i < users.size(); i++) {
            MapValueData user = (MapValueData)users.get(i);
            Map<String, ValueData> profile = user.getAll();

            result[i] = profile.get(AbstractMetric.ID).getAsString();
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
            Map<String, ValueData> values = new HashMap<>();

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
        return doCreateValueData(iterator, trackedFields, LongValueData.class, new CreateValueAction() {
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
        DBObject[] getDBOperations(ReadBasedMetric metric, Context clauses);

        ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator);
    }
}
