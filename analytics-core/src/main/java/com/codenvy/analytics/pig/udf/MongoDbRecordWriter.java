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
package com.codenvy.analytics.pig.udf;

import com.mongodb.*;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoDbRecordWriter extends RecordWriter<WritableComparable, Tuple> {

    /** Collection to write data in. */
    protected DBCollection dbCollection;

    /** Mongo client. Have to be closed. */
    protected MongoClient mongoClient;

    /** MongoDbRecordWriter constructor. */
    MongoDbRecordWriter(String serverUrl) throws IOException {
        MongoClientURI uri = new MongoClientURI(serverUrl);
        mongoClient = new MongoClient(uri);

        DB db = mongoClient.getDB(uri.getDatabase());
        if (uri.getUsername() != null) {
            db.authenticate(uri.getUsername(), uri.getPassword());
        }

        db.setWriteConcern(WriteConcern.ACKNOWLEDGED);

        this.dbCollection = db.getCollection(uri.getCollection());
    }

    /** {@inheritedDoc) */
    @Override
    public void write(WritableComparable key, Tuple value) throws IOException, InterruptedException {
        DBObject dbObject = new BasicDBObject();

        dbObject.put("_id", value.get(0));
        for (int i = 1; i < value.size(); i++) {
            Tuple tuple = (Tuple)value.get(i);
            dbObject.put(tuple.get(0).toString(), tuple.get(1));
        }

        dbCollection.save(dbObject);
    }

    /** {@inheritedDoc) */
    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        mongoClient.close();
    }
}
