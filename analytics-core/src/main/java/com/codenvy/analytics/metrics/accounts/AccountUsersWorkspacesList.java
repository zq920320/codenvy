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
import com.codenvy.api.core.util.Pair;
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
public class AccountUsersWorkspacesList extends AbstractAccountMetric {

    public static final String PATH_USER_BY_ID = "/user/{userId}";
    public static final String PARAM_USER_ID   = "{userId}";
    public static final String ROLE            = "role";

    public AccountUsersWorkspacesList() {
        super(MetricType.ACCOUNT_USERS_WORKSPACES_LIST);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        validateContext(context);
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        AccountMembership accountById = getAccountMembership(accountId);
        List<Workspace> workspaces = getWorkspaces(accountById.getId());

        List<ValueData> list = new ArrayList<>();

        for (Workspace workspace : workspaces) {

            String pathWorkspaceMembers = AccountWorkspacesList.PATH_WORKSPACES_MEMBERS
                    .replace(AccountWorkspacesList.PARAM_WORKSPACE_ID, workspace.getId());

            List<Member> members;
            try {
                members = getMembers(workspace.getId());
            } catch (IOException e) {
                // Members data isn't available. So, skip it.
                continue;
            }

            for (Member member : members) {
                for (String role : member.getRoles()) {
                    Map<String, ValueData> map = new HashMap<>();

                    map.put(USER, new StringValueData(
                            getUserEmail(member.getUserId())));
                    map.put(WS, new StringValueData(
                            workspace.getId().equals(member.getWorkspaceId()) ? workspace.getName()
                                                                              : member.getWorkspaceId()));
                    map.put(ROLE, new StringValueData(role));

                    list.add(new MapValueData(map));
                }
            }
        }

        return new ListValueData(list);
    }

    private String getUserEmail(String userId) throws IOException {

        String pathUserById = PATH_USER_BY_ID.replace(PARAM_USER_ID, userId);
        User user =
                httpMetricTransport.getResource(User.class,
                                                "GET",
                                                pathUserById,
                                                null,
                                                new Pair[0]);

        if (user == null) {
            throw new IOException("Can not get User by userId : " + userId);
        }

        return user.getEmail();
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public String getDescription() {
        return "Users data by workspaces in account";
    }
}
