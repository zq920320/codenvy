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
package com.codenvy.analytics.metrics.workspaces;

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ActiveWsSet extends CalculatedMetric {

    public ActiveWsSet() {
        super(MetricType.ACTIVE_WS_SET, new MetricType[]{MetricType.ACTIVE_WORKSPACES_SET});
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        return basedMetric[0].getValue(context);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return basedMetric[0].getValueDataClass();
    }

    @Override
    public String getDescription() {
        return basedMetric[0].getDescription();
    }
}
