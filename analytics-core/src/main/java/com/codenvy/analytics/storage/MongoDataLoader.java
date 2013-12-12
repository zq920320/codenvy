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
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Pagination;
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

    MongoDataLoader(MongoClientURI clientURI) throws IOException {
        MongoClient mongoClient = new MongoClient(clientURI);
        db = mongoClient.getDB(clientURI.getDatabase());

        if (isAuthRequired(clientURI)) {
            db.authenticate(clientURI.getUsername(), clientURI.getPassword());
        }

        allFilters = new HashSet<>();
        for (MetricFilter filter : MetricFilter.values()) {
            allFilters.add(filter.name().toLowerCase());
        }
    }

    private boolean isAuthRequired(MongoClientURI clientURI) {
        return clientURI.getUsername() != null && !clientURI.getUsername().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public ValueData loadValue(ReadBasedMetric metric, Map<String, String> clauses) throws IOException {
        DBCollection dbCollection = db.getCollection(getCollectionName(metric, clauses));

        try {
            DBObject filter = metric.getFilter(clauses);
            DBObject[] dbOperations = metric.getDBOperations(clauses);
            AggregationOutput aggregation = dbCollection.aggregate(filter, dbOperations);

            PageSupportedIterator iterator = new PageSupportedIterator(aggregation.results().iterator(), clauses);

            return createdValueData(metric, iterator);

        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private String getCollectionName(ReadBasedMetric metric, Map<String, String> clauses) {
        if (metric.getName().equalsIgnoreCase(MetricType.USERS_PROFILES.name()) ||
            metric.getName().equalsIgnoreCase(MetricType.USERS_STATISTICS.name()) ||
            metric.getName().equalsIgnoreCase(MetricType.USERS_ACTIVITY.name())) {

            return metric.getStorageTable();
        }

        if (isExtendedCollection(clauses)) {
            return metric.getStorageTable() + EXT_COLLECTION_NAME_SUFFIX;
        } else {
            return metric.getStorageTable();
        }
    }

    private boolean isExtendedCollection(Map<String, String> clauses) {
        return Utils.getFilters(clauses).size() > 0;
    }

    private ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
        Class<? extends ValueData> clazz = metric.getValueDataClass();

        if (clazz == LongValueData.class) {
            return createLongValueData(iterator);

        } else if (clazz == MapValueData.class) {
            return createMapValueData(iterator);

        } else if (clazz == SetValueData.class) {
            return createSetValueData(iterator);

        } else if (clazz == ListValueData.class) {
            return createListValueData(iterator);
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private ValueData createListValueData(Iterator<DBObject> iterator) {
        return doCreateValueData(iterator, ListValueData.class, new Action() {
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

    private ValueData createSetValueData(Iterator<DBObject> iterator) {
        return doCreateValueData(iterator, SetValueData.class, new Action() {
            Set<ValueData> values = new HashSet<>();

            @Override
            public void accumulate(String key, Object value) {
                if (key.equals("value")) {
                    this.values.add(ValueDataFactory.createValueData(value));
                }
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

    private ValueData createMapValueData(Iterator<DBObject> iterator) {
        return doCreateValueData(iterator, MapValueData.class, new Action() {
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

    private ValueData createLongValueData(Iterator<DBObject> iterator) {
        return doCreateValueData(iterator, LongValueData.class, new Action() {
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

    private ValueData doCreateValueData(Iterator<DBObject> iterator,
                                        Class<? extends ValueData> clazz,
                                        Action action) {

        ValueData result = ValueDataFactory.createDefaultValue(clazz);

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();

            for (String key : dbObject.keySet()) {
                if (!key.equals("_id") && !allFilters.contains(key)) {
                    action.accumulate(key, dbObject.get(key));
                }
            }

            result = result.union(action.pull());
        }

        return result;
    }

    /** Create value action. */
    private interface Action {
        void accumulate(String key, Object value);

        ValueData pull();
    }

    /**
     * Simply implemented {@link Iterator} which supports pagination.
     * First {@link Pagination#PER_PAGE} * ({@link Pagination#PAGE} - 1) elements will be skipped.
     * There is possible performance issue with skip operator in MongoDB when result is extremely huge. So, it is
     * highly recommended to use filters to reduce result as much as possible.
     */
    private class PageSupportedIterator implements Iterator<DBObject> {
        private final long               limit;
        private final Iterator<DBObject> delegated;

        private long processed;

        PageSupportedIterator(Iterator<DBObject> delegated, Map<String, String> clauses) {
            this.delegated = delegated;

            if (Pagination.PAGE.exists(clauses)) {
                limit = Long.valueOf(Pagination.PER_PAGE.get(clauses));
                skip(limit * (Long.valueOf(Pagination.PAGE.get(clauses)) - 1));
            } else {
                limit = Long.MAX_VALUE;
            }
        }

        private void skip(long number) {
            for (int i = 0; i < number; i++) {
                try {
                    delegated.next();
                } catch (NoSuchElementException e) {
                    // the limit is reached
                }
            }
        }

        @Override
        public boolean hasNext() {
            return delegated.hasNext() && limit > processed;
        }

        @Override
        public DBObject next() {
            if (hasNext()) {
                processed++;
                return delegated.next();
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
