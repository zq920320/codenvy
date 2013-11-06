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

import com.mongodb.DBCollection;
import com.mongodb.Mongo;

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
    protected Mongo mongo;

//    /** MongoDbRecordWriter constructor. */
//    MongoDbRecordWriter(String serverUrl, TupleTransformer transformer) throws IOException {
//        MongoURI uri = new MongoURI(serverUrl);
//
//        this.mongo = new Mongo(uri);
//        DB db = mongo.getDB(uri.getDatabase());
//
//        if (uri.getUsername() != null) {
//            db.authenticate(uri.getUsername(), uri.getPassword());
//        }
//
//        db.setWriteConcern(WriteConcern.ACKNOWLEDGED);
//
//        this.dbCollection = db.getCollection(uri.getCollection());
//        this.transformer = transformer;
//    }

    /** {@inheritedDoc) */
    @Override
    public void write(WritableComparable key, Tuple value) throws IOException, InterruptedException {
//        try {
//            DBObject dbObject = transformer.transform(value);
//            dbCollection.save(dbObject);
//        } catch (ExecException e) {
//            throw new IOException(e);
//        }
    }

    /** {@inheritedDoc) */
    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        mongo.close();
    }
}
