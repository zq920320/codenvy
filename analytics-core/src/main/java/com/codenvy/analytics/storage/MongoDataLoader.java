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
package com.codenvy.analytics.storage;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDataLoader implements DataLoader {

    public static final String EXT_COLLECTION_NAME_SUFFIX = "-raw";

    private final DB          db;
    private final Set<String> allFilters;

    MongoDataLoader(MongoClient mongoClient) throws IOException {
        db = MongoDataStorage.getUsedDB(mongoClient);

        allFilters = new HashSet<>();
        for (MetricFilter filter : MetricFilter.values()) {
            allFilters.add(filter.name().toLowerCase());
        }
    }

    @Override
    public ValueData loadValue(ReadBasedMetric metric, Map<String, String> clauses) throws IOException {
        DBCollection dbCollection = db.getCollection(getStorageTable(metric, clauses));

        try {
            DBObject filter = metric.getFilter(clauses);
            DBObject[] dbOperations = metric.getDBOperations(clauses);

            AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperations);

            return createdValueData(metric, aggregation.results().iterator());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /**
     * TODO comment
     *
     * @see com.codenvy.analytics.metrics.ReadBasedMetric#getStorageTable()
     */
    private String getStorageTable(ReadBasedMetric metric, Map<String, String> clauses) {
        if (metric.isSingleTable() || Utils.isSimpleContext(clauses)) {
            return metric.getStorageTable();
        } else {
            return metric.getStorageTable() + EXT_COLLECTION_NAME_SUFFIX;
        }
    }

    private ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
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

    private ValueData createListValueData(Iterator<DBObject> iterator, String[] fields) {
        return doCreateValueData(iterator, fields, ListValueData.class, new Action() {
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

    private ValueData createSetValueData(Iterator<DBObject> iterator, String[] fields) {
        return doCreateValueData(iterator, fields, SetValueData.class, new Action() {
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

    private ValueData createMapValueData(Iterator<DBObject> iterator, String[] fields) {
        return doCreateValueData(iterator, fields, MapValueData.class, new Action() {
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

    private ValueData createLongValueData(Iterator<DBObject> iterator, String[] fields) {
        return doCreateValueData(iterator, fields, LongValueData.class, new Action() {
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

    private ValueData createDoubleValueData(Iterator<DBObject> iterator, String[] fields) {
        return doCreateValueData(iterator, fields, LongValueData.class, new Action() {
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
     * @param fields
     *         the list of fields indicate what data to read from resulted item
     * @param clazz
     *         the resulted class of {@link ValueData}
     * @param action
     *         the delegated action, contains behavior how to created needed result depending on given clazz
     */
    private ValueData doCreateValueData(Iterator<DBObject> iterator,
                                        String[] fields,
                                        Class<? extends ValueData> clazz,
                                        Action action) {

        ValueData result = ValueDataFactory.createDefaultValue(clazz);

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();

            if (fields.length == 0) {
                for (String key : dbObject.keySet()) {
                    action.accumulate(key, dbObject.get(key));
                }
            } else {
                for (String key : fields) {
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
}
