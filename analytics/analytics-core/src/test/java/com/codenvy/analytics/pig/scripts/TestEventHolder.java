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
package com.codenvy.analytics.pig.scripts;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Injector;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestEventHolder extends BaseTest {

    private EventsHolder eventsHolder;

    @BeforeClass
    public void prepare() throws Exception {
        eventsHolder = Injector.getInstance(EventsHolder.class);
    }

    @Test
    public void testGetParametersValues() throws Exception {
        Map<String, Object> m = eventsHolder.getParametersValues("ide-usage", "SOURCE#source# ACTION#action# PARAMETERS#a=b#");
        assertEquals(m.size(), 3);
        assertEquals(m.get("SOURCE"), "source");
        assertEquals(m.get("ACTION"), "action");
        assertEquals(m.get("a"), "b");
    }

    @Test
    public void testGetParametersValuesEmptyParameters() throws Exception {
        Map<String, Object> m = eventsHolder.getParametersValues("ide-usage", "SOURCE#source# ACTION#action# PARAMETERS##");
        assertEquals(m.size(), 2);
        assertEquals(m.get("SOURCE"), "source");
        assertEquals(m.get("ACTION"), "action");
    }
}
