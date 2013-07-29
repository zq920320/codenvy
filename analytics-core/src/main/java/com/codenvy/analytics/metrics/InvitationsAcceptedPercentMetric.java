/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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

package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.DoubleValueData;
import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class InvitationsAcceptedPercentMetric extends CalculateBasedMetric {

    private final Metric                          invitationSentMetric;
    private final UsersAddedToWorkspaceListMetric userAddedToWsMetric;

    InvitationsAcceptedPercentMetric() {
        super(MetricType.INVITATIONS_ACCEPTED_PERCENT);

        this.invitationSentMetric = MetricFactory.createMetric(MetricType.INVITATIONS_SENT_NUMBER);
        this.userAddedToWsMetric = (UsersAddedToWorkspaceListMetric)MetricFactory.createMetric(MetricType.USERS_ADDED_TO_WORKSPACE_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<MetricParameter> getParams() {
        Set<MetricParameter> params = invitationSentMetric.getParams();
        params.addAll(userAddedToWsMetric.getParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData)userAddedToWsMetric.getValue(context);
        Filter filter = userAddedToWsMetric.createFilter(valueData);

        long total = invitationSentMetric.getValue(context).getAsLong();
        int number = filter.size(MetricFilter.FILTER_USER_ADDED_FROM, UsersAddedToWorkspaceListMetric.INVITE);

        return new DoubleValueData(100D * number / total);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class< ? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }
}
