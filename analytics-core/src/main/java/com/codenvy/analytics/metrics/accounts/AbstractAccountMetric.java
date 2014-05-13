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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.*;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.account.shared.dto.Subscription;
import com.codenvy.api.user.shared.dto.Attribute;
import com.codenvy.api.user.shared.dto.Member;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.user.shared.dto.User;
import com.codenvy.api.workspace.shared.dto.Workspace;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.min;

/**
 * @author Alexander Reshetnyak
 */
public abstract class AbstractAccountMetric extends AbstractMetric {

    public static final String PATH_ACCOUNT = "/account";
    public static final String PATH_PROFILE = "/profile";

    public static final String PATH_ACCOUNT_WORKSPACES = "/workspace/find/account?id={accountId}";
    public static final String PARAM_ACCOUNT_ID        = "{accountId}";
    public static final String PATH_WORKSPACES_MEMBERS = "/workspace/{workspaceId}/members";
    public static final String PARAM_WORKSPACE_ID      = "{workspaceId}";

    public static final String PATH_ACCOUNT_SUBSCRIPTIONS = "/account/{id}/subscriptions";
    public static final String PARAM_ID                   = "{id}";

    public static final String PATH_USER       = "/user";
    public static final String PATH_USER_BY_ID = "/user/{userId}";
    public static final String PARAM_USER_ID   = "{userId}";

    public static final String ACCOUNT_ID            = "account_id";
    public static final String ACCOUNT_NAME          = "account_name";
    public static final String ACCOUNT_ROLES         = "account_roles";
    public static final String ACCOUNT_ATTRIBUTES    = "account_attributes";
    public static final String ACCOUNT_PROFILE_NAME  = "account_profile_name";
    public static final String ACCOUNT_PROFILE_EMAIL = "account_profile_email";

    public static final String PROFILE_ATTRIBUTE_FIRST_NAME = "firstName";
    public static final String PROFILE_ATTRIBUTE_LAST_NAME  = "lastName";
    public static final String PROFILE_ATTRIBUTE_EMAIL      = "email";
    public static final String ROLES                        = "roles";

    public static final String SUBSCRIPTION_START_DATE = "subscription_start_date";
    public static final String SUBSCRIPTION_END_DATE   = "subscription_end_date";
    public static final String SUBSCRIPTION_PROPERTIES = "subscription_properties";
    public static final String SUBSCRIPTION_SERVICE_ID = "subscription_service_id";

    public static final String WORKSPACE_NAME      = "workspace_name";
    public static final String WORKSPACE_TEMPORARY = "workspace_temporary";
    public static final String WORKSPACE_ROLES     = "workspace_roles";

    public static final String ROLE_WORKSPACE_ADMIN     = "workspace/admin";
    public static final String ROLE_WORKSPACE_DEVELOPER = "workspace/developer";
    public static final String ROLE_ACCOUNT_OWNER       = "account/owner";
    public static final String ROLE_ACCOUNT_MEMBER      = "account/member";


    protected final MetricTransport httpMetricTransport;

    public AbstractAccountMetric(MetricType metricType) {
        super(metricType);
        this.httpMetricTransport = Injector.getInstance(MetricTransport.class);
    }

    protected List<AccountMembership> getAccountMemberships() throws IOException {
        return httpMetricTransport.getResources(AccountMembership.class, "GET", AccountsList.PATH_ACCOUNT);
    }

    protected AccountMembership getAccountMembership(Context context) throws IOException {
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);
        List<AccountMembership> accountMemberships = getAccountMemberships();

        for (AccountMembership accountMembership : accountMemberships) {
            if (accountMembership.getId().equals(accountId)) {
                return accountMembership;
            }
        }

