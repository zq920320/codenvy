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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.BaseTest;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.model.Account;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/** @author Anatoliy Bazko */
public class TestActiveSubscribersRecipientGroup extends BaseTest {

    private Map<String, String>             context;
    private Account                         account;
    private ActiveSubscribersRecipientGroup group;

    @BeforeTest
    public void prepare() throws Exception {
        AccountManager accountManager = mock(AccountManager.class);
        context = Utils.newContext();
        account = mock(Account.class);

        ParameterConfiguration paramConf = new ParameterConfiguration();
        paramConf.setKey(ActiveSubscribersRecipientGroup.TARIFF_PLAN);
        paramConf.setValue(ActiveSubscribersRecipientGroup.TARIFF_MANAGED_FACTORY);
        List<ParameterConfiguration> params = Arrays.asList(paramConf);

        group = new ActiveSubscribersRecipientGroup(params, accountManager);
    }

    @Test
    public void testIsNotActiveSubscriberWhenTariffPlanIsWrong() throws Exception {
        doReturn("Premium").when(account).getAttribute(ActiveSubscribersRecipientGroup.TARIFF_PLAN);

        assertFalse(group.isActiveSubscriber(account, context));
    }

    @Test
    public void testIsNotActiveSubscriberWhenPeriodIsWrong() throws Exception {
        doReturn(ActiveSubscribersRecipientGroup.TARIFF_MANAGED_FACTORY).when(account)
                .getAttribute(ActiveSubscribersRecipientGroup.TARIFF_PLAN);
        doReturn("" + Utils.parseDate("20131009").getTimeInMillis()).when(account)
                .getAttribute(ActiveSubscribersRecipientGroup.TARIFF_START_TIME);
        doReturn("" + Utils.parseDate("20131009").getTimeInMillis()).when(account)
                .getAttribute(ActiveSubscribersRecipientGroup.TARIFF_END_TIME);
        Parameters.TO_DATE.put(context, "20131010");

        assertFalse(group.isActiveSubscriber(account, context));
    }

    @Test
    public void testIsActiveSubscriber() throws Exception {
        doReturn(ActiveSubscribersRecipientGroup.TARIFF_MANAGED_FACTORY).when(account)
                .getAttribute(ActiveSubscribersRecipientGroup.TARIFF_PLAN);
        doReturn("" + Utils.parseDate("20131010").getTimeInMillis()).when(account)
                .getAttribute(ActiveSubscribersRecipientGroup.TARIFF_START_TIME);
        doReturn("" + Utils.parseDate("20131010").getTimeInMillis()).when(account)
                .getAttribute(ActiveSubscribersRecipientGroup.TARIFF_END_TIME);
        Parameters.TO_DATE.put(context, "20131010");

        assertTrue(group.isActiveSubscriber(account, context));
    }
}
