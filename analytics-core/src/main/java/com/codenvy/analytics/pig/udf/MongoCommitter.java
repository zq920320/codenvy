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

import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MongoCommitter extends OutputCommitter {

    /** {@inheritedDoc) */
    @Override
    public void setupJob(JobContext jobContext) throws IOException {
    }

    /** {@inheritedDoc) */
    @Override
    public void cleanupJob(JobContext jobContext) throws IOException {
    }

    /** {@inheritedDoc) */
    @Override
    public void setupTask(TaskAttemptContext taskContext) throws IOException {
    }

    /** {@inheritedDoc) */
    @Override
    public boolean needsTaskCommit(TaskAttemptContext taskContext) throws IOException {
        return false;
    }

    /** {@inheritedDoc) */
    @Override
    public void commitTask(TaskAttemptContext taskContext) throws IOException {
    }

    /** {@inheritedDoc) */
    @Override
    public void abortTask(TaskAttemptContext taskContext) throws IOException {
    }
}
