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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.workspace.shared.dto.Workspace;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.List;

/**
 * @author Alexander Reshetnyak
 */
@RolesAllowed(value = {"system/admin", "system/manager"})
public class AccountWorkspaces extends AbstractAccountMetric {

    public AccountWorkspaces() {
        super(MetricType.ACCOUNT_WORKSPACES);
    }

    @Override
    public String getDescription() {
        return "Number of workspaces for account";
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        validateContext(context);
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        AccountMembership accountById = getAccountMembership(accountId);
        List<Workspace> workspaces = getWorkspaces(accountById.getId());

        return new LongValueData(workspaces.size());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }
}
