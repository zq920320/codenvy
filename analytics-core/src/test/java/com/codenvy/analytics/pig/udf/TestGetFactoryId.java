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
package com.codenvy.analytics.pig.udf;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.ScriptType;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.codenvy.analytics.pig.scripts.util.Event.Builder.createFactoryUrlAcceptedEvent;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestGetFactoryId extends BaseTest {

    private GetFactoryId getFactoryId;

    @BeforeClass
    public void prepare() throws Exception {
        getFactoryId = new GetFactoryId();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("uid1", "anonymousUser_00001", "[anonymousUser_00001]").withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "anonymousUser_00002", "[anonymousUser_00002]").withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid3", "user1@gmail.com", "[user1@gmail.com]").withDate("2013-01-01").withTime("10:00:00,000").build());

        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid1", "tmp-1", "anonymousUser_00001").withDate("2013-01-01").withTime("10:00:01,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid2", "tmp-2", "user1@gmail.com").withDate("2013-01-01").withTime("10:00:02,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid3", "tmp-3", "user1@gmail.com").withDate("2013-01-01").withTime("10:00:03,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid4", "tmp-4", "anonymousUser_00002").withDate("2013-01-01").withTime("10:00:04,000").build());

        // use case with same workspace id, but different time. Shuald be return latest factory id (fid004)
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid5", "tmp-5", "anonymousUser_00002").withDate("2013-01-01").withTime("11:00:04,000").build());
        events.add(Event.Builder.createWorkspaceCreatedEvent("wsid5", "tmp-6", "anonymousUser_00002").withDate("2013-01-01").withTime("12:00:04,000").build());

        events.add(createFactoryUrlAcceptedEvent("tmp-1", "factory1", "", "", "").withDate("2013-01-01").withTime("13:00:00").build());
        events.add(createFactoryUrlAcceptedEvent("tmp-2", "factory2", "", "", "").withDate("2013-01-01").withTime("13:00:00").build());
        events.add(createFactoryUrlAcceptedEvent("tmp-3", "https://test.com/factory?id=fid001", "", "", "").withDate("2013-01-01").withTime("13:00:00").build());
        events.add(createFactoryUrlAcceptedEvent("tmp-4", "https://test.com/factory?id=fid002", "", "", "").withDate("2013-01-01").withTime("13:00:00").build());

        events.add(createFactoryUrlAcceptedEvent("tmp-5", "https://test.com/factory?id=fid003", "", "", "").withDate("2013-01-01").withTime("11:00:10").build());
        events.add(createFactoryUrlAcceptedEvent("tmp-6", "https://test.com/factory?id=fid004", "", "", "").withDate("2013-01-01").withTime("12:00:10").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.WORKSPACES_PROFILES, MetricType.WORKSPACES_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.WORKSPACES_PROFILES, builder.build());

        builder.putAll(scriptsManager.getScript(ScriptType.ACCEPTED_FACTORIES, MetricType.FACTORIES_ACCEPTED_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.ACCEPTED_FACTORIES, builder.build());
    }

    @Test(dataProvider = "provider")
    public void shouldCutPTypeParam(String wsid, String factoryId) throws Exception {
        Tuple tuple = makeTuple(wsid);
        assertEquals(factoryId, getFactoryId.exec(tuple));
    }

    private Tuple makeTuple(String url) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(url);

        return tuple;
    }

    @DataProvider(name = "provider")
    public Object[][] createData() {
        return new Object[][]{
                {"wsid1", null},
                {"wsid2", null},
                {"wsid3", "fid001"},
                {"wsid4", "fid002"},
                {"wsid5", "fid004"},
        };
    }
}
