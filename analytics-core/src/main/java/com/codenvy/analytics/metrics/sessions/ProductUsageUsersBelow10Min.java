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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class ProductUsageUsersBelow10Min extends CalculatedMetric implements Expandable {

    public ProductUsageUsersBelow10Min() {
        super(MetricType.PRODUCT_USAGE_USERS_BELOW_10_MIN,
              new MetricType[]{MetricType.PRODUCT_USAGE_USERS_TOTAL,
                               MetricType.PRODUCT_USAGE_USERS_BETWEEN_10_AND_60_MIN,
                               MetricType.PRODUCT_USAGE_USERS_BETWEEN_60_AND_300_MIN,
                               MetricType.PRODUCT_USAGE_USERS_ABOVE_300_MIN});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData value1 = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData value2 = ValueDataUtil.getAsLong(basedMetric[1], context);
        LongValueData value3 = ValueDataUtil.getAsLong(basedMetric[2], context);
        LongValueData value4 = ValueDataUtil.getAsLong(basedMetric[3], context);

        return new LongValueData(value1.getAsLong()
                                 - value2.getAsLong()
                                 - value3.getAsLong()
                                 - value4.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of registered users who spent in product less than 10 minutes";
    }

    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        ValueData value1 = ((Expandable)basedMetric[0]).getExpandedValue(context);
        ValueData value2 = ((Expandable)basedMetric[1]).getExpandedValue(context);
        ValueData value3 = ((Expandable)basedMetric[2]).getExpandedValue(context);
        ValueData value4 = ((Expandable)basedMetric[3]).getExpandedValue(context);

        ValueData result = value1;
        result = result.subtract(value2);
        result = result.subtract(value3);
        result = result.subtract(value4);

        return result;
    }

    @Override
    public String getExpandedField() {
        return ((Expandable)basedMetric[0]).getExpandedField();
    }
}
