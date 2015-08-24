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

import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Alexander Reshetnyak
 */
public class SingupValidationEmailNotConfirmedSet extends CalculatedMetric {

    public SingupValidationEmailNotConfirmedSet() {
        super(MetricType.SINGUP_VALIDATION_EMAIL_NOT_CONFIRMED_SET, new MetricType[]{MetricType.SINGUP_VALIDATION_EMAIL_SEND_SET,
                                                                                     MetricType.SINGUP_VALIDATION_EMAIL_CONFIRMED_SET});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        SetValueData send = ValueDataUtil.getAsSet(basedMetric[0], context);
        SetValueData confirmed = ValueDataUtil.getAsSet(basedMetric[1], context);

        Set<ValueData> notConfirmedEmail = new LinkedHashSet<>();
        for (ValueData sendEmail : send.getAll()) {
            if (!confirmed.getAll().contains(sendEmail)) {
                notConfirmedEmail.add(sendEmail);
            }
        }

        return new SetValueData(notConfirmedEmail);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return SetValueData.class;
    }

    @Override
    public String getDescription() {
        return "Set of users which didn't confirm singup validation email";
    }
}
