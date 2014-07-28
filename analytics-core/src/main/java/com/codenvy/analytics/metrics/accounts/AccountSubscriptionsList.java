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
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.datamodel.*;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.RequiredFilter;
import com.codenvy.api.account.shared.dto.MemberDescriptor;
import com.codenvy.api.account.shared.dto.SubscriptionDescriptor;

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
@RequiredFilter(MetricFilter.ACCOUNT_ID)
public class AccountSubscriptionsList extends AbstractAccountMetric {

    public AccountSubscriptionsList() {
        super(MetricType.ACCOUNT_SUBSCRIPTIONS_LIST);
    }

    @Override
    public String getDescription() {
        return "Subscriptions data for account";
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        MemberDescriptor accountMembership = getAccountMembership(context);
        List<SubscriptionDescriptor> subscriptions = getSubscriptions(accountMembership.getUserId());

        List<ValueData> list2Return = new ArrayList<>();

        for (SubscriptionDescriptor subscription : subscriptions) {
            Map<String, ValueData> m = new HashMap<>();
            m.put(SUBSCRIPTION_SERVICE_ID, StringValueData.valueOf(subscription.getServiceId()));
            m.put(SUBSCRIPTION_START_DATE, LongValueData.valueOf(subscription.getStartDate()));
            m.put(SUBSCRIPTION_END_DATE, LongValueData.valueOf(subscription.getEndDate()));
            m.put(SUBSCRIPTION_PROPERTIES, StringValueData.valueOf(subscription.getProperties().toString()));

            list2Return.add(new MapValueData(m));
        }

        return new ListValueData(list2Return);
    }
}