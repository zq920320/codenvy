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

import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.SetStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:kuleshov@codenvy.com">Dmitry Kuleshov</a> */
public class FactoryUrlAcceptedNumber extends CalculatedMetric {

    FactoryUrlAcceptedNumber() {
        super(MetricType.FACTORY_URL_ACCEPTED_NUMBER, MetricType.FACTORY_URL_ACCEPTED);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return new LinkedHashSet<>(Arrays.asList(new MetricParameter[]{MetricParameter.FROM_DATE,
                                                                       MetricParameter.TO_DATE}));
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        SetStringValueData setStringValueData = (SetStringValueData)super.getValue(context);
        return new LongValueData(setStringValueData.size());
    }

    @Override
    public String getDescription() {
        return "The Number of active temporary workspaces";
    }
}
