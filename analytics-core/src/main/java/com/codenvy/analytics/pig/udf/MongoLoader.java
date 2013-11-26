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

import org.apache.hadoop.mapreduce.*;
import org.apache.pig.*;
import org.apache.pig.backend.executionengine.ExecException;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
import org.apache.pig.impl.util.Utils;
import org.apache.pig.parser.ParserException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoLoader extends LoadFunc implements LoadMetadata {

    public static final String SERVER_URL_PARAM = "server.url";

    private final TupleFactory   tupleFactory;
    private final ResourceSchema schema;

    private MongoReader reader;

    public MongoLoader(String schema) throws ParserException {
        this.tupleFactory = TupleFactory.getInstance();
        this.schema = new ResourceSchema(Utils.parseSchema(schema));
    }

    /** {@inheritDoc} */
    @Override
    public void setLocation(String location, Job job) throws IOException {
        job.getConfiguration().set(SERVER_URL_PARAM, location);
    }

    /** {@inheritDoc} */
    @Override
    public InputFormat getInputFormat() throws IOException {
        return new MongoInputFormat();
    }

    /** {@inheritDoc} */
    @Override
    public void prepareToRead(RecordReader reader, PigSplit split) throws IOException {
        this.reader = (MongoReader)reader;
    }

    /** {@inheritDoc} */
    @Override
    public Tuple getNext() throws IOException {
        try {
            Tuple tuple = null;

            if (reader.nextKeyValue()) {
                DBObject value = (DBObject)reader.getCurrentValue();

                tuple = tupleFactory.newTuple(schema.getFields().length);
                for (int i = 0; i < schema.getFields().length; i++) {
                    String key = schema.getFields()[i].getName();
                    tuple.set(i, value.get(key));
                }
            }

            return tuple;
        } catch (InterruptedException e) {
            throw new ExecException(e.getMessage(), e);
        }
    }

    @Override
    public ResourceSchema getSchema(String location, Job job) throws IOException {
        return schema;
    }

    @Override
    public ResourceStatistics getStatistics(String location, Job job) throws IOException {
        return null;
    }

    @Override
    public String[] getPartitionKeys(String location, Job job) throws IOException {
        return null;
    }

    @Override
    public void setPartitionFilter(Expression partitionFilter) throws IOException {
    }

    /** MongoDB implementation for {@link InputFormat} */
    private class MongoInputFormat extends InputFormat {
        @Override
        public List<InputSplit> getSplits(JobContext jobContext) throws IOException, InterruptedException {
            MongoInputSplit split = new MongoInputSplit(jobContext.getConfiguration());
            return Arrays.asList(new InputSplit[]{split});
        }

        @Override
        public RecordReader createRecordReader(InputSplit inputSplit, TaskAttemptContext taskAttemptContext)
                throws IOException, InterruptedException {
            return new MongoReader(inputSplit.getLocations()[0]);
        }
    }

    /** MongoDB implementation for {@link RecordReader} */
    private class MongoReader extends RecordReader {

        private DBCursor    dbCursor;
        private MongoClient mongoClient;
        private int         progress;


        public MongoReader(String serverUrl) throws IOException {
            MongoClientURI uri = new MongoClientURI(serverUrl);
            mongoClient = new MongoClient(uri);

            DB db = mongoClient.getDB(uri.getDatabase());
            if (uri.getUsername() != null && !uri.getUsername().isEmpty()) {
                db.authenticate(uri.getUsername(), uri.getPassword());
            }

            dbCursor = db.getCollection(uri.getCollection()).find();
        }

        @Override
        public void initialize(InputSplit inputSplit, TaskAttemptContext taskAttemptContext)
                throws IOException, InterruptedException {
        }

        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            return dbCursor.hasNext();
        }

        @Override
        public Object getCurrentKey() throws IOException, InterruptedException {
            return null;
        }

        @Override
        public Object getCurrentValue() throws IOException, InterruptedException {
            progress++;
            return dbCursor.next();
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            return progress / dbCursor.size();
        }

        @Override
        public void close() throws IOException {
            mongoClient.close();
        }
    }
}

