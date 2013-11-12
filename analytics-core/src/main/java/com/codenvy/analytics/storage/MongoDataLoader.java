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
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.Parameters;
import com.mongodb.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDataLoader implements DataLoader {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MongoDataLoader.class);

    public static final  String VALUE_KEY              = "value";
    public static final  String COLLECTION_NAME_SUFFIX = "-raw";
    private static final long   DAY_IN_MILLISECONDS    = 86400000L;

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
    public ValueData loadValue(Metric metric, Map<String, String> clauses) throws IOException {
        DBCollection dbCollection = db.getCollection(getCollectionName(metric, clauses));

        try {
            DBObject matcher = getMatcher(clauses);
            DBObject aggregator = getAggregator();
            AggregationOutput aggregation = dbCollection.aggregate(matcher, aggregator);

            return createdValueData(metric.getValueDataClass(), aggregation.results().iterator());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ValueData loadParamValue(Metric metric, Map<String, String> clauses) throws IOException {
        // TODO

        DBCollection dbCollection = db.getCollection(getCollectionName(metric, clauses));

        try {
            DBObject matcher = getMatcher(clauses);
            DBObject aggregator = getAggregator();
            AggregationOutput aggregation = dbCollection.aggregate(matcher, aggregator);

            return createdValueData(metric.getValueDataClass(), aggregation.results().iterator());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private String getCollectionName(Metric metric, Map<String, String> clauses) {
        if (Utils.getFilters(clauses).isEmpty()) {
            return metric.getName().toLowerCase();
        } else {
            return metric.getName().toLowerCase() + COLLECTION_NAME_SUFFIX;
        }
    }

    private DBObject getMatcher(Map<String, String> clauses) throws ParseException {
        BasicDBObject match = new BasicDBObject();

        if (Parameters.TO_DATE.exists(clauses) && Parameters.FROM_DATE.exists(clauses)) {
            DBObject range = new BasicDBObject();
            range.put("$gte", Utils.getFromDate(clauses).getTimeInMillis());
            range.put("$lt", Utils.getToDate(clauses).getTimeInMillis() + DAY_IN_MILLISECONDS);

            match.put("_id", range);
        }

        for (MetricFilter filter : Utils.getFilters(clauses)) {
            String[] values = filter.get(clauses).split(",");
            match.put(filter.name().toLowerCase(), new BasicDBObject("$in", values));
        }
        return new BasicDBObject("$match", match);
    }

    private DBObject getAggregator() throws ParseException {
        DBObject group = new BasicDBObject();
        group.put("_id", null);
        group.put(VALUE_KEY, new BasicDBObject("$sum", "$" + VALUE_KEY));

        return new BasicDBObject("$group", group);
    }

    private ValueData createdValueData(Class<? extends ValueData> clazz, Iterator<DBObject> iterator) {
        if (clazz == LongValueData.class) {
            return createLongValueData(iterator);
        }

        throw new IllegalArgumentException("Unknown class " + clazz.getName());
    }

    private ValueData createLongValueData(Iterator<DBObject> iterator) {
        long value = 0;

        while (iterator.hasNext()) {
            DBObject dbObject = iterator.next();
            value += (Long)dbObject.get(VALUE_KEY);
        }

        return new LongValueData(value);
    }
}
