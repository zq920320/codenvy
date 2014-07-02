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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.datamodel.ValueData;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractAlias extends CalculatedMetric {

    public AbstractAlias(MetricType metricType, MetricType basedMetric) {
        super(metricType, new MetricType[]{basedMetric});
    }

    @Override
    public String getDescription() {
        return basedMetric[0].getDescription();
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        return basedMetric[0].getValue(context);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return basedMetric[0].getValueDataClass();
    }
}
