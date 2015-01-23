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

import com.codenvy.analytics.Injector;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.persistent.MongoDataLoader;
import com.codenvy.api.account.shared.dto.AccountDescriptor;
import com.codenvy.api.account.shared.dto.MemberDescriptor;
import com.codenvy.api.account.shared.dto.SubscriptionDescriptor;
import com.codenvy.api.user.shared.dto.ProfileDescriptor;
import com.codenvy.api.user.shared.dto.UserDescriptor;
import com.codenvy.api.workspace.shared.dto.WorkspaceDescriptor;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;

/**
 * @author Alexander Reshetnyak
 */
public abstract class AbstractAccountMetric extends AbstractMetric {

    public static final String PATH_ACCOUNT = "/account";
    public static final String PATH_PROFILE = "/profile";

    public static final String PATH_ACCOUNT_BY_ID      = "/account/{accountId}";
    public static final String PATH_ACCOUNT_WORKSPACES = "/workspace/find/account?id={accountId}";
    public static final String PARAM_ACCOUNT_ID        = "{accountId}";
    public static final String PATH_WORKSPACES_MEMBERS = "/workspace/{workspaceId}/members";
    public static final String PARAM_WORKSPACE_ID      = "{workspaceId}";

    public static final String PATH_ACCOUNT_SUBSCRIPTIONS = "/account/{id}/subscriptions";
    public static final String PARAM_ID                   = "{id}";

    public static final String PATH_USER = "/user";

    public static final String ACCOUNT_ID            = "account_id";
    public static final String ACCOUNT_NAME          = "account_name";
    public static final String ACCOUNT_ROLES         = "account_roles";
    public static final String ACCOUNT_ATTRIBUTES    = "account_attributes";
    public static final String ACCOUNT_USER_ID       = "account_user_id";
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

    public static final String WORKSPACE_ID        = "workspace_id";
    public static final String WORKSPACE_NAME      = "workspace_name";
    public static final String WORKSPACE_TEMPORARY = "workspace_temporary";
    public static final String WORKSPACE_ROLES     = "workspace_roles";

    public static final String ROLE_WORKSPACE_ADMIN     = "workspace/admin";
    public static final String ROLE_WORKSPACE_DEVELOPER = "workspace/developer";
    public static final String ROLE_ACCOUNT_OWNER       = "account/owner";
    public static final String ROLE_ACCOUNT_MEMBER      = "account/member";

    private static final MetricTransport httpMetricTransport;

    static {
        httpMetricTransport = Injector.getInstance(MetricTransport.class);
    }


    public AbstractAccountMetric(MetricType metricType) {
        super(metricType);
    }

    protected List<MemberDescriptor> getAccountMemberships() throws IOException {
        return httpMetricTransport.getResources(MemberDescriptor.class, "GET", PATH_ACCOUNT);
    }

    protected AccountDescriptor getAccountDescriptorById(String accountId) throws IOException {
        return httpMetricTransport.getResource(AccountDescriptor.class, "GET",
                                               PATH_ACCOUNT_BY_ID.replace(PARAM_ACCOUNT_ID, accountId));
    }

    protected MemberDescriptor getAccountMembership(Context context) throws IOException {
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);
        List<MemberDescriptor> accountMemberships = getAccountMemberships();

        for (MemberDescriptor accountMembership : accountMemberships) {
            if (accountMembership.getAccountReference().getId().equals(accountId)) {
                return accountMembership;
            }
        }

        throw new IOException("There is no account with id " + accountId);
    }

    protected List<WorkspaceDescriptor> getWorkspaces(String accountId) throws IOException {
        return httpMetricTransport
                .getResources(WorkspaceDescriptor.class, "GET", PATH_ACCOUNT_WORKSPACES.replace(PARAM_ACCOUNT_ID, accountId));
    }

    protected List<MemberDescriptor> getMembers(String workspaceId) throws IOException {
        String pathWorkspaceMembers = PATH_WORKSPACES_MEMBERS.replace(PARAM_WORKSPACE_ID, workspaceId);
        return httpMetricTransport.getResources(MemberDescriptor.class, "GET", pathWorkspaceMembers);
    }

    protected ProfileDescriptor getProfile() throws IOException {
        return httpMetricTransport.getResource(ProfileDescriptor.class, "GET", PATH_PROFILE);
    }

    protected StringValueData getEmail(ProfileDescriptor profile) {
        Map<String, String> attributes = profile.getAttributes();
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            if (PROFILE_ATTRIBUTE_EMAIL.equalsIgnoreCase(attribute.getKey())) {
                return new StringValueData(attribute.getValue());
            }
        }
        return StringValueData.DEFAULT;
    }

    protected StringValueData getFullName(ProfileDescriptor profile) {
        String firsName = "";
        String lastName = "";

        Map<String, String> attributes = profile.getAttributes();
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            if (PROFILE_ATTRIBUTE_FIRST_NAME.equalsIgnoreCase(attribute.getKey())) {
                firsName = attribute.getValue();
            }
            if (PROFILE_ATTRIBUTE_LAST_NAME.equalsIgnoreCase(attribute.getKey())) {
                lastName = attribute.getValue();
            }
        }
        return new StringValueData(firsName + " " + lastName);
    }

    public static UserDescriptor getCurrentUser() throws IOException {
        return httpMetricTransport.getResource(UserDescriptor.class, "GET", AbstractAccountMetric.PATH_USER);
    }

    protected String getUserRoleInWorkspace(String userId, String workspaceId) throws IOException {
        List<MemberDescriptor> members = getMembers(workspaceId);

        for (MemberDescriptor member : members) {
            if (member.getUserId().equals(userId)) {
                return member.getRoles().toString();
            }
        }

        throw new IOException("There is no member " + userId + " in " + workspaceId);
    }

    protected List<SubscriptionDescriptor> getSubscriptions(String accountId) throws IOException {
        String path = PATH_ACCOUNT_SUBSCRIPTIONS.replace(PARAM_ID, accountId);
        return httpMetricTransport.getResources(SubscriptionDescriptor.class, "GET", path);
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
            final int order = sortCondition.substring(0, 1).equals(MongoDataLoader.ASC_SORT_SIGN) ? 1 : -1;

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
