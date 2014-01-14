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

import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.User;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author Anatoliy Bazko */
public class ActiveSubscriptionsRecipientGroup extends AbstractRecipientGroup {
    // TODO configuration
    private final AccountManager accountManager;

    public ActiveSubscriptionsRecipientGroup(List<ParameterConfiguration> parameters)
            throws OrganizationServiceException {
        super(parameters);

        accountManager = new AccountManager();
    }

    @Override
    public Set<String> getEmails(Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_ORG_ID_SET);
        SetValueData activeOrgId = (SetValueData)metric.getValue(context);

        Set<String> emails = new HashSet<>();
        for (ValueData valueData : activeOrgId.getAll()) {
            Account account = getAccountById(valueData);

            if (isActiveSubscriber(account)) {
                User accountOwner = getAccountOwner(account);
                Set<String> aliases = accountOwner.getAliases(); // TODO
            }
        }

        return emails;
    }

    private User getAccountOwner(Account account) throws IOException {
        try {
            return accountManager.getAccountOwner(account.getName());
        } catch (OrganizationServiceException e) {
            throw new IOException(e);
        }
    }

    private Account getAccountById(ValueData valueData) throws IOException {
        try {
            return accountManager.getAccountById(valueData.getAsString());
        } catch (OrganizationServiceException e) {
            throw new IOException(e);
        }
    }

    private boolean isActiveSubscriber(Account account) throws IOException {
        return false;
    }
}
