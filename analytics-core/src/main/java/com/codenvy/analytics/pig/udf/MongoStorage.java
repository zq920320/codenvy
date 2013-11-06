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

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.pig.StoreFunc;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoStorage extends StoreFunc {

    /**
     * Job parameter where destination server url is stored.
     * Serve url parameter has the next format: user:password@server:port
     */
    public static final String SERVER_URL_PARAM = "server.url";

    /**
     * Job parameter where script type value is stored. Every Pig-latin
     * script has its own resulted format to be stored. So, it is needed
     * to use special {@link TupleTransformer} then.
     */
    public static final String SCRIPT_TYPE_PARAM = "script.type";

    /** Writer to mongo storage. */
    private RecordWriter writer;

    /** Contains Pig-latin script type {@link ScriptType}. */
    private final String scriptType;

    /** {@link MongoStorage} constructor. */
    public MongoStorage(String scriptType) {
        this.scriptType = scriptType;
    }

    /** {@inheritedDoc) */
    @Override
    public OutputFormat getOutputFormat() throws IOException {
        return new MongoOutputFormat();
    }

    /** {@inheritedDoc) */
    @Override
    public void setStoreLocation(String location, Job job) throws IOException {
        job.getConfiguration().set(SERVER_URL_PARAM, location);
        job.getConfiguration().set(SCRIPT_TYPE_PARAM, scriptType);
    }

    /** {@inheritedDoc) */
    @Override
    public void prepareToWrite(RecordWriter writer) throws IOException {
        this.writer = writer;
    }

    /** {@inheritedDoc) */
    @Override
    public void putNext(Tuple t) throws IOException {
        try {
            writer.write(null, t);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}

