/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.scripts;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.metrics.MetricParameter;
import com.codenvy.analytics.metrics.MetricParameter.ENTITY_TYPE;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;
import com.codenvy.analytics.scripts.util.Event;
import com.codenvy.analytics.scripts.util.LogGenerator;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TestProductUsageTimeTopDomains extends BaseTest {

    @Test
    public void testExecute() throws Exception {
        List<Event> events = new ArrayList<Event>();

        // 5 min in day
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-10-01")
                                .withTime("20:30:00").build());

        // 10 min, in week
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-09-30")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-09-30")
                                .withTime("20:30:00").build());


        // 15 min, in month
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-09-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-09-15")
                                .withTime("20:30:00").build());

        // 20 min, in 2 months
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-08-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-08-15")
                                .withTime("20:30:00").build());

        // 25 min, in 3 months
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-07-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-07-15")
                                .withTime("20:30:00").build());

        // 30 min, in 1 year
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-05-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2010-05-15")
                                .withTime("20:30:00").build());

        // 35 min, in lifetime
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2009-05-15")
                                .withTime("20:25:00").build());
        events.add(Event.Builder.createProjectBuiltEvent("user1@gmail.com", "ws1", "", "", "").withDate("2009-05-15")
                                .withTime("20:30:00").build());

        FileUtils.deleteDirectory(new File(BASE_DIR, "USERS"));
        FileUtils.deleteDirectory(new File(BASE_DIR, "DOMAINS"));
        FileUtils.deleteDirectory(new File(BASE_DIR, "LOG"));

        File log = LogGenerator.generateLog(events);

        Map<String, String> context = Utils.newContext();
        context.put(MetricParameter.RESULT_DIR.name(), BASE_DIR);
        context.put(MetricParameter.TO_DATE.name(), "20101001");
        execute(ScriptType.PRODUCT_USAGE_TIME_LOG_PREPARATION, log, context);

        context.put(MetricParameter.INTERVAL.name(), "P1D");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_USERS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, context);

        context.put(MetricParameter.INTERVAL.name(), "P7D");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_USERS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, context);

        context.put(MetricParameter.INTERVAL.name(), "P30D");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_USERS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, context);

        context.put(MetricParameter.INTERVAL.name(), "P60D");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_USERS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, context);

        context.put(MetricParameter.INTERVAL.name(), "P90D");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());
        executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_USERS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, context);

        context.put(MetricParameter.INTERVAL.name(), "P365D");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_USERS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, context);

        context.put(MetricParameter.INTERVAL.name(), "P100Y");
        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.USERS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_USERS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        execute(ScriptType.PRODUCT_USAGE_TIME_DOMAINS, log, context);

        context.put(MetricParameter.ENTITY.name(), ENTITY_TYPE.DOMAINS.name());
        context.put(MetricParameter.INTERVAL.name(), "P1D");
        ListListStringValueData value = (ListListStringValueData)executeAndReturnResult(ScriptType.PRODUCT_USAGE_TIME_TOP, log,
                                                                                        context);
        List<ListStringValueData> all = value.getAll();
        ListStringValueData item1 = new ListStringValueData(Arrays.asList("gmail.com", "7", "5", "10", "15", "20", "25", "30", "35"));

        assertEquals(all.size(), 1);
        assertTrue(all.contains(item1));
    }
}
