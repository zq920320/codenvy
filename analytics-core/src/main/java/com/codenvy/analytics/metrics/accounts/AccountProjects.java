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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;

import org.eclipse.che.api.account.shared.dto.MemberDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;

import java.io.IOException;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
public class AccountProjects extends AbstractAccountMetric {
    public AccountProjects() {
        super(MetricType.ACCOUNT_PROJECTS);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        long projects = 0;

        for (MemberDescriptor accountDesc : getMemberDescriptors(context)) {
            List<WorkspaceDescriptor> workspacesDesc = getWorkspacesByAccountId(accountDesc.getAccountReference().getId());
            for (WorkspaceDescriptor workspace : workspacesDesc) {
                projects += getProjects(workspace.getId()).size();
            }
        }

        return LongValueData.valueOf(projects);
    }

    private List<MemberDescriptor> getMemberDescriptors(Context context) throws IOException {
        if (context.exists(Parameters.USER_PRINCIPAL_ROLE) && context.getAsString(Parameters.USER_PRINCIPAL_ROLE).startsWith("system")) {
            return getMemberDescriptorsByUserId((String)context.get(MetricFilter.USER));
        }
        return getMemberDescriptorsCurrentUser();
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of projects in users account.";
    }
}
