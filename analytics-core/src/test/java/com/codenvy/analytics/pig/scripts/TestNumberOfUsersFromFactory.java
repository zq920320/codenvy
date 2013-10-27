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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.PigServer;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.testng.Assert.*;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestNumberOfUsersFromFactory extends BaseTest {

    private Map<String, String> params = new HashMap<>();

    @BeforeClass
    public void setUp() throws IOException {
        List<Event> events = new ArrayList<>();
        events.add(Event.Builder.createUserAddedToWsEvent("", "", "", "tmp-1", "Anonymoususer_1", "website")
                        .withDate("2013-01-01").build());

        events.add(Event.Builder.createUserChangedNameEvent("user1@gmail.com", "user3@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_2", "user5@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_1", "user4@gmail.com").withDate("2013-01-01")
                        .build());
        events.add(Event.Builder.createUserChangedNameEvent("Anonymoususer_2", "user5@gmail.com").withDate("2013-01-01")
                        .build());

        events.add(Event.Builder.createUserCreatedEvent("user-id1", "user3@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id2", "user4@gmail.com").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id3", "Anonymoususer_1").withDate("2013-01-01").build());
        events.add(Event.Builder.createUserCreatedEvent("user-id4", "Anonymoususer_2").withDate("2013-01-01").build());


        File log = LogGenerator.generateLog(events);

        Parameters.LOG.put(params, log.getAbsolutePath());
        Parameters.FROM_DATE.put(params, "20130101");
        Parameters.TO_DATE.put(params, "20130101");
        Parameters.USER.put(params, Parameters.USER_TYPES.ANY.name());
        Parameters.WS.put(params, Parameters.WS_TYPES.ANY.name());
        Parameters.METRIC.put(params, "fake");
    }

    @Test
    public void testExecute() throws Exception {
        Iterator<Tuple> iterator = PigServer.executeAndReturn(ScriptType.NUMBER_OF_USERS_FROM_FACTORY, params);

        assertTrue(iterator.hasNext());

        Tuple tuple = iterator.next();
        assertEquals(tuple.get(1).toString(), "(date,20130101)");
        assertEquals(tuple.get(2).toString(), "(value,1)");

        assertFalse(iterator.hasNext());
    }
}
