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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.RequiredFilter;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.shared.dto.Workspace;

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
public class AccountWorkspacesList extends AbstractAccountMetric {

    public AccountWorkspacesList() {
        super(MetricType.ACCOUNT_WORKSPACES_LIST);
    }

    @Override
    public String getDescription() {
        return "Workspaces data for account";
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        AccountMembership accountById = getAccountMembership(context);

        User user = getCurrentUser();
        String currentUserId = user.getId();

        List<ValueData> list2Return = new ArrayList<>();
        for (Workspace workspace : getWorkspaces(accountById.getId())) {
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


    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}