/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.mbstorage.sql;

import com.codenvy.api.account.MemoryUsedMetric;
import com.codenvy.api.account.UsageInformer;
import com.codenvy.api.core.ServerException;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Setup;

import java.sql.SQLException;
import java.util.Map;

/**
 * @author Sergii Kabashniuk
 */
public class SelectSumBench extends BenchBase {


    @Setup(Level.Iteration)
    public void cleanup() throws ServerException {
        storage.createMemoryUsedRecord(
                new MemoryUsedMetric(256, 1L, 10L, "user-1231",
                                     "ac-1", "ws-1232345", "run-123123"));

        storage.createMemoryUsedRecord(
                new MemoryUsedMetric(256, 9L, 16L, "user-1231",
                                     "ac-1", "ws-1232345", "run-123123"));

        storage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024, 11L, 17L, "user-1231",
                                     "ac-1", "ws-123", "run-123123"));

        storage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024, 11L, 17L, "user-1231",
                                     "ac-1", "ws-1232345", "run-123123"));

        storage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024, 11L, 19L, "user-1231",
                                     "ac-1", "ws-1232345", "run-123123"));

        storage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024, 21L, 50L, "user-1231",
                                     "ac-1", "ws-1232345", "run-123123"));

        storage.createMemoryUsedRecord(
                new MemoryUsedMetric(1024, 21L, 50L, "user-1231",
                                     "ac-5", "ws-1232345", "run-123123"));
    }



    @Benchmark
    public Long selectAccountSum() throws SQLException, ServerException {

        return storage.getMemoryUsed("ac-1", 12, 18);
    }

    @Benchmark
    public Map<String, Long> selectAccountReport() throws SQLException, ServerException {

        return storage.getMemoryUsedReport("ac-1", 12, 18);
    }

}
