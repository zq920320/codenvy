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

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;

import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsList;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsLong;
import static com.codenvy.analytics.datamodel.ValueDataUtil.treatAsSet;
import static org.junit.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestWorkspaces extends BaseTest {

//    WORKSPACES_PROFILES,
//    WORKSPACES_PROFILES_LIST,
//    CREATED_UNIQUE_WORKSPACES,
//    CREATED_WORKSPACES_SET,
//    NEW_ACTIVE_WORKSPACES,
//    RETURNING_ACTIVE_WORKSPACES,
//    ACTIVE_WORKSPACES_SET,
//    WORKSPACES_STATISTICS_LIST,
//    WORKSPACES_STATISTICS,

    @Test
    public void testCreatedWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_WORKSPACES);
        long l = treatAsLong(valueData);
    }

    @Test
    public void testTotalWorkspaces() throws Exception {
        Context.Builder context = new Context.Builder();
        context.put(Parameters.TO_DATE, Parameters.TO_DATE.getDefaultValue());

        ValueData valueData = getValue(MetricType.TOTAL_WORKSPACES, context.build());
        long l = treatAsLong(valueData);
    }

    @Test
    public void testNonActiveWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.NON_ACTIVE_WORKSPACES);
        long l = treatAsLong(valueData);

        assertEquals(l, 0);
    }

    @Test
    public void testActiveWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.ACTIVE_WORKSPACES);
        long l = treatAsLong(valueData);
    }

    @Test
    public void testDestroyedWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.DESTROYED_WORKSPACES);
        long l = treatAsLong(valueData);
    }

    @Test
    public void testWorkspaces() throws Exception {
        ValueData valueData = getValue(MetricType.WORKSPACES);
        long l = treatAsLong(valueData);
    }

    @Test
    public void testCreatedWorkspacesSet() throws Exception {
        ValueData valueData = getValue(MetricType.CREATED_WORKSPACES_SET);
        Set<ValueData> s = treatAsSet(valueData);
    }

    @Test
    public void testWorkspacesProfilesList() throws Exception {
        ValueData valueData = getValue(MetricType.WORKSPACES_PROFILES_LIST);
        List<ValueData> l = treatAsList(valueData);
    }
}
