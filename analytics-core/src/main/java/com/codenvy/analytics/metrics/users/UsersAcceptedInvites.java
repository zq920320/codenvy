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
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersAcceptedInvites extends CalculatedMetric implements Expandable {

    public UsersAcceptedInvites() {
        super(MetricType.USERS_ACCEPTED_INVITES,
              new MetricType[]{MetricType.USERS_ADDED_TO_WORKSPACES_USING_INVITATION});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        return basedMetric[0].getValue(context);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of users who accepted invites";
    }
    
    @Override
    public String getExpandedValueField() {
        return USER;
    }
    
    @Override
    public ListValueData getExpandedValue(Context context) throws IOException {
        return ((Expandable) basedMetric[0]).getExpandedValue(context);
    }
}
