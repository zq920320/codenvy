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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class AbandonedFactorySessions extends CalculatedMetric {

    public AbandonedFactorySessions() {
        super(MetricType.ABANDONED_FACTORY_SESSIONS, new MetricType[]{MetricType.FACTORY_SESSIONS,
                                                                      MetricType.CONVERTED_FACTORY_SESSIONS});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData total = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData conv = ValueDataUtil.getAsLong(basedMetric[1], context);

        return new LongValueData(total.getAsLong() - conv.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }


    @Override
    public String getDescription() {
        return "The number of abandoned sessions in temporary workspaces";
    }
}
