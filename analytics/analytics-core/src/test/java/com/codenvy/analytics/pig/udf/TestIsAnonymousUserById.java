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

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Reshetnyak
 */
public class TestIsAnonymousUserById extends BaseTest {

    IsAnonymousUserById isAnonymousUserById;

    @BeforeClass
    public void prepare() throws Exception {
        isAnonymousUserById = new IsAnonymousUserById();

        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserCreatedEvent("uid1", "user1@gmail.com", "user1@gmail.com")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());
        events.add(Event.Builder.createUserCreatedEvent("uid2", "AnonymousUser_1", "AnonymousUser_1")
                                .withDate("2013-01-01").withTime("10:00:00,000").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.LOG, log.getAbsolutePath());

        builder.putAll(scriptsManager.getScript(ScriptType.USERS_PROFILES, MetricType.USERS_PROFILES_LIST).getParamsAsMap());
        pigServer.execute(ScriptType.USERS_PROFILES, builder.build());
    }

    @Test(dataProvider = "provider")
    public void shouldCutPTypeParam(String userId, boolean result) throws Exception {
        Tuple tuple = makeTuple(userId);
        assertEquals(result, isAnonymousUserById.exec(tuple).booleanValue());
    }

    private Tuple makeTuple(String userId) {
        Tuple tuple = tupleFactory.newTuple();
        tuple.append(userId);

        return tuple;
    }

    @DataProvider(name = "provider")
    public Object[][] createData() {
        return new Object[][]{
                {"uid1", false},
                {"uid2", true},
                {"user2@gmail.com", false},
                {"AnonymousUser_2", true},
        };
    }
}