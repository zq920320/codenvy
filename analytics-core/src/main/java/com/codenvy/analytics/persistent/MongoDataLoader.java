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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataFactory;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDataLoader implements DataLoader {

    private final DB db;

    MongoDataLoader(DB db) {
        this.db = db;
    }

    @Override
    public ValueData loadValue(ReadBasedMetric metric, Context clauses) throws IOException {
        DBCollection dbCollection = db.getCollection(metric.getStorageCollectionName());

        try {
            DBObject filter = metric.getFilter(clauses);
            
            DBObject[] dbOperations;
            // don't use pagination in case of filtering by using expanded metric values
            if (clauses.exists(Parameters.EXPANDED_METRIC_NAME)) {
                dbOperations = metric.getDBOperations(clauses, false);
            } else {
                dbOperations = metric.getDBOperations(clauses, true);                
            }

            AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperations);

            return createdValueData(metric, aggregation.results().iterator());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    public ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
        Class<? extends ValueData> clazz = metric.getValueDataClass();

        if (clazz == LongValueData.class) {
            return createLongValueData(iterator, metric.getTrackedFields());

        } else if (clazz == DoubleValueData.class) {
            return createDoubleValueData(iterator, metric.getTrackedFields());

        } else if (clazz == MapValueData.class) {
            return createMapValueData(iterator, metric.getTrackedFields());

        } else if (clazz == SetValueData.class) {
            return createSetValueData(iterator, metric.getTrackedFields());

        } else if (clazz == ListValueData.class) {
            return createListValueData(iterator, metric.getTrackedFields());
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private ValueData createListValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, ListValueData.class, new Action() {
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
        return doCreateValueData(iterator, trackedFields, SetValueData.class, new Action() {
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

    private ValueData createMapValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, MapValueData.class, new Action() {
            Map<String, ValueData> values = new HashMap<>();

            @Override
            public void accumulate(String key, Object value) {
                this.values.put(key, ValueDataFactory.createValueData(value));
            }

            @Override
            public ValueData pull() {
                try {
                    return new MapValueData(values);
                } finally {
                    values.clear();
                }
            }
        });
    }

    private ValueData createLongValueData(Iterator<DBObject> iterator, String[] trackedFields) {
        return doCreateValueData(iterator, trackedFields, LongValueData.class, new Action() {
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
        return doCreateValueData(iterator, trackedFields, LongValueData.class, new Action() {
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
                                        Action action) {

        ValueData result = ValueDataFactory.createDefaultValue(clazz);

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();

            for (String key : trackedFields) {
                if (dbObject.containsField(key) && dbObject.get(key) != null) {
                    action.accumulate(key, dbObject.get(key));
                }
            }

            result = result.union(action.pull());
        }

        return result;
    }

    /** Create value action. */
    private interface Action {

        /**
         * Accumulates every key-value pair over every entry for single resulted item
         *
         * @param key
         *         the key
         * @param value
         *         the corresponding value
         */
        void accumulate(String key, Object value);

        /**
         * Creates a {@link ValueData}.
         *
         * @return the {@link ValueData}
         */
        ValueData pull();
    }

    @Override
    public ListValueData loadExpandedValue(ReadBasedMetric metric, Context context, List<DBObject> projection) throws IOException {
        final String SUM_OPERATOR_PATTERN = "{ \"$sum\"";
        
        DBCollection dbCollection = db.getCollection(metric.getStorageCollectionName());

        try {
            DBObject filter = metric.getFilter(context);
            DBObject[] dbOperations = metric.getDBOperations(context, true);

            // remove "$group" and "$project" operators
            List<DBObject> dbOperationsForExplanation = new ArrayList<>();
            for (DBObject dbObject: Arrays.asList(dbOperations)) {
                if (! (dbObject.containsField("$group")
                        && dbObject.toString().contains(SUM_OPERATOR_PATTERN))) {
                    dbOperationsForExplanation.add(dbObject);
                } else {
                    dbOperationsForExplanation.addAll(projection);  // add projection in place of "$sum" operator                    
                }
            }

            AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperationsForExplanation.toArray(new DBObject[0]));

            return (ListValueData) createListValueData(aggregation.results().iterator(), metric.getTrackedFields());
        } catch (ParseException e) {
            throw new IOException(e);
        }
        
    }
}
