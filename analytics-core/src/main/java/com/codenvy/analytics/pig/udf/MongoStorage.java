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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.persistent.CollectionsManagement;
import com.codenvy.analytics.persistent.MongoDataStorage;
import com.mongodb.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.*;
import org.apache.pig.StoreFunc;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoStorage extends StoreFunc {

    public static final String SERVER_URL_PARAM = "server.url";

    private final String user;
    private final String password;

    private RecordWriter writer;

    /** {@link MongoStorage} constructor. */
    public MongoStorage(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /** {@inheritDoc) */
    @Override
    public OutputFormat getOutputFormat() throws IOException {
        return new MongoOutputFormat();
    }

    /** {@inheritDoc) */
    @Override
    public void setStoreLocation(String location, Job job) throws IOException {
        String serverUrl;

        if (user.isEmpty()) {
            serverUrl = location;

        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("mongodb://");
            builder.append(user);
            builder.append(":");
            builder.append(password);
            builder.append("@");
            builder.append(location.substring("mongodb://".length()));

            serverUrl = builder.toString();
        }

        job.getConfiguration().set(SERVER_URL_PARAM, serverUrl);
    }

    /** {@inheritDoc) */
    @Override
    public void prepareToWrite(RecordWriter writer) throws IOException {
        this.writer = writer;
    }

    /** {@inheritDoc) */
    @Override
    public void putNext(Tuple t) throws IOException {
        try {
            writer.write(null, t);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     *
     */
    public static class MongoWriter extends RecordWriter<WritableComparable, Tuple> {

        /** Collection to write data in. */
        protected DBCollection dbCollection;

        public MongoWriter(Configuration configuration) throws IOException {
            MongoClientURI uri = new MongoClientURI(configuration.get(SERVER_URL_PARAM));

            MongoDataStorage mongoDataStorage = Injector.getInstance(MongoDataStorage.class);
            CollectionsManagement collectionsManagement = Injector.getInstance(CollectionsManagement.class);

            DB db = mongoDataStorage.getDb();

            if (!uri.getCollection().startsWith("test") &&
                !collectionsManagement.isCollectionExists(uri.getCollection())) {

                throw new IOException("Collection " + uri.getCollection() + " doesn't exist in configuration");
            }

            this.dbCollection = db.getCollection(uri.getCollection());
        }

        /** {@inheritDoc) */
        @Override
        public void write(WritableComparable key, Tuple value) throws IOException, InterruptedException {
            DBObject dbObject = new BasicDBObject();

            for (int i = 1; i < value.size(); i++) {
                Tuple tuple = (Tuple)value.get(i);

                Object data = tuple.get(1);
                if (data != null) {
                    dbObject.put(tuple.get(0).toString(), data);
                }
            }

            dbCollection.update(new BasicDBObject("_id", value.get(0)),
                                new BasicDBObject("$set", dbObject),
                                true,
                                false);
        }

        /** {@inheritDoc) */
        @Override
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        }
    }

    /** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
    public static class MongoOutputFormat extends OutputFormat<WritableComparable, Tuple> {

        /** {@inheritDoc} */
        @Override
        public RecordWriter<WritableComparable, Tuple> getRecordWriter(TaskAttemptContext context) throws IOException,
                                                                                                          InterruptedException {
            return new MongoWriter(context.getConfiguration());
        }

        /** {@inheritDoc} */
        @Override
        public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
        }

        /** {@inheritDoc} */
        @Override
        public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
            return new MongoCommitter();
        }
    }

    /** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
    public static class MongoCommitter extends OutputCommitter {

        @Override
        public void setupJob(JobContext jobContext) throws IOException {
        }

        @Override
        public void cleanupJob(JobContext jobContext) throws IOException {
        }

        @Override
        public void setupTask(TaskAttemptContext taskContext) throws IOException {
        }

        @Override
        public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
            return false;
        }

        @Override
        public void commitTask(TaskAttemptContext taskContext) throws IOException {
        }

        @Override
        public void abortTask(TaskAttemptContext taskContext) throws IOException {
        }
    }
}

