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
import com.codenvy.api.account.shared.dto.AccountMembership;
import com.codenvy.api.user.shared.dto.Profile;

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
public class AccountsList extends AbstractAccountMetric {

    public AccountsList() {
        super(MetricType.ACCOUNTS_LIST);
    }

    @Override
    public String getDescription() {
        return "Accounts data";
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        Profile profile = getProfile();
        List<AccountMembership> accountMemberships = getAccountMemberships();

        List<ValueData> list2Return = new ArrayList<>();
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        for (AccountMembership accountMembership : accountMemberships) {
            if (!context.exists(MetricFilter.ACCOUNT_ID) || accountMembership.getId().equals(accountId)) {

                Map<String, ValueData> m = new HashMap<>();
                m.put(ACCOUNT_ID, StringValueData.valueOf(accountMembership.getId()));
                m.put(ACCOUNT_NAME, StringValueData.valueOf(accountMembership.getName()));
                m.put(ACCOUNT_ROLES, StringValueData.valueOf(accountMembership.getRoles().toString()));
                m.put(ACCOUNT_ATTRIBUTES, StringValueData.valueOf(accountMembership.getAttributes().toString()));
                m.put(ACCOUNT_USER_ID, StringValueData.valueOf(profile.getUserId()));
                m.put(ACCOUNT_PROFILE_EMAIL, getEmail(profile));
                m.put(ACCOUNT_PROFILE_NAME, getFullName(profile));

                list2Return.add(new MapValueData(m));
            }
        }

        return new ListValueData(list2Return);
    }
}
