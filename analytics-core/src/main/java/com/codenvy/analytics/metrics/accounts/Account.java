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
import com.codenvy.api.user.shared.dto.Attribute;
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
public class Account extends AbstractAccountMetric {

    public static final String ACCOUNT_ID            = "account_id";
    public static final String ACCOUNT_NAME          = "account_name";
    public static final String ACCOUNT_ROLES         = "account_roles";
    public static final String ACCOUNT_ATTRIBUTES    = "account_attributes";
    public static final String ACCOUNT_PROFILE_NAME  = "account_profile_name";
    public static final String ACCOUNT_PROFILE_EMAIL = "account_profile_email";

    public Account() {
        super(MetricType.ACCOUNT);
    }

    @Override
    public String getDescription() {
        return "Account data";
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        validateContext(context);
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);

        AccountMembership accountById = getAccountMembership(accountId);
        Profile profile = getProfile();

        Map<String, ValueData> map = new HashMap<>();

        map.put(ACCOUNT_ID, new StringValueData(accountById.getId()));
        map.put(ACCOUNT_NAME, new StringValueData(accountById.getName()));
        map.put(ACCOUNT_ROLES, new StringValueData(accountById.getRoles().toString()));
        map.put(ACCOUNT_ATTRIBUTES, new StringValueData(accountById.getAttributes().toString()));

        map.put(ACCOUNT_PROFILE_NAME, getFullName(profile));
        map.put(ACCOUNT_PROFILE_EMAIL, getEmail(profile));

        List<ValueData> list = new ArrayList<>();
        list.add(new MapValueData(map));

        return new ListValueData(list);
    }

    private StringValueData getFullName(Profile profile) {
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

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}
