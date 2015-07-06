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
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.RequiredFilter;
import com.codenvy.api.subscription.shared.dto.SubscriptionDescriptor;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@RolesAllowed(value = {"system/admin", "system/manager"})
@RequiredFilter(MetricFilter.ACCOUNT_ID)
public class AccountSubscriptionsList extends AbstractAccountMetric {

    public AccountSubscriptionsList() {
        super(MetricType.ACCOUNT_SUBSCRIPTIONS_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        List<SubscriptionDescriptor> subscriptions = getSubscriptionsByAccountId(accountId);
        List<ValueData> l = FluentIterable.from(subscriptions).transform(new Function<SubscriptionDescriptor, ValueData>() {
            @Override
            public ValueData apply(SubscriptionDescriptor subscription) {
                Map<String, ValueData> m = new HashMap<>(4);
                m.put(SUBSCRIPTION_SERVICE_ID, StringValueData.valueOf(subscription.getServiceId()));
                m.put(SUBSCRIPTION_START_DATE, StringValueData.valueOf(nullToEmpty(subscription.getStartDate())));
                m.put(SUBSCRIPTION_END_DATE, StringValueData.valueOf(nullToEmpty(subscription.getEndDate())));
                m.put(SUBSCRIPTION_PROPERTIES, StringValueData.valueOf(subscription.getProperties().toString()));
                return MapValueData.valueOf(m);
            }
        }).toList();

        return ListValueData.valueOf(l);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Subscriptions";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}