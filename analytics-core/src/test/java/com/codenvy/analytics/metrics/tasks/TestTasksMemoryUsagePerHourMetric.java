/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.tasks;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsDouble;
import static java.lang.Math.round;
import static org.testng.AssertJUnit.assertEquals;

/** @author dnochevnov@codenvy.com */
public class TestTasksMemoryUsagePerHourMetric extends BaseTest {

    @BeforeClass
    public void setUp() throws Exception {
        new TestData().prepareData();
    }

    @Test
    public void testGetValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_MEMORY_USAGE_PER_HOUR);
        DoubleValueData d = getAsDouble(metric, Context.EMPTY);
        assertEquals(round(d.getAsDouble() * 10000), 542);
    }

    @Test
    public void testGetExpandedValue() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.TASKS_MEMORY_USAGE_PER_HOUR);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        Map<String, Map<String, ValueData>> m = listToMap(expandedValue, AbstractMetric.PROJECT_ID);
        Assert.assertEquals(m.size(), 2);
        assertEquals("{user/ws/project1={project_id=user/ws/project1}, user/ws/project2={project_id=user/ws/project2}}", m.toString());
    }

}
