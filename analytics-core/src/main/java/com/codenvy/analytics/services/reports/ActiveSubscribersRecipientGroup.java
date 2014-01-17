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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.persistent.OrganizationClient;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;
import com.codenvy.organization.client.AccountManager;
import com.codenvy.organization.exception.OrganizationServiceException;
import com.codenvy.organization.model.Account;
import com.codenvy.organization.model.User;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** @author Anatoliy Bazko */
public class ActiveSubscribersRecipientGroup extends AbstractRecipientGroup {
    public static final String TARIFF_PLAN            = "tariff_plan";
    public static final String TARIFF_START_TIME      = "tariff_start_time";
    public static final String TARIFF_END_TIME        = "tariff_end_time";
    public static final String TARIFF_MANAGED_FACTORY = "Managed Factory";

    private final Set<String> tariffPlan;

    private final AccountManager accountManager;

    public ActiveSubscribersRecipientGroup(List<ParameterConfiguration> parameters)
            throws OrganizationServiceException {
        this(parameters, Injector.getInstance(OrganizationClient.class).getAccountManager());
    }

    public ActiveSubscribersRecipientGroup(List<ParameterConfiguration> parameters,
                                           AccountManager accountManager) throws OrganizationServiceException {
        super(parameters);
        this.accountManager = accountManager;
        this.tariffPlan = getParameters(TARIFF_PLAN);
    }

    @Override
    public Set<String> getEmails(Map<String, String> context) throws IOException {
        Set<String> emails = new HashSet<>();

        SetValueData activeOrgId = getActiveOrgId(context);

        for (ValueData valueData : activeOrgId.getAll()) {
            Account account = getAccountById(valueData);

            if (isActiveSubscriber(account, context)) {
                User accountOwner = getAccountOwner(account);
                emails.addAll(accountOwner.getAliases());
            }
        }

        return emails;
    }

    protected SetValueData getActiveOrgId(Map<String, String> context) throws IOException {
        Metric metric = MetricFactory.getMetric(MetricType.ACTIVE_ORG_ID_SET);
        return (SetValueData)metric.getValue(context);
    }

    protected User getAccountOwner(Account account) throws IOException {
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

    protected boolean isActiveSubscriber(Account account, Map<String, String> context) throws IOException {
        if (tariffPlan.contains(account.getAttribute(TARIFF_PLAN))) {
            String startTimeStr = account.getAttribute(TARIFF_START_TIME);
            String endTimeStr = account.getAttribute(TARIFF_END_TIME);

            if (startTimeStr != null && endTimeStr != null) {
                long startTime = Long.parseLong(startTimeStr);
                long endTime = Long.parseLong(endTimeStr);

                Long currTime;
                try {
                    currTime = Utils.getToDate(context).getTimeInMillis();
                } catch (ParseException e) {
                    throw new IOException(e);
                }

                if (startTime <= currTime && currTime <= endTime) {
                    return true;
                }
            }
        }

        return false;
    }
}
