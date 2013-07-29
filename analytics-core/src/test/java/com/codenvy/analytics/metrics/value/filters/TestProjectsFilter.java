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


package com.codenvy.analytics.metrics.value.filters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ListStringValueData;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestProjectsFilter {

    private ProjectsFilter      filter;
    private ListStringValueData item1;
    private ListStringValueData item2;
    private ListStringValueData item3;
    private ListStringValueData item4;

    @BeforeMethod
    public void setUp() {
        item1 = new ListStringValueData(Arrays.asList("ws1", "user1", "project1", "type1", "paas1"));
        item2 = new ListStringValueData(Arrays.asList("ws1", "user2", "project1", "type2", "paas2"));
        item3 = new ListStringValueData(Arrays.asList("ws2", "user2", "project2", "type3", "paas3"));
        item4 = new ListStringValueData(Arrays.asList("ws1", "user1", "project1", "type1", "paas1"));

        ListListStringValueData value = new ListListStringValueData(Arrays.asList(item1, item2, item3, item4));
        filter = new ProjectsFilter(value);
    }

    @Test
    public void testGetIndex() {
        List<ListStringValueData> all = filter.getUniqueProjects().getAll();

        assertEquals(all.size(), 3);
        assertTrue(all.contains(item1));
        assertTrue(all.contains(item2));
        assertTrue(all.contains(item3));
    }
}
