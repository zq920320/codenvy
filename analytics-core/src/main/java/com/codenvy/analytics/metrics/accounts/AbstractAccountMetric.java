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
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.core.util.Pair;
import com.codenvy.api.user.shared.dto.Attribute;
import com.codenvy.api.user.shared.dto.Member;
import com.codenvy.api.user.shared.dto.Profile;
import com.codenvy.api.workspace.shared.dto.Workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
public abstract class AbstractAccountMetric extends AbstractMetric {

    public static final String PATH_ACCOUNT = "/account";
    public static final String PATH_PROFILE = "/profile";

    public static final String PATH_ACCOUNT_WORKSPACES = "/workspace/find/account";
    public static final String PARAM_ID                = "id";

    public static final String PATH_WORKSPACES_MEMBERS = "/workspace/{workspaceId}/members";
    public static final String PARAM_WORKSPACE_ID      = "{workspaceId}";

    public static final String PROFILE_ATTRIBUTE_FIRST_NAME = "firstName";
    public static final String PROFILE_ATTRIBUTE_LAST_NAME  = "lastName";
    public static final String PROFILE_ATTRIBUTE_EMAIL      = "email";

    protected final MetricTransport httpMetricTransport;

    public AbstractAccountMetric(MetricType metricType) {
        super(metricType);
        this.httpMetricTransport = Injector.getInstance(MetricTransport.class);
    }

    protected void validateContext(Context context) throws IOException{
        if (!context.exists(MetricFilter.ACCOUNT_ID)) {
            throw new IOException("The metric filter " + MetricFilter.ACCOUNT_ID + " is not found in context.");
        }
    }

    protected List<AccountMembership> getAccountMemberships() throws IOException {
        return httpMetricTransport.getResources(AccountMembership.class,
                                                "GET",
                                                Account.PATH_ACCOUNT);
    }

    protected AccountMembership getAccountMembership(String accountId)
            throws IOException {
        List<AccountMembership> accountMemberships = getAccountMemberships();

        AccountMembership accountById = null;

        for (AccountMembership accountMembership : accountMemberships) {
            if (accountMembership.getId().equals(accountId)) {
                accountById = accountMembership;
                break;
            }
        }

        if (accountById == null) {
            throw new IOException("The account with id " + accountId + " is not found.");
        }

        return accountById;
    }

    protected List<Workspace> getWorkspaces(String accountId) throws IOException {
        List<Pair<String, String>> pairs = new ArrayList<>();
        pairs.add(new Pair(PARAM_ID, accountId));

        return httpMetricTransport.getResources(Workspace.class,
                                                "GET",
                                                PATH_ACCOUNT_WORKSPACES,
                                                null,
                                                pairs.toArray(new Pair[pairs.size()]));
    }

    protected List<Member> getMembers(String workspaceId) throws IOException {
        String pathWorkspaceMembers = PATH_WORKSPACES_MEMBERS.replace(PARAM_WORKSPACE_ID, workspaceId);

        return httpMetricTransport.getResources(Member.class,
                                                "GET",
                                                pathWorkspaceMembers);
    }

    protected Profile getProfile() throws IOException {
        return httpMetricTransport.getResource(Profile.class,
                                               "GET",
                                               PATH_PROFILE);
    }

    protected StringValueData getEmail(Profile profile) {
        for (Attribute attribute : profile.getAttributes()) {
            if (PROFILE_ATTRIBUTE_EMAIL.equalsIgnoreCase(attribute.getName())) {
                return new StringValueData(attribute.getValue());
            }
        }
        return StringValueData.DEFAULT;
    }
}
