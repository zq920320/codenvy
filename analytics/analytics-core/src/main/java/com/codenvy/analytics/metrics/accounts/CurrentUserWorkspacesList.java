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
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.RequiredFilter;

import org.eclipse.che.api.account.shared.dto.MemberDescriptor;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
@InternalMetric
@RequiredFilter(MetricFilter.ACCOUNT_ID)
public class CurrentUserWorkspacesList extends AbstractAccountMetric {

    public CurrentUserWorkspacesList() {
        super(MetricType.CURRENT_USER_WORKSPACES_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Workspaces data for account";
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        MemberDescriptor accountById = getAccountMembership(context);

        UserDescriptor user = getCurrentUser();
        String currentUserId = user.getId();

        List<ValueData> list2Return = new ArrayList<>();
        for (WorkspaceDescriptor workspace : getWorkspacesByAccountId(accountById.getAccountReference().getId())) {
            Map<String, ValueData> m = new HashMap<>();
            m.put(ACCOUNT_ID, new StringValueData(workspace.getAccountId()));
            m.put(WORKSPACE_ID, new StringValueData(workspace.getId()));
            m.put(WORKSPACE_NAME, new StringValueData(workspace.getName()));
            m.put(WORKSPACE_TEMPORARY, new StringValueData(String.valueOf(workspace.isTemporary())));

            String rolesCurrentUser = getUserRoleInWorkspace(currentUserId, workspace.getId());
            m.put(WORKSPACE_ROLES, rolesCurrentUser != null ? StringValueData.valueOf(rolesCurrentUser) : StringValueData.DEFAULT);

            list2Return.add(new MapValueData(m));
        }

        return new ListValueData(list2Return);
    }


    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}