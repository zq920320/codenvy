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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.pig.scripts.util.Event;
import com.codenvy.analytics.pig.scripts.util.LogGenerator;

import org.apache.pig.data.Tuple;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.*;

import static org.testng.Assert.assertEquals;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestExtractOrgAndAffiliateId extends BaseTest {

    private Context context;

    @BeforeClass
    public void prepare() throws Exception {
        List<Event> events = new ArrayList<>();

        events.add(new Event.Builder().withParam("EVENT", "event1").withParam("ORG-ID", "")
                                      .withParam("AFFILIATE-ID", "").withDate("2013-01-01").build());

        events.add(new Event.Builder().withParam("EVENT", "event2").withParam("ORG-ID", "o")
                                      .withParam("AFFILIATE-ID", "a").withDate("2013-01-01").build());

        events.add(new Event.Builder().withParam("EVENT", "event3").withParam("ORG-ID", "}")
                                      .withParam("AFFILIATE-ID", "a}").withDate("2013-01-01").build());

        events.add(new Event.Builder().withParam("EVENT", "event4").withParam("ORG-ID", "o")
                                      .withParam("FACTORY-URL", "http://www.com?affiliateid=a")
                                      .withParam("AFFILIATE-ID", "").withDate("2013-01-01").build());

        events.add(new Event.Builder().withParam("EVENT", "event5").withParam("ORG-ID", "o")
                                      .withParam("FACTORY-URL", "http://www.com?affiliateid=A&orgid=O")
                                      .withParam("AFFILIATE-ID", "a").withDate("2013-01-01").build());


        File log = LogGenerator.generateLog(events);

        Context.Builder builder = new Context.Builder();
        builder.put(Parameters.FROM_DATE, "20130101");
        builder.put(Parameters.TO_DATE, "20130101");
        builder.put(Parameters.USER, Parameters.USER_TYPES.ANY.name());
        builder.put(Parameters.WS, Parameters.WS_TYPES.ANY.name());
        builder.put(Parameters.STORAGE_TABLE, "fake");
        builder.put(Parameters.LOG, log.getAbsolutePath());
        context = builder.build();
    }

    @Test
    public void testExtractQueryParam() throws Exception {
        Set<String> actual = new HashSet<>();

        Iterator<Tuple> iterator = pigServer.executeAndReturn(ScriptType.TEST_EXTRACT_ORG_AND_AFFILIATE_ID, context);
        while (iterator.hasNext()) {
            actual.add(iterator.next().toString());
        }

        Set<String> expected = new HashSet<>();
        expected.add("(event1,,)");
        expected.add("(event2,o,a)");
        expected.add("(event3,,a)");
        expected.add("(event4,o,a)");
        expected.add("(event5,o,a)");

        assertEquals(actual, expected);
    }
}
