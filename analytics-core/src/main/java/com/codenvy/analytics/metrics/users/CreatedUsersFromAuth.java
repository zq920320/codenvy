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

import com.codenvy.analytics.datamodel.ListValueData;
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
public class CreatedUsersFromAuth extends CalculatedMetric implements Expandable{

    public CreatedUsersFromAuth() {
        super(MetricType.CREATED_USERS_FROM_AUTH, new MetricType[]{MetricType.CREATED_USERS,
                                                                   MetricType.CREATED_USERS_FROM_FACTORY});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData created = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData createdFromFactory = ValueDataUtil.getAsLong(basedMetric[1], context);

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
    
    @Override
    public String getExpandedValueField() {
        return USER;
    }
    
    @Override
    public ListValueData getExpandedValue(Context context) throws IOException {
        ListValueData minuendList = ((Expandable) basedMetric[0]).getExpandedValue(context);
        ListValueData subtrahendList = ((Expandable) basedMetric[1]).getExpandedValue(context);
        
        ListValueData result = minuendList.doSubtract(subtrahendList);
        
        return result;
    }
}
