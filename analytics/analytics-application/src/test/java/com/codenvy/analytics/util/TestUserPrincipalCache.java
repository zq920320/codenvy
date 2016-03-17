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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestUserPrincipalCache extends BaseTest {

    private static final String USER = "user";

    private Principal principal;

    @BeforeMethod
    public void setUp() {
        principal = mock(Principal.class);
        when(principal.getName()).thenReturn(USER);
        cache.clear();
    }

    @Test
    public void testPut() throws Exception {
        cache.put(principal, new UserPrincipalCache.UserContext(null, Collections.<String>emptySet(), Collections.<String>emptySet()));
        assertEquals(cache.size(), 1);
    }

    @Test
    public void testGet() throws Exception {
        HashSet<String> allowedUses = new HashSet<>(Arrays.asList("user"));
        HashSet<String> allowedWorkspaces = new HashSet<>(Arrays.asList("ws"));

        cache.put(principal, new UserPrincipalCache.UserContext(null, allowedUses, allowedWorkspaces));
        assertTrue(cache.exist(principal));

        UserPrincipalCache.UserContext userContext = cache.get(principal);
        assertEquals(allowedUses, userContext.getAllowedUsers());
        assertEquals(allowedWorkspaces, userContext.getAllowedWorkspaces());
    }

    @Test
    public void testExpireFist() throws Exception {
        cache.put(principal, new UserPrincipalCache.UserContext(null, Collections.<String>emptySet(), Collections.<String>emptySet()));
        Thread.sleep(5000);

        assertTrue(cache.exist(principal));

        for (int i = 0; i < UserPrincipalCache.MAX_ENTRIES; i++) {
            Principal mockPrincipal = mock(Principal.class);
            when(mockPrincipal.getName()).thenReturn("user" + i);

            cache.put(mockPrincipal, new UserPrincipalCache.UserContext(null, Collections.<String>emptySet(), Collections.<String>emptySet()));
        }

        Thread.sleep(2 * 60 * 1000);
        assertFalse(cache.exist(principal));
        assertEquals(UserPrincipalCache.MAX_ENTRIES, cache.size());
    }
}
