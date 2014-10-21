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
        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com","[user1@gmail.com]").withDate("2013-02-10").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "user2@gmail.com", "[user2@gmail.com]").withDate("2013-02-10").build());
        events.add(Event.Builder.createUserCreatedEvent("uid3", "anonymoususer_edjkx4", "[]").withDate("2013-02-10").build());
        events.add(Event.Builder.createUserCreatedEvent("uid4", "AnonymousUser_lnmyzh", "[]").withDate("2013-02-10").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "ws1", "user1@gmail.com").withDate("2013-02-10").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid3", "ws2", "anonymoususer_edjkx4").withDate("2013-02-10").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "tmp-22rct0cq0rh8vs", "user2@gmail.com").withDate("2013-02-10").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid4", "tmp-p42qbfzn6iz9gn", "AnonymousUser_lnmyzh").withDate("2013-02-10").build());
        
        events.add(Event.Builder.createProjectCreatedEvent("uid1", "wsid1", "p", "t").withDate("2013-02-10").build());
        events.add(Event.Builder.createProjectCreatedEvent("uid3", "wsid3", "p", "t").withDate("2013-02-10").build());
        events.add(Event.Builder.createProjectCreatedEvent("uid2", "wsid2", "p", "t").withDate("2013-02-10").build());
        events.add(Event.Builder.createProjectCreatedEvent("uid4", "wsid4", "p", "t").withDate("2013-02-10").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130210");
        builder.put(Parameters.TO_DATE, "20130210");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.EVENTS, MetricType.PROJECTS).getParamsAsMap());
        builder.put(MetricFilter.WS, Parameters.WS_TYPES.ANY.toString());
        pigServer.execute(ScriptType.EVENTS, builder.build());
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
        return new Object[][]{{"wsid1", null, 1},
                              {"wsid2", null, 1},
                              {null, "uid1", 1},
                              {null, "uid3", 1},
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
