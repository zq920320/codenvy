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
    private final Set<String> filters;

    MongoDataLoader(MongoClientURI clientURI) throws IOException {
        MongoClient mongoClient = new MongoClient(clientURI);
        db = mongoClient.getDB(clientURI.getDatabase());

        if (isAuthRequired(clientURI)) {
            db.authenticate(clientURI.getUsername(), clientURI.getPassword());
        }

        filters = new HashSet<>();
        for (MetricFilter filter : MetricFilter.values()) {
            filters.add(filter.name().toLowerCase());
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
            DBObject matcher = metric.getMatcher(clauses);

            if (metric.isAggregationSupport()) {
                DBObject aggregator = metric.getAggregator(clauses);
                AggregationOutput aggregation = dbCollection.aggregate(matcher, aggregator);

                return createdValueData(metric, aggregation.results().iterator());
            } else {
                DBCursor dbCursor = dbCollection.find((DBObject)matcher.get("$match"));
                return createdValueData(metric, dbCursor);
            }
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private String getCollectionName(ReadBasedMetric metric, Map<String, String> clauses) {
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
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private ValueData createSetValueData(Iterator<DBObject> iterator) {
        ValueData result = ValueDataFactory.createDefaultValue(SetValueData.class);

        while (iterator.hasNext()) {
            Set<ValueData> values = new HashSet<>();

            DBObject dbObject = iterator.next();
            for (String key : dbObject.keySet()) {
                if (!key.equals("_id") && !filters.contains(key)) {
                    values.add(ValueDataFactory.createValueData(dbObject.get("value")));
                }
            }

            result = result.union(new SetValueData(values));
        }

        return result;
    }

    private ValueData createMapValueData(Iterator<DBObject> iterator) {
        ValueData result = ValueDataFactory.createDefaultValue(MapValueData.class);

        while (iterator.hasNext()) {
            Map<String, ValueData> values = new HashMap<>();

            DBObject dbObject = iterator.next();
            for (String key : dbObject.keySet()) {
                if (!key.equals("_id") && !filters.contains(key)) {
                    values.put(key, ValueDataFactory.createValueData(dbObject.get(key)));
                }
            }

            result = result.union(new MapValueData(values));
        }

        return result;
    }


    private ValueData createLongValueData(Iterator<DBObject> iterator) {
        long value = 0;

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();
            for (String key : dbObject.keySet()) {
                if (!key.equals("_id") && !filters.contains(key)) {
                    value += ((Number)dbObject.get(key)).longValue();
                }
            }
        }

        return new LongValueData(value);
    }
}
