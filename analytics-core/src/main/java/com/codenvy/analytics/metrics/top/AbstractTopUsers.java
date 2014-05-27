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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters.PassedDaysCount;

/** @author Anatoliy Bazko */
public abstract class AbstractTopUsers extends AbstractTopEntitiesTime {

    public AbstractTopUsers(MetricType metricType, PassedDaysCount passedDaysCount) {
        super(metricType,
              new MetricType[]{MetricType.PRODUCT_USERS_TIME},
              MetricFilter.USER,
              passedDaysCount);
    }

    // for testing purpose
    protected AbstractTopUsers(MetricType metricType, Metric basedMetric, PassedDaysCount passedDaysCount) {
        super(metricType,
              new Metric[]{basedMetric},
              MetricFilter.USER,
              passedDaysCount);
    }


    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return super.getValueDataClass();
    }
}
