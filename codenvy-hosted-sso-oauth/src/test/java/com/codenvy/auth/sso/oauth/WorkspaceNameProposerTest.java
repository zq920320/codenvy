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
package com.codenvy.auth.sso.oauth;


import com.codenvy.api.core.ApiException;
import com.codenvy.api.core.NotFoundException;
import com.codenvy.api.workspace.server.dao.Workspace;
import com.codenvy.api.workspace.server.dao.WorkspaceDao;
import com.codenvy.dto.server.DtoFactory;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Listeners(value = {MockitoTestNGListener.class})
public class WorkspaceNameProposerTest {
    private static final String DEFAULT_WS_NAME   = "wsname";
    private static final String DEFAULT_DELIMITER = "-";

    @Mock
    private WorkspaceDao workspaceDao;

    @InjectMocks
    private WorkspaceNameProposer proposer;

    @Test
    public void shouldNotChangeWsNameIfItIsValidAndNotRegistered() throws ApiException {
        doThrow(NotFoundException.class).when(workspaceDao).getByName(anyString());

        assertEquals(proposer.propose(DEFAULT_WS_NAME), DEFAULT_WS_NAME);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfArgumentIsNull() throws ApiException {
        proposer.propose(null);
    }

    @Test(dataProvider = "notNormalizedNamesProvider")
    public void shouldNormalizeWsName(String origin, String expected) throws ApiException {
        doThrow(NotFoundException.class).when(workspaceDao).getByName(anyString());

        assertEquals(proposer.propose(origin), expected);
    }

    @DataProvider(name = "notNormalizedNamesProvider")
    public String[][] dataProvider() {
        return new String[][]{{"!@#%$ )[}aaa", "aaa"},
                              {"aaa****a", "aaa----a"},
                              {"!@#$%a@#$%b#$%^d^&**", "a----b----d----"},
                              {"!@$^0_.-!-._0!@#", "0_.---._0---"},
                              {"morethan20charactersemail", "morethan20characters"}};
    }

    @Test
    public void shouldAddHashIfWsDoesNotExistButNameIsShorterThan3Symbols() throws ApiException {
        when(workspaceDao.getByName(anyString())).thenReturn(null);

        assertTrue(isOriginStringWithHash(proposer.propose("w"), DEFAULT_DELIMITER, "w"));
    }

    @Test
    public void shouldAddHashWithOutDashIfNormalizedNameIsEmpty()
            throws  ApiException {
        when(workspaceDao.getByName(anyString())).thenReturn(null);

        assertTrue(isOriginStringWithHash(proposer.propose("#####"), "", ""));
    }

    @Test
    public void shouldProposeNewWsNameWithHashIfOriginIsRegistered() throws ApiException {
        when(workspaceDao.getByName(DEFAULT_WS_NAME))
                .thenReturn(new Workspace().withName(DEFAULT_WS_NAME));

        assertTrue(isOriginStringWithHash(proposer.propose(DEFAULT_WS_NAME), DEFAULT_DELIMITER, DEFAULT_WS_NAME));
    }

    private boolean isOriginStringWithHash(String wsName, String delimiter, String origin) {
        return Pattern.compile("^" + origin + delimiter + "[a-z0-9]{6}$").matcher(wsName).matches();
    }
}
