/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.account;

import org.eclipse.che.api.account.server.dao.Account;
import org.eclipse.che.api.account.server.dao.AccountDao;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests of {@link DefaultAccountCreator}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class DefaultAccountCreatorTest {

    @Mock
    private AccountDao accountDao;

    @Test
    public void shouldNotCreateAccountIfItAlreadyExists() throws Exception {
        when(accountDao.exist(DefaultAccountCreator.DEFAULT_ACCOUNT_ID)).thenReturn(true);

        new DefaultAccountCreator(accountDao).createDefaultAccount();

        verify(accountDao, never()).create(any());
    }

    @Test
    public void shouldCreateDefaultAccount() throws Exception {
        new DefaultAccountCreator(accountDao).createDefaultAccount();

        verify(accountDao).create(new Account(DefaultAccountCreator.DEFAULT_ACCOUNT_ID, DefaultAccountCreator.DEFAULT_ACCOUNT_NAME));
    }
}
