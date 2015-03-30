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
package com.codenvy.analytics.metrics;


import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.im.IMDownloadsList;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getSummaryValue;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsMap;
import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestIMDownloadStatistics extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(new Event.Builder().withDate("2013-01-01")
                                      .withParam("EVENT", "im-artifact-downloaded")
                                      .withParam("USER", "user1")
                                      .withParam("ARTIFACT", "codenvy")
                                      .withParam("VERSION", "3.5.0").build());
        events.add(new Event.Builder().withDate("2013-01-01")
                                      .withParam("EVENT", "im-artifact-downloaded")
                                      .withParam("USER", "user2")
                                      .withParam("ARTIFACT", "codenvy")
                                      .withParam("VERSION", "3.4.0").build());
        events.add(new Event.Builder().withDate("2013-01-01")
                                      .withParam("EVENT", "im-artifact-downloaded")
                                      .withParam("USER", "user3")
                                      .withParam("ARTIFACT", "install-codenvy")
                                      .withParam("VERSION", "3.1.0").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.IM_DOWNLOADS).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    @Test
    public void testIMDownloadStatisticsList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.IM_DOWNLOADS_LIST);
        ListValueData l = getAsList(metric, Context.EMPTY);
        assertEquals(l.size(), 3);

        Map<String, Map<String, ValueData>> m = listToMap(l, "user");
        Map<String, ValueData> data = m.get("user1");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.5.0"));

        data = m.get("user2");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.4.0"));

        data = m.get("user3");
        assertEquals(data.get("artifact"), StringValueData.valueOf("install-codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.1.0"));
    }

    @Test
    public void testIMDownloadStatistics() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.IM_DOWNLOADS);
        LongValueData l = getAsLong(metric, Context.EMPTY);
        assertEquals(l, LongValueData.valueOf(3));
    }

    @Test
    public void testIMDownloadStatisticsSummary() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.IM_DOWNLOADS_LIST);
        ListValueData l = getSummaryValue(metric, Context.EMPTY);

        assertEquals(l.size(), 1);

        Map<String, ValueData> m = treatAsMap(l.getAll().get(0));
        assertEquals(m.get(IMDownloadsList.CODENVY_BINARIES), LongValueData.valueOf(2));
        assertEquals(m.get(IMDownloadsList.INSTALL_SINGLE_SCRIPT), LongValueData.valueOf(1));
    }
}


