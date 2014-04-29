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
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.account.shared.dto.Subscription;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed(value = {"system/admin", "system/manager"})
public class AccountSubscriptionsList extends AbstractAccountMetric {

    public static final String PATH_ACCOUNT_SUBSCRIPTIONS = "/account/{id}/subscriptions";
    public static final String PARAM_ID                   = "{id}";
    public static final String SERVICE_ID                 = "service_id";
    public static final String START_DATE                 = "start_date";
    public static final String END_DATE                   = "end_date";
    public static final String PROPERTIES                 = "properties";


    public AccountSubscriptionsList() {
        super(MetricType.ACCOUNT_SUBSCRIPTIONS_LIST);
    }

    @Override
    public String getDescription() {
        return "Subscriptions data for account";
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        validateContext(context);
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        AccountMembership accountById = getAccountMembership(accountId);
        List<Subscription> subscriptions = getSubscriptions(accountById.getId());

        List<ValueData> list = new ArrayList<>();

        for (Subscription subscription : subscriptions) {

            Map<String, ValueData> map = new HashMap<>();

            map.put(SERVICE_ID, new StringValueData(subscription.getServiceId()));
            map.put(START_DATE, new LongValueData(subscription.getStartDate()));
            map.put(END_DATE, new LongValueData(subscription.getEndDate()));
            map.put(PROPERTIES, new StringValueData(subscription.getProperties().toString()));

            list.add(new MapValueData(map));
        }

        return new ListValueData(list);
    }

    private List<Subscription> getSubscriptions(String accountId) throws IOException {
        String pathAccountSubscriptions = PATH_ACCOUNT_SUBSCRIPTIONS.replace(PARAM_ID, accountId);

        return httpMetricTransport.getResources(Subscription.class,
                                                "GET",
                                                pathAccountSubscriptions);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}