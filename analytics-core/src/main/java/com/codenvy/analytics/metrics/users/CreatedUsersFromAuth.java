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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class CreatedUsersFromAuth extends CalculatedMetric {

    public CreatedUsersFromAuth() {
        super(MetricType.CREATED_USERS_FROM_AUTH, new MetricType[]{MetricType.CREATED_USERS,
                                                                   MetricType.CREATED_USERS_FROM_FACTORY});
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        LongValueData created = (LongValueData)basedMetric[0].getValue(context);
        LongValueData createdFromFactory = (LongValueData)basedMetric[1].getValue(context);

        return new LongValueData(created.getAsLong() - createdFromFactory.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of created users using authentication form or oAuth";
    }
}
