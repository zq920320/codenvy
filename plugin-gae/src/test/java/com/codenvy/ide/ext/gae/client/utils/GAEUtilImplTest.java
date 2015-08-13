/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.client.utils;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.ide.api.app.CurrentProject;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.codenvy.ide.ext.gae.client.utils.GAEUtilImpl.APP_ENGINE_ADMIN_SCOPE;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_JAVA_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PHP_ID;
import static com.codenvy.ide.ext.gae.shared.GAEConstants.GAE_PYTHON_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(DataProviderRunner.class)
public class GAEUtilImplTest {

    private static final String SOME_TEXT = "someText";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private CurrentProject project;
    @Mock
    private OAuthToken     token;
    @InjectMocks
    private GAEUtilImpl    gaeUtil;

    @DataProvider
    public static Object[][] checkIsCorrectAppId() {
        return new Object[][]{
                {true, "sometext"},
                {true, "some-text"},
                {true, "some-text-538"},
                {false, "someText"},
                {false, "a"},
                {false, "some*"},
                {false, "some/"},
                {false, "text-"},
                {false, "some("},
                {false, "3some"},
                {false, "SomeText"}
        };
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @UseDataProvider("checkIsCorrectAppId")
    public void applicationIdCorrectionShouldBeChecked(boolean isCorrect, String appId) throws Exception {
        assertThat(gaeUtil.isCorrectAppId(appId), is(isCorrect));
    }

    @Test
    public void trueValueShouldBeReturnedWhenCurrentProjectIsMaven() throws Exception {
        when(project.getProjectDescription().getType()).thenReturn(GAE_JAVA_ID);

        assertThat(gaeUtil.isAppEngineProject(project), is(true));
    }

    @Test
    public void trueValueShouldBeReturnedWhenCurrentProjectIsPython() throws Exception {
        when(project.getProjectDescription().getType()).thenReturn(GAE_PYTHON_ID);

        assertThat(gaeUtil.isAppEngineProject(project), is(true));
    }

    @Test
    public void trueValueShouldBeReturnedWhenCurrentProjectIsGaePhp() throws Exception {
        when(project.getProjectDescription().getType()).thenReturn(GAE_PHP_ID);

        assertThat(gaeUtil.isAppEngineProject(project), is(true));
    }

    @Test
    public void falseValueShouldBeReturnedWhenCurrentProjectIsUndefined() throws Exception {
        when(project.getProjectDescription().getType()).thenReturn(SOME_TEXT);

        assertThat(gaeUtil.isAppEngineProject(project), is(false));
    }

    @Test
    public void authenticatedShouldBeFalseWhenTokenIsNull() throws Exception {
        assertThat(gaeUtil.isAuthenticatedInAppEngine(null), is(false));
    }

    @Test
    public void authenticatedShouldBeFalseWhenStringTokenIsNull() throws Exception {
        when(token.getToken()).thenReturn(null);

        assertThat(gaeUtil.isAuthenticatedInAppEngine(token), is(false));
    }

    @Test
    public void authenticatedShouldBeFalseWhenStringTokenIsEmpty() throws Exception {
        when(token.getToken()).thenReturn("");

        assertThat(gaeUtil.isAuthenticatedInAppEngine(token), is(false));
    }

    @Test
    public void authenticatedShouldBeFalseWhenTokenScopeIsNull() throws Exception {
        when(token.getScope()).thenReturn(null);

        assertThat(gaeUtil.isAuthenticatedInAppEngine(token), is(false));
    }

    @Test
    public void authenticatedShouldBeFalseWhenTokenScopeIsEmpty() throws Exception {
        when(token.getScope()).thenReturn("");

        assertThat(gaeUtil.isAuthenticatedInAppEngine(token), is(false));
    }

    @Test
    public void authenticatedShouldBeFalseWhenTokenScopeNotContainsAdminScope() throws Exception {
        when(token.getScope()).thenReturn(SOME_TEXT);

        assertThat(gaeUtil.isAuthenticatedInAppEngine(token), is(false));
    }

    @Test
    public void authenticatedShouldBeTrue() throws Exception {
        when(token.getToken()).thenReturn(SOME_TEXT);
        when(token.getScope()).thenReturn(APP_ENGINE_ADMIN_SCOPE);

        assertThat(gaeUtil.isAuthenticatedInAppEngine(token), is(true));
    }
}