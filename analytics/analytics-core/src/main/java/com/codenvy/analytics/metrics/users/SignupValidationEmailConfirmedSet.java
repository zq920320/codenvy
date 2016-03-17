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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.Utils;
import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.AbstractMetric;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.codenvy.analytics.Utils.toArray;

/**
 * @author Alexander Reshetnyak
 */
public class SignupValidationEmailConfirmedSet extends CalculatedMetric {

    public SignupValidationEmailConfirmedSet() {
        super(MetricType.SIGNUP_VALIDATION_EMAIL_CONFIRMED_SET, new MetricType[]{MetricType.SIGNUP_VALIDATION_EMAIL_SEND_SET,
                                                                                 MetricType.USERS_PROFILES_LIST});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        SetValueData sendEmail = ValueDataUtil.getAsSet(basedMetric[0], context);

        Context.Builder usersProfilesContextBuilder = new Context.Builder(context);
        usersProfilesContextBuilder.put(MetricFilter.ALIASES, Utils.getFilterAsString(sendEmail.getAll()));
        ListValueData usersCreatedList = ValueDataUtil.getAsList(basedMetric[1],usersProfilesContextBuilder.build());

        Set<ValueData> confirmedEmail = new LinkedHashSet<>();
        for (ValueData user : usersCreatedList.getAll()) {
            confirmedEmail.add(StringValueData.valueOf(toArray(((MapValueData)user).getAll().get(AbstractMetric.ALIASES))[0]));
        }

        return new SetValueData(confirmedEmail);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return SetValueData.class;
    }

    @Override
    public String getDescription() {
        return "Set of users which confirmed singup validation email";
    }

}
