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
package com.codenvy.analytics.util;


import com.codenvy.analytics.BaseTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestUtils extends BaseTest {

    private static final String SYSTEM_USER = "user@codenvy.com";
    private static final String SOME_USER   = "user@gmail.com";


    @Test(dataProvider = "systemLoginProvider")
    public void testIsSystemUser(String login, boolean validated) throws Exception {
        assertEquals(validated, utils.isSystemUser(login));
    }

    @DataProvider(name = "systemLoginProvider")
    public Object[][] systemLoginProvider() {
        return new Object[][]{{SYSTEM_USER, true},
                              {SOME_USER, false},
                              {"codenvy.com", false}};
    }
}
