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
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.RequiredFilter;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.workspace.shared.dto.MemberDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@RolesAllowed(value = {"system/admin", "system/manager"})
@RequiredFilter(MetricFilter.ACCOUNT_ID)
public class AccountUsersStatisticsList extends AbstractAccountMetric {

    public AccountUsersStatisticsList() {
        super(MetricType.ACCOUNT_USERS_STATISTICS_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        List<WorkspaceDescriptor> workspaces = getWorkspacesByAccountId(accountId);
        ImmutableList<MemberDescriptor> members = getMembers(workspaces);

        List<ValueData> l = FluentIterable.from(members).transform(new Function<MemberDescriptor, ValueData>() {
            @Override
            public ValueData apply(MemberDescriptor memberDescriptor) {
                String userId = memberDescriptor.getUserId();
                String workspaceId = memberDescriptor.getWorkspaceReference().getId();

                Map<String, ValueData> m = new HashMap<>(7);
                m.put("workspace", StringValueData.valueOf(workspaceId));
                m.put("user", StringValueData.valueOf(userId));
                m.put("roles", StringValueData.valueOf(memberDescriptor.getRoles().toString()));

                Context.Builder builder = new Context.Builder();
                builder.put(MetricFilter.USER_ID, userId);
                builder.put(MetricFilter.WS_ID, workspaceId);
                Context statContext = builder.build();

                m.put("builds", fetchStatistics(MetricType.BUILDS, statContext));
                m.put("runs", fetchStatistics(MetricType.RUNS, statContext));
                m.put("gigabyte_ram_hours", fetchStatistics(MetricType.TASKS_GIGABYTE_RAM_HOURS, statContext));

                ListValueData listVD = (ListValueData)fetchStatistics(MetricType.USERS_STATISTICS_LIST, statContext);
                m.put("time", ValueDataUtil.getFirstValue(listVD, "time", LongValueData.DEFAULT));

                return MapValueData.valueOf(m);
            }
        }).toList();

        return ListValueData.valueOf(l);
    }

    protected ValueData fetchStatistics(MetricType metricType, Context statContext) {
        Metric metric = MetricFactory.getMetric(metricType);
        try {
            return metric.getValue(statContext);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "User statistics";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}