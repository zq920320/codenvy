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

import com.codenvy.analytics.Configurator;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.Parameters;
import com.mongodb.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDataLoader implements DataLoader {

    public static final String MONGO_DATA_LOADER_HOST     = "mongo.data.loader.host";
    public static final String MONGO_DATA_LOADER_PORT     = "mongo.data.loader.port";
    public static final String MONGO_DATA_LOADER_USER     = "mongo.data.loader.user";
    public static final String MONGO_DATA_LOADER_PASSWORD = "mongo.data.loader.password";
    public static final String MONGO_DATA_LOADER_DB       = "mongo.data.loader.db";
    public static final String VALUE_KEY                  = "value";

    private final DB db;

    public MongoDataLoader() throws IOException {
        StringBuilder serverUrl = new StringBuilder();
        serverUrl.append(Configurator.getString(MONGO_DATA_LOADER_HOST));
        serverUrl.append(":");
        serverUrl.append(Configurator.getString(MONGO_DATA_LOADER_PORT));

        MongoClient mongoClient = new MongoClient(serverUrl.toString());
        db = mongoClient.getDB(Configurator.getString(MONGO_DATA_LOADER_DB));

        if (!Configurator.getString(MONGO_DATA_LOADER_USER).isEmpty()) {
            db.authenticate(Configurator.getString(MONGO_DATA_LOADER_USER),
                            Configurator.getString(MONGO_DATA_LOADER_PASSWORD).toCharArray());
        }
    }

    /** {@inheritDoc} */
    @Override
    public ValueData loadValue(Metric metric, Map<String, String> clauses) throws IOException {
        DBCollection dbCollection = db.getCollection(metric.getName().toLowerCase());

        try {
            DBObject matcher = getMatcher(clauses);
            DBObject aggregator = getAggregator();
            AggregationOutput aggregation = dbCollection.aggregate(matcher, aggregator);

            return createdValueData(metric.getValueDataClass(), aggregation.results().iterator());
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    private DBObject getMatcher(Map<String, String> clauses) throws ParseException {
        BasicDBObject dbObject = new BasicDBObject();

        for (Map.Entry<String, String> entry : clauses.entrySet()) {
            String param = entry.getKey();

            if (Parameters.TO_DATE.toString().equals(param)) {
                if (Parameters.TO_DATE.get(clauses).equals(Parameters.FROM_DATE.get(clauses))) {
                    dbObject.put("$match", new BasicDBObject("_id", Long.parseLong(entry.getValue())));
                } else {
                    DBObject range = new BasicDBObject();
                    range.put("$gte", Long.parseLong(Parameters.FROM_DATE.get(clauses)));
                    range.put("$lte", Long.parseLong(Parameters.TO_DATE.get(clauses)));

                    dbObject.put("$match", new BasicDBObject("_id", range));
                }
            }
        }

        return dbObject;
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
