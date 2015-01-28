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

import com.codenvy.api.account.server.MemoryUsedMetric;
import com.codenvy.api.account.server.UsageInformer;
import com.codenvy.api.core.ServerException;

import org.openjdk.jmh.annotations.*;

import java.sql.SQLException;

/**
 * @author Sergii Kabashniuk
 */
@State(Scope.Benchmark)
public class InsertBench extends BenchBase {

    @Benchmark
    public UsageInformer insertMetric() throws SQLException, ServerException {

        return storage.createMemoryUsedRecord(
                new MemoryUsedMetric(256, System.currentTimeMillis(), System.currentTimeMillis() + 100, "user-1231",
                                     "acc-12450", "ws-1232345", "run-123123"));
    }




//    @TearDown(Level.Iteration)
//    public void cleanup() {
//        initializer.clean();
//        initializer.init();
//    }

}
