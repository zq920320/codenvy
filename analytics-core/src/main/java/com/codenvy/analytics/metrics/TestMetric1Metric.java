package com.codenvy.analytics.metrics;/*
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


import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ScriptBasedMetric;
import com.codenvy.analytics.scripts.ScriptType;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestMetric1Metric extends ScriptBasedMetric {

    public TestMetric1Metric() {
        super(MetricType.TEST_METRIC_1);
    }

    @Override
    protected ScriptType getScriptType() {
        return ScriptType.USERS_BY_COMPANY;
    }
}
