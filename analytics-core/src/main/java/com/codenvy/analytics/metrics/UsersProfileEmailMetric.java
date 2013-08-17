/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
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


package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.StringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UsersProfileEmailMetric extends CalculateBasedMetric {

    private final UsersProfileMetric basedMetric;
    
    UsersProfileEmailMetric() {
        super(MetricType.USER_PROFILE_EMAIL);
        this.basedMetric = (UsersProfileMetric)MetricFactory.createMetric(MetricType.USER_PROFILE);
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ListListStringValueData valueData = (ListListStringValueData) basedMetric.getValue(context);
        return new StringValueData(basedMetric.getEmail(valueData));
    }

    /** {@inheritDoc} */
    @Override
    public Class< ? extends ValueData> getValueDataClass() {
        return StringValueData.class;
    }
}
