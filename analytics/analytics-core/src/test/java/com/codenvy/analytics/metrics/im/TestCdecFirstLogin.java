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
package com.codenvy.analytics.metrics.im;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.datamodel.ValueDataUtil.getAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/** @author Dmytro Nochevnov */
public class TestCdecFirstLogin extends BaseTest {
    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createCdecFirstLoginEvent(UID1, dateToMillis("2013-01-01 10:01:00"), "88.88.88.88")
                                .withDate("2013-01-01", "10:01:00").build());
        events.add(Event.Builder.createCdecFirstLoginEvent(UID2, dateToMillis("2013-01-01 10:02:00"), "88.88.88.89")
                                .withDate("2013-01-01", "10:02:00").build());
        events.add(Event.Builder.createCdecFirstLoginEvent(UID3, dateToMillis("2013-01-01 10:03:00"), "88.88.88.90")
                                .withDate("2013-01-01", "10:03:00").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.CDEC_FIRST_LOGIN).getParamsAsMap());
        pigServer.execute(ScriptType.EVENTS, builder.build());
    }

    @Test
    public void testIMInstallStatisticsList() throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.CDEC_FIRST_LOGIN);
        assertEquals(getAsLong(metric, Context.EMPTY).getAsLong(), 3);

        ListValueData expandedValue = (ListValueData)((Expandable)metric).getExpandedValue(Context.EMPTY);

        List<ValueData> l = treatAsList(expandedValue);
        assertEquals(l.size(), 3);
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID1)));
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID2)));
        assertTrue(l.contains(MapValueData.valueOf("user=" + UID3)));
    }
}
