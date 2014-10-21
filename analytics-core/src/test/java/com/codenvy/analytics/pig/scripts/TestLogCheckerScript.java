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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.testng.Assert.assertEquals;

/** @author Anatoliy Bazko */
public class TestLogCheckerScript extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(
                Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws1", "project1", "type1", "null").withDate("2013-01-01").build());
        events.add(Event.Builder.createApplicationCreatedEvent("user1@gmail.com", "ws1", "project2", "type1", "").withDate("2013-01-01").build());
        events.add(Event.Builder.createProjectCreatedEvent("", "", "project3", "type1").withDate("2013-01-01").build());

        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.LOG, log.getAbsolutePath());

        int count = 0;
        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.LOG_CHECKER, builder.build());
        for (; iterator.hasNext(); iterator.next()) {
            count++;
        }
        assertEquals(count, 3);
    }
}
