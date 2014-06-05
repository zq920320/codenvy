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


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;


/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestFilters extends BaseTest {

    @BeforeClass
    public void init() throws Exception {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createProjectCreatedEvent("user1", "ws1", "", "p", "t").withDate("2013-02-10").build());
        events.add(Event.Builder.createProjectCreatedEvent("anonymoususer_edjkx4", "ws2", "", "p", "t").withDate("2013-02-10").build());
        events.add(Event.Builder.createProjectCreatedEvent("user2", "tmp-22rct0cq0rh8vs", "", "p", "t").withDate("2013-02-10").build());
        events.add(
                Event.Builder.createProjectCreatedEvent("AnonymousUser_lnmyzh", "tmp-p42qbfzn6iz9gn", "", "p", "t").withDate("2013-02-10").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.putAll(scriptsManager.getScript(ScriptType.PROJECTS, MetricType.PROJECTS).getParamsAsMap());
        builder.put(Parameters.LOG, log.getAbsolutePath());
        pigServer.execute(ScriptType.PROJECTS, builder.build());
    }

    @Test(dataProvider = "dataProvider")
    public void test(Object wsFilter, Object userFilter, long result) throws Exception {
        Metric metric = MetricFactory.getMetric(MetricType.CREATED_PROJECTS);

        Context.Builder builder = new Context.Builder();
        if (wsFilter != null) {
            builder.put(MetricFilter.WS, wsFilter);
        }
        if (userFilter != null) {
            builder.put(MetricFilter.USER, userFilter);
        }


        ValueData valueData = metric.getValue(builder.build());
        assertEquals(wsFilter + ":" + userFilter, valueData, LongValueData.valueOf(result));
    }

    @DataProvider(name = "dataProvider")
    public Object[][] parametersFilterProvider() {
        return new Object[][]{{"ws1", null, 1},
                              {"tmp-22rct0cq0rh8vs", null, 1},
                              {null, "user1", 1},
                              {null, "anonymoususer_edjkx4", 1},
                              {Parameters.WS_TYPES.TEMPORARY.toString(), Parameters.USER_TYPES.ANONYMOUS.toString(), 1},
                              {Parameters.WS_TYPES.PERSISTENT.toString(), Parameters.USER_TYPES.REGISTERED.toString(), 1},
                              {Parameters.WS_TYPES.ANY.toString(), Parameters.USER_TYPES.ANY.toString(), 4},
                              {null, null, 4},
                              {null, Parameters.USER_TYPES.ANONYMOUS.toString(), 2},
                              {null, Parameters.USER_TYPES.REGISTERED.toString(), 2},
                              {Parameters.WS_TYPES.PERSISTENT.toString(), null, 2},
                              {Parameters.WS_TYPES.TEMPORARY.toString(), null, 2}};
    }
}
