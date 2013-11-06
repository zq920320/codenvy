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

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.*;
import org.apache.pig.data.Tuple;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoOutputFormat extends OutputFormat<WritableComparable, Tuple> {

    /** {@inheritDoc} */
    @Override
    public RecordWriter<WritableComparable, Tuple> getRecordWriter(TaskAttemptContext context) throws IOException,
                                                                                                      InterruptedException {
        String serverUrl = context.getConfiguration().get(MongoStorage.SERVER_URL_PARAM);
        return new MongoDbRecordWriter(serverUrl);
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

