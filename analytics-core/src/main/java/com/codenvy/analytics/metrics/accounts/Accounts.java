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
public class Accounts extends AbstractAccountMetric {

    public Accounts() {
        super(MetricType.ACCOUNTS);
    }

    @Override
    public String getDescription() {
        return "Accounts data";
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        List<AccountMembership> accountMemberships = getAccountMemberships();
        Profile profile = getProfile();

        List<ValueData> list = new ArrayList<>();

        for (AccountMembership accountMembership : accountMemberships) {
            Map<String, ValueData> map = new HashMap<>();

            map.put(Account.ACCOUNT_ID, new StringValueData(accountMembership.getId()));
            map.put(Account.ACCOUNT_NAME, new StringValueData(accountMembership.getName()));
            map.put(Account.ACCOUNT_ROLES, new StringValueData(accountMembership.getRoles().toString()));

            map.put(Account.ACCOUNT_PROFILE_EMAIL, getEmail(profile));

            list.add(new MapValueData(map));
        }

        return new ListValueData(list);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
