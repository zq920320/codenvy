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
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestIMInstallStatistics extends BaseTest {

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createImArtifactInstallStartedEvent("user1", dateToMillis("2013-01-01 10:00:00"), "codenvy", "3.5.0", "88.88.88.88")
                                .withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createImArtifactInstallStartedEvent("user2", dateToMillis("2013-01-01 10:00:00"), "codenvy", "3.5.1", "88.88.88.89")
                                .withDate("2013-01-01", "10:00:00").build());
        events.add(Event.Builder.createImArtifactInstallStartedEvent("user3", dateToMillis("2013-01-01 10:00:00"), "codenvy", "3.5.5", "88.88.88.90")
                                .withDate("2013-01-01", "10:00:00").build());

        events.add(Event.Builder.createImArtifactInstallFinishedSuccessfullyEvent("user1", dateToMillis("2013-01-01 11:00:00"), "codenvy", "3.5.0", "88.88.88.88")
                                .withDate("2013-01-01", "11:00:00").build());
        events.add(Event.Builder.createImArtifactInstallFinishedSuccessfullyEvent("user2", dateToMillis("2013-01-01 11:00:00"), "codenvy", "3.5.1", "88.88.88.89")
                                .withDate("2013-01-01", "11:00:00").build());

        events.add(Event.Builder.createImArtifactInstallFinishedUnSuccessfullyEvent("user3", dateToMillis("2013-01-01 10:30:00"), "codenvy", "3.5.5", "88.88.88.90", "Binaries+to+install+codenvy%3A3.11.9.1%23S+not+found")
                                .withDate("2013-01-01", "10:30:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.IM_INSTALLS_STARTED).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.IM_INSTALLS_FINISHED_SUCCESSFULLY).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS_BY_TYPE, MetricType.IM_INSTALLS_FINISHED_UNSUCCESSFULLY).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS_BY_TYPE, builder.build());
    }

    private long dateToMillis(String date) throws ParseException {
        return fullDateFormat.parse(date).getTime();
    }

    @Test
    public void testIMInstallStatisticsList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.IM_INSTALLS_STARTED);
        assertEquals(getAsLong(metric, Context.EMPTY).getAsLong(), 3);

        metric = MetricFactory.getMetric(MetricType.IM_INSTALLS_STARTED_LIST);
        ListValueData l = getAsList(metric, Context.EMPTY);
        assertEquals(l.size(), 3);

        Map<String, Map<String, ValueData>> m = listToMap(l, "user");
        Map<String, ValueData> data = m.get("user1");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.5.0"));
        assertEquals(data.get("user_ip"), StringValueData.valueOf("88.88.88.88"));

        data = m.get("user2");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.5.1"));
        assertEquals(data.get("user_ip"), StringValueData.valueOf("88.88.88.89"));

        data = m.get("user3");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.5.5"));
        assertEquals(data.get("user_ip"), StringValueData.valueOf("88.88.88.90"));
    }

    @Test
    public void testIMInstallFinishedSuccessfullyStatisticsList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.IM_INSTALLS_FINISHED_SUCCESSFULLY);
        assertEquals(getAsLong(metric, Context.EMPTY).getAsLong(), 2);

        metric = MetricFactory.getMetric(MetricType.IM_INSTALLS_FINISHED_SUCCESSFULLY_LIST);
        ListValueData l = getAsList(metric, Context.EMPTY);
        assertEquals(l.size(), 2);

        Map<String, Map<String, ValueData>> m = listToMap(l, "user");
        Map<String, ValueData> data = m.get("user1");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.5.0"));
        assertEquals(data.get("user_ip"), StringValueData.valueOf("88.88.88.88"));

        data = m.get("user2");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.5.1"));
        assertEquals(data.get("user_ip"), StringValueData.valueOf("88.88.88.89"));
    }

    @Test
    public void testIMInstallFinishedUnSuccessfullyStatisticsList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.IM_INSTALLS_FINISHED_UNSUCCESSFULLY);
        assertEquals(getAsLong(metric, Context.EMPTY).getAsLong(), 1);

        metric = MetricFactory.getMetric(MetricType.IM_INSTALLS_FINISHED_UNSUCCESSFULLY_LIST);
        ListValueData l = getAsList(metric, Context.EMPTY);
        assertEquals(l.size(), 1);

        Map<String, Map<String, ValueData>> m = listToMap(l, "user");
        Map<String, ValueData> data = m.get("user3");
        assertEquals(data.get("artifact"), StringValueData.valueOf("codenvy"));
        assertEquals(data.get("version"), StringValueData.valueOf("3.5.5"));
        assertEquals(data.get("user_ip"), StringValueData.valueOf("88.88.88.90"));
        assertEquals(data.get("error_message"), StringValueData.valueOf("Binaries to install codenvy:3.11.9.1#S not found"));
    }
}
