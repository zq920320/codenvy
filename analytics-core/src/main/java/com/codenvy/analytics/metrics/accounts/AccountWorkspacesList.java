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
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class AccountWorkspacesList extends AbstractAccountMetric {

    public static final String PATH_USER = "/user";

    public static final String WORKSPACE_NAME      = "workspace_name";
    public static final String WORKSPACE_TEMPORARY = "workspace_temporary";
    public static final String WORKSPACE_ROLES     = "workspace_roles";

    public AccountWorkspacesList() {
        super(MetricType.ACCOUNT_WORKSPACES_LIST);
    }

    @Override
    public String getDescription() {
        return "Workspaces data for account";
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        validateContext(context);
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        AccountMembership accountById = getAccountMembership(accountId);

        User user = httpMetricTransport.getResource(User.class,
                                                    "GET",
                                                    PATH_USER);
        String currentUserId = user.getId();

        List<Workspace> workspaces = getWorkspaces(accountById.getId());

        List<ValueData> list = new ArrayList<>();
        for (Workspace workspace : workspaces) {

            Map<String, ValueData> map = new HashMap<>();

            map.put(Account.ACCOUNT_ID, new StringValueData(workspace.getAccountId()));
            map.put(WORKSPACE_NAME, new StringValueData(workspace.getName()));
            map.put(WORKSPACE_TEMPORARY, new StringValueData(String.valueOf(workspace.isTemporary())));

            // Get roles for current user in this workspace
            String rolesCurrentUser = getUserRole(currentUserId, workspace.getId());

            map.put(WORKSPACE_ROLES,
                    rolesCurrentUser != null ? new StringValueData(rolesCurrentUser) : StringValueData.DEFAULT);

            list.add(new MapValueData(map));
        }

        return new ListValueData(list);
    }

    private String getUserRole(String userId, String workspaceId) throws IOException {
        List<Member> members = getMembers(workspaceId);

        String rolesCurrentUser = null;
        for (Member member : members) {
            if (member.getUserId().equals(userId)) {
                rolesCurrentUser = member.getRoles().toString();
            }
        }
        return rolesCurrentUser;
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}