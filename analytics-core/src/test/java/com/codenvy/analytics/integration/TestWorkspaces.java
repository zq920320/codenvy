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

package com.codenvy.analytics.integration;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestWorkspaces extends BaseTest {

    @Test
    public void testTotalWorkspaces() throws Exception {
        Context.Builder context = new Context.Builder();
        context.put(Parameters.TO_DATE, Parameters.TO_DATE.getDefaultValue());

        ValueData valueData = getValue(MetricType.TOTAL_WORKSPACES, context.build());
        long l = treatAsLong(valueData);

        assertEquals(l, 2);
    }

    @Test
    public void testNewActiveWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.NEW_ACTIVE_WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 3);
    }

    @Test
    public void testReturningActiveWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.RETURNING_ACTIVE_WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 0);
    }

    @Test
    public void testNonActiveWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.NON_ACTIVE_WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 0);
    }

    @Test
    public void testDestroyedWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.DESTROYED_WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 1);
    }

    @Test
    public void testWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 3);
    }

    @Test
    public void testCreatedWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 3);
    }

    @Test
    public void testCreatedWorkspacesSet() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_WORKSPACES_SET);
        Set<ValueData> s = treatAsSet(valueData);

        assertEquals(s.size(), 3);

        Set<String> actualWsNames = new HashSet<>(s.size());
        Set<String> expectedWsNames = ImmutableSet.of("iedexmain2",
                                                      "ws_for_deleting",
                                                      "ws_after_updating");

        for (ValueData vd : s) {
            Context.Builder context = new Context.Builder();
            context.put(MetricFilter._ID, vd.getAsString());

            ValueData wsProfile = getValue(MetricType.WORKSPACES_PROFILES_LIST, context.build());
            Map<String, Map<String, ValueData>> m = listToMap((ListValueData)wsProfile, "ws_name");

            assertEquals(m.size(), 1);

            actualWsNames.add(m.keySet().iterator().next());
        }

        assertEquals(actualWsNames, expectedWsNames);
    }


    @Test
    public void testActiveWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.ACTIVE_WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 3);
    }

    @Test
    public void testActiveWorkspacesSet() throws Exception {
        ValueData valueData = getValue(MetricType.ACTIVE_WORKSPACES_SET);
        Set<ValueData> s = treatAsSet(valueData);

        assertEquals(s.size(), 3);

        Set<String> actualWsNames = new HashSet<>(s.size());
        Set<String> expectedWsNames = ImmutableSet.of("iedexmain2",
                                                      "ws_for_deleting",
                                                      "ws_after_updating");

        for (ValueData vd : s) {
            actualWsNames.add(getWsNameById(vd.getAsString()));
        }

        assertEquals(actualWsNames, expectedWsNames);
    }

    @Test
    public void testWorkspacesProfiles() throws Exception {
        ValueData valueData = getValue(MetricType.WORKSPACES_PROFILES);
        long l = treatAsLong(valueData);

        assertEquals(l, 3);
    }

    @Test
    public void testWorkspacesProfilesList() throws Exception {
        ValueData valueData = getValue(MetricType.WORKSPACES_PROFILES_LIST);

        Map<String, Map<String, ValueData>> m = listToMap((ListValueData)valueData, "ws_name");

        assertEquals(m.size(), 3);
        assertTrue(m.containsKey("ws_after_updating"));
        assertTrue(m.containsKey("iedexmain2"));
        assertTrue(m.containsKey("ws_for_deleting"));

        Map<String, ValueData> wsProfile = m.get("ws_after_updating");
        assertEquals(wsProfile.get("persistent_ws"), StringValueData.valueOf("1"));
        assertNotNull(wsProfile.get("_id"));

        wsProfile = m.get("iedexmain2");
        assertEquals(wsProfile.get("persistent_ws"), StringValueData.valueOf("1"));
        assertNotNull(wsProfile.get("_id"));

        wsProfile = m.get("ws_for_deleting");
        assertEquals(wsProfile.get("persistent_ws"), StringValueData.valueOf("1"));
        assertNotNull(wsProfile.get("_id"));
    }

    @Test
    public void testWorkspacesStatistics() throws Exception {
        ValueData valueData = getValue(MetricType.WORKSPACES_STATISTICS);
        long l = treatAsLong(valueData);

        assertEquals(l, 1);
    }

    @Test
    public void testWorkspacesStatisticsList() throws Exception {
        ValueData valueData = getValue(MetricType.WORKSPACES_STATISTICS_LIST);
        Map<String, Map<String, ValueData>> m = listToMap((ListValueData)valueData, "ws");

        assertEquals(m.size(), 1);

        String wsId = m.keySet().iterator().next();
        assertEquals("iedexmain2", getWsNameById(wsId));

        Map<String, ValueData> wsStat = m.get(wsId);
        assertEquals(wsStat.get("projects"), StringValueData.valueOf("1"));
    }

    protected String getWsNameById(String wsId) throws IOException {
        Context.Builder context = new Context.Builder();
        context.put(MetricFilter._ID, wsId);

        ValueData wsProfile = getValue(MetricType.WORKSPACES_PROFILES_LIST, context.build());
        Map<String, Map<String, ValueData>> m = listToMap((ListValueData)wsProfile, "ws_name");

        assertEquals(m.size(), 1);

        return m.keySet().iterator().next();
    }
}
