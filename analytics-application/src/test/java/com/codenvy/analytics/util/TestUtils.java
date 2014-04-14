/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.util;


import com.codenvy.analytics.BaseTest;
import com.codenvy.api.analytics.dto.MetricInfoDTO;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Anatoliy Bazko
 */
public class TestUtils extends BaseTest {

    private static final String SYSTEM_USER = "user@codenvy.com";
    private static final String SOME_USER   = "user@gmail.com";

    private Principal       principal;
    private SecurityContext securityContext;
    private MetricInfoDTO   metricInfoDTO;

    @BeforeMethod
    public void setUp() {
        principal = mock(Principal.class);
        securityContext = mock(SecurityContext.class);
        metricInfoDTO = mock(MetricInfoDTO.class);
    }

    @Test
    public void restrictAccessIfPrincipalIsNull() throws Exception {
        when(securityContext.getUserPrincipal()).thenReturn(null);
        when(metricInfoDTO.getRolesAllowed()).thenReturn(Arrays.asList("user"));

        assertEquals(false, Utils.isRolesAllowed(metricInfoDTO, securityContext));
    }

    @Test
    public void restrictAccessForSystemUserIfRolesAreEmpty() throws Exception {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(SYSTEM_USER);

        assertEquals(false, Utils.isRolesAllowed(metricInfoDTO, securityContext));
    }

    @Test
    public void restrictAccessForUserIfRolesAreEmpty() throws Exception {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(SOME_USER);

        assertEquals(false, Utils.isRolesAllowed(metricInfoDTO, securityContext));
    }

    @Test
    public void allowAccessForSystemUserIfUserRoles() throws Exception {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(SYSTEM_USER);
        when(metricInfoDTO.getRolesAllowed()).thenReturn(Arrays.asList(SOME_USER));

        assertEquals(true, Utils.isRolesAllowed(metricInfoDTO, securityContext));
    }

    @Test
    public void allowAccessForUserIfAnyRoles() throws Exception {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn(SOME_USER);
        when(metricInfoDTO.getRolesAllowed()).thenReturn(Arrays.asList("any"));

        assertEquals(true, Utils.isRolesAllowed(metricInfoDTO, securityContext));
    }

    @Test
    public void allowAccessIfRolesSuit() throws Exception {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(securityContext.isUserInRole("user")).thenReturn(true);
        when(principal.getName()).thenReturn(SOME_USER);
        when(metricInfoDTO.getRolesAllowed()).thenReturn(Arrays.asList("user"));

        assertEquals(true, Utils.isRolesAllowed(metricInfoDTO, securityContext));
    }

    @Test
    public void restrictAccessIfRolesNotSuit() throws Exception {
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(securityContext.isUserInRole("user")).thenReturn(false);
        when(principal.getName()).thenReturn(SOME_USER);
        when(metricInfoDTO.getRolesAllowed()).thenReturn(Arrays.asList("user"));

        assertEquals(false, Utils.isRolesAllowed(metricInfoDTO, securityContext));
    }

    @Test(dataProvider = "systemLoginProvider")
    public void testIsSystemUser(String login, boolean validated) throws Exception {
        assertEquals(validated, Utils.isSystemUser(login));
    }

    @DataProvider(name = "systemLoginProvider")
    public Object[][] systemLoginProvider() {
        return new Object[][]{{SYSTEM_USER, true},
                              {SOME_USER, false},
                              {"codenvy.com", false}};
    }
}
