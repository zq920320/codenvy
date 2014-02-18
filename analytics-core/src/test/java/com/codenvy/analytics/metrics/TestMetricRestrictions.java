/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.LongValueData;

import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestMetricRestrictions extends BaseTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowExceptionIfFilterMissed() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_USED);

        Map<String, String> context = Utils.newContext();
        metric.getValue(context);
    }

    @Test
    public void shouldReturnValueIfFilterExists() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.FACTORY_USED);

        Map<String, String> context = Utils.newContext();
        MetricFilter.FACTORY.put(context, "some value");

        assertEquals(metric.getValue(context), LongValueData.valueOf(0));
    }
}
