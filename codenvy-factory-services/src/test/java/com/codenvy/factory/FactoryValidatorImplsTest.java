/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.factory;

import com.codenvy.api.account.server.dao.AccountDao;
import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.dto.Factory;
import com.codenvy.api.user.server.dao.UserDao;
import com.codenvy.api.user.server.dao.UserProfileDao;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

@Listeners(value = {MockitoTestNGListener.class})
public class FactoryValidatorImplsTest {

    @Mock
    private AccountDao accountDao;

    @Mock
    private UserDao userDao;

    @Mock
    UserProfileDao profileDao;

    @Mock
    Factory factoryUrl;

    @InjectMocks
    private FactoryUrlAcceptValidatorImpl acceptValidator;

    @InjectMocks
    private FactoryUrlCreateValidatorImpl createValidator;


    @Test
    public void shouldCallAllMethodsOnSave() throws FactoryUrlException {

        FactoryUrlCreateValidatorImpl spy = spy(createValidator);
        doNothing().when(spy).validateVcs(any(Factory.class));
        doNothing().when(spy).validateOrgid(any(Factory.class));
        doNothing().when(spy).validateTrackedFactoryAndParams(any(Factory.class));
        doNothing().when(spy).validateProjectName(any(Factory.class));

        //main invoke
        spy.validateOnCreate(factoryUrl);

        verify(spy, atLeastOnce()).validateVcs(any(Factory.class));
        verify(spy, atLeastOnce()).validateOrgid(any(Factory.class));
        verify(spy, atLeastOnce()).validateTrackedFactoryAndParams(any(Factory.class));
        verify(spy, atLeastOnce()).validateProjectName(any(Factory.class));
    }

    @Test
    public void shouldCallAllMethodsOnAcceptNonEncoded() throws FactoryUrlException {

        FactoryUrlAcceptValidatorImpl spy = spy(acceptValidator);
        doNothing().when(spy).validateVcs(any(Factory.class));
        doNothing().when(spy).validateOrgid(any(Factory.class));
        doNothing().when(spy).validateTrackedFactoryAndParams(any(Factory.class));
        doNothing().when(spy).validateProjectName(any(Factory.class));

        //main invoke
        spy.validateOnAccept(factoryUrl, false);

        verify(spy, atLeastOnce()).validateVcs(any(Factory.class));
        verify(spy, atLeastOnce()).validateOrgid(any(Factory.class));
        verify(spy, atLeastOnce()).validateTrackedFactoryAndParams(any(Factory.class));
        verify(spy, atLeastOnce()).validateProjectName(any(Factory.class));
    }

    @Test
    public void shouldCallGivenMethodOnAcceptEncoded() throws FactoryUrlException {

        FactoryUrlAcceptValidatorImpl spy = spy(acceptValidator);
        doThrow(RuntimeException.class).when(spy).validateVcs(any(Factory.class));
        doThrow(RuntimeException.class).when(spy).validateOrgid(any(Factory.class));
        doThrow(RuntimeException.class).when(spy).validateProjectName(any(Factory.class));
        doNothing().when(spy).validateTrackedFactoryAndParams(any(Factory.class));

        //main invoke
        spy.validateOnAccept(factoryUrl, true);

        verify(spy, atLeastOnce()).validateTrackedFactoryAndParams(any(Factory.class));
    }



}
