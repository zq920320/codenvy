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
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;

import org.eclipse.che.api.account.shared.dto.AccountDescriptor;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Reshetnyak
 */
public class AccountProjects extends AbstractAccountMetric {
    public AccountProjects() {
        super(MetricType.ACCOUNT_PROJECTS);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        List<AccountDescriptor> accountsDesc = getAccountDescriptors(context);
        return toValueData(context, accountsDesc);
    }

    protected ValueData toValueData(Context context, List<AccountDescriptor> accountsDesc) throws IOException {
        long projects = 0;

        for (AccountDescriptor accountDesc : accountsDesc) {
            Map<String, ValueData> m = new HashMap<>();

            m.put("account_id", StringValueData.valueOf(accountDesc.getId()));
            m.put("name", StringValueData.valueOf(accountDesc.getName()));

            List<WorkspaceDescriptor> workspacesDesc = getWorkspacesByAccountId(accountDesc.getId());
            m.put("workspaces", LongValueData.valueOf(workspacesDesc.size()));
            for (WorkspaceDescriptor workspace : workspacesDesc) {
                projects += getProjects(workspace.getId()).size();
            }
            m.put("projects", LongValueData.valueOf(projects));
        }

        return LongValueData.valueOf(projects);
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
