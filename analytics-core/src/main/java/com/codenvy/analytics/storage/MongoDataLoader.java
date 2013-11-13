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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.ReadBasedMetric;
import com.mongodb.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDataLoader implements DataLoader {

    public static final  String VALUE_KEY                  = "value";
    public static final  String EXT_COLLECTION_NAME_SUFFIX = "-raw";
    private static final long   DAY_IN_MILLISECONDS        = 86400000L;

    private final DB db;

    MongoDataLoader(MongoClientURI clientURI) throws IOException {
        MongoClient mongoClient = new MongoClient(clientURI);
        db = mongoClient.getDB(clientURI.getDatabase());

        if (isAuthRequired(clientURI)) {
            db.authenticate(clientURI.getUsername(), clientURI.getPassword());
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
            DBObject matcher = getMatcher(metric, clauses);
            DBObject aggregator = getAggregator(metric, clauses);
            AggregationOutput aggregation = dbCollection.aggregate(matcher, aggregator);

            return createdValueData(metric, aggregation.results().iterator());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private String getCollectionName(ReadBasedMetric metric, Map<String, String> clauses) {
        if (isExtendedCollection(clauses)) {
            return metric.getName().toLowerCase() + EXT_COLLECTION_NAME_SUFFIX;
        } else {
            return metric.getName().toLowerCase();
        }
    }

    private boolean isExtendedCollection(Map<String, String> clauses) {
        return Utils.getFilters(clauses).size() > 0;
    }

    private DBObject getMatcher(ReadBasedMetric metric, Map<String, String> clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        DBObject range = new BasicDBObject();
        range.put("$gte", Utils.getFromDate(clauses).getTimeInMillis());
        range.put("$lt", Utils.getToDate(clauses).getTimeInMillis() + DAY_IN_MILLISECONDS);
        match.put("_id", range);

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values = filter.get(clauses).split(",");
            match.put(filter.name().toLowerCase(), new BasicDBObject("$in", values));
        }

        if (isExtendedCollection(clauses)) {
            for (Parameters param : metric.getParams()) {
                if (param != Parameters.FROM_DATE && param != Parameters.TO_DATE) {
                    String[] values = param.get(clauses).split(",");
                    match.put(param.name().toLowerCase(), new BasicDBObject("$in", values));
                }
            }
        }

        return new BasicDBObject("$match", match);
    }

    private DBObject getAggregator(ReadBasedMetric metric, Map<String, String> clauses) throws ParseException {
        DBObject group = new BasicDBObject();
        group.put("_id", null);

        group.put(VALUE_KEY, new BasicDBObject("$sum", "$" + VALUE_KEY));

        if (!isExtendedCollection(clauses)) {
            for (Parameters param : metric.getParams()) {
                if (param != Parameters.FROM_DATE && param != Parameters.TO_DATE) {
                    for (String field : param.get(clauses).split(",")) {
                        group.put(field, new BasicDBObject("$sum", "$" + field));
                    }
                }
            }
        }

        return new BasicDBObject("$group", group);
    }

    private ValueData createdValueData(ReadBasedMetric metric, Iterator<DBObject> iterator) {
        if (metric.getValueDataClass() == LongValueData.class) {
            return createLongValueData(iterator);
        }

        throw new IllegalArgumentException("Unknown class " + metric.getValueDataClass().getName());
    }


    private ValueData createLongValueData(Iterator<DBObject> iterator) {
        long value = 0;

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();
            for (String key : dbObject.keySet()) {
                if (!key.equals("_id")) {
                    value += ((Number)dbObject.get(key)).longValue();
                }
            }
        }

        return new LongValueData(value);
    }
}