        throw new IOException("There is no account with id " + accountId);
    }

    protected List<Workspace> getWorkspaces(String accountId) throws IOException {
        return httpMetricTransport
                .getResources(Workspace.class, "GET", PATH_ACCOUNT_WORKSPACES.replace(PARAM_ACCOUNT_ID, accountId));
    }

    protected List<Member> getMembers(String workspaceId) throws IOException {
        String pathWorkspaceMembers = PATH_WORKSPACES_MEMBERS.replace(PARAM_WORKSPACE_ID, workspaceId);
        return httpMetricTransport.getResources(Member.class, "GET", pathWorkspaceMembers);
    }

    protected Profile getProfile() throws IOException {
        return httpMetricTransport.getResource(Profile.class, "GET", PATH_PROFILE);
    }

    protected Profile getProfile(String id) throws IOException {
        return httpMetricTransport.getResource(Profile.class, "GET", PATH_PROFILE + "/" + id);
    }

    protected StringValueData getEmail(Profile profile) {
        for (Attribute attribute : profile.getAttributes()) {
            if (PROFILE_ATTRIBUTE_EMAIL.equalsIgnoreCase(attribute.getName())) {
                return new StringValueData(attribute.getValue());
            }
        }
        return StringValueData.DEFAULT;
    }

    protected StringValueData getFullName(Profile profile) {
        String firsName = "";
        String lastName = "";

        for (Attribute attribute : profile.getAttributes()) {
            if (PROFILE_ATTRIBUTE_FIRST_NAME.equalsIgnoreCase(attribute.getName())) {
                firsName = attribute.getValue();
            }
            if (PROFILE_ATTRIBUTE_LAST_NAME.equalsIgnoreCase(attribute.getName())) {
                lastName = attribute.getValue();
            }
        }
        return new StringValueData(firsName + " " + lastName);
    }

    protected User getCurrentUser() throws IOException {
        return httpMetricTransport.getResource(User.class, "GET", AbstractAccountMetric.PATH_USER);
    }

    protected String getUserRoleInWorkspace(String userId, String workspaceId) throws IOException {
        List<Member> members = getMembers(workspaceId);

        for (Member member : members) {
            if (member.getUserId().equals(userId)) {
                return member.getRoles().toString();
            }
        }

        throw new IOException("There is no member " + userId + " in " + workspaceId);
    }

    protected String getUserEmail(String userId) throws IOException {
        /* TODO uncomment after update staging
        String pathUserById =
                AbstractAccountMetric.PATH_USER_BY_ID.replace(AbstractAccountMetric.PARAM_USER_ID, userId);
        User user = httpMetricTransport.getResource(User.class, "GET", pathUserById);
        return user == null ? "" : user.getEmail();
        */

        return getEmail(getProfile(userId)).getAsString();
    }

    protected List<Subscription> getSubscriptions(String accountId) throws IOException {
        String path = PATH_ACCOUNT_SUBSCRIPTIONS.replace(PARAM_ID, accountId);
        return httpMetricTransport.getResources(Subscription.class, "GET", path);
    }

    protected List<ValueData> keepSpecificPage(List<ValueData> list, Context context) {
        if (context.exists(Parameters.PAGE) && context.exists(Parameters.PER_PAGE)) {
            long page = context.getAsLong(Parameters.PAGE);
            long perPage = context.getAsLong(Parameters.PER_PAGE);

            int fromIndex;
            if (page <= 0 || perPage <= 0) {
                return Collections.emptyList();
            } else {
                fromIndex = (int)((page - 1) * perPage);
                if (fromIndex > list.size()) {
                    return Collections.emptyList();
                }
            }

            int toIndex = (int)min(page * perPage, list.size());
            return list.subList(fromIndex, toIndex);
        } else {
            return list;
        }
    }

    protected List<ValueData> sort(List<ValueData> list, Context context) {
        if (context.exists(Parameters.SORT)) {
            String sortCondition = context.getAsString(Parameters.SORT);

            final String field = sortCondition.substring(1);
            final int order = sortCondition.substring(0, 1).equals(ReadBasedMetric.ASC_SORT_SIGN) ? 1 : -1;

            Collections.sort(list, new Comparator<ValueData>() {
                @Override
                public int compare(ValueData o1, ValueData o2) {
                    ValueData v1 = ValueDataUtil.treatAsMap(o1).get(field);
                    ValueData v2 = ValueDataUtil.treatAsMap(o2).get(field);

                    return v1 == null || v2 == null ? 0 : order * v1.getAsString().compareTo(v2.getAsString());
                }
            });
        }

        return list;
    }
}
