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
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;

/**
 * @author Dmytro Nochevnov
 */
public abstract class SignupValidationEmailExpandable extends CalculatedMetric implements Expandable {
    public SignupValidationEmailExpandable(MetricType signupValidationEmailConfirmed, MetricType[] metricTypes) {
        super(signupValidationEmailConfirmed, metricTypes);
    }

    @Override public String getExpandedField() {
        return USER;
    }

    @Override public ValueData getExpandedValue(Context context) throws IOException {
        SetValueData setValue = ValueDataUtil.getAsSet(basedMetric[0], context);
        return ValueDataUtil.convertToListOfMapValues(setValue, getExpandedField());
    }
}
