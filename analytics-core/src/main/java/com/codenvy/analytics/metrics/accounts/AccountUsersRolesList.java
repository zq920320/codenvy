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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.RequiredFilter;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.user.shared.dto.Member;
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
public class AccountUsersRolesList extends AbstractAccountMetric {

    public AccountUsersRolesList() {
        super(MetricType.ACCOUNT_USERS_ROLES_LIST);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        AccountMembership accountById = getAccountMembership(context);

        User currentUser = getCurrentUser();
        String currentUserId = currentUser.getId();

        List<ValueData> list2Return = new ArrayList<>();
        for (Workspace workspace : getWorkspaces(accountById.getId())) {
            String rolesCurrentUserInWorkspace = getUserRoleInWorkspace(currentUserId, workspace.getId());
            boolean hasAdminRoles = rolesCurrentUserInWorkspace.contains(ROLE_WORKSPACE_ADMIN.toLowerCase());

            for (Member member : getMembers(workspace.getId())) {
                if (hasAdminRoles || member.getUserId().equals(currentUserId)) {
                    Map<String, ValueData> m = new HashMap<>();
                    m.put(ROLES, StringValueData.valueOf(member.getRoles().toString()));
                    m.put(USER, StringValueData.valueOf(member.getUserId()));
                    m.put(WS, StringValueData.valueOf(workspace.getId()));

                    list2Return.add(new MapValueData(m));
                }
            }
        }

        list2Return = sort(list2Return, context);
        list2Return = keepSpecificPage(list2Return, context);
        return new ListValueData(list2Return);
    }

    @Override
    public String getDescription() {
        return "Users roles in workspaces";
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
