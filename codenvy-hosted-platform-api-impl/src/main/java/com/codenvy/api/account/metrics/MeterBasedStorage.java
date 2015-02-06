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
package com.codenvy.api.account.metrics;

import com.codenvy.api.core.ServerException;

import java.util.Map;

/**
 * Provide access to metrics used by account.
 *
 * @author Sergii Kabashniuk
 */
public interface MeterBasedStorage {
    /**
     * Record memory usage metric.
     *
     * @param metric
     *         - memory usage metric
     * @return - tracker. It allow to inform about the usage and stopping usage of memory.
     */
    UsageInformer createMemoryUsedRecord(MemoryUsedMetric metric) throws ServerException;


    /**
     * Get total consumed GB*h of account by given period.
     *
     * @param accountId
     *         - Given accountId
     * @param from
     *         - starting of period
     * @param until
     *         -
     *         end of period
     * @return - total number of consumed  GB*h
     */
    Double getMemoryUsed(String accountId, long from, long until) throws ServerException;

    /**
     * Get total consumed GB*h of account by given period group by workspace id's
     *
     * @param accountId
     *         - Given accountId
     * @param from
     *         - starting of period
     * @param until
     *         -
     *         end of period
     * @return - total number of consumed  GB*h group by workspace id's
     */
    Map<String, Double> getMemoryUsedReport(String accountId, long from, long until) throws ServerException;
}
