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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.SetValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;

/**
 * @author Dmytro Nochevnov
 */
public class SingupValidationEmailSend extends CalculatedMetric {

    public SingupValidationEmailSend() {
        super(MetricType.SINGUP_VALIDATION_EMAIL_SEND, new MetricType[]{MetricType.SINGUP_VALIDATION_EMAIL_SEND_SET});
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        SetValueData sendEmail = ValueDataUtil.getAsSet(basedMetric[0], context);

        return new LongValueData(sendEmail.size());
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }


    @Override
    public String getDescription() {
        return "Number of users which was sent validation email";
    }
}
