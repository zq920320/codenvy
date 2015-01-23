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
package com.codenvy.analytics.metrics.users;

import com.codenvy.analytics.datamodel.DoubleValueData;
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
public class UsersAcceptedInvitesPercent extends CalculatedMetric implements Expandable {

    public UsersAcceptedInvitesPercent() {
        super(MetricType.USERS_ACCEPTED_INVITES_PERCENT, new MetricType[]{MetricType.USER_INVITE,
                                                                          MetricType.USERS_ACCEPTED_INVITES});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData total = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData number = ValueDataUtil.getAsLong(basedMetric[1], context);

        return new DoubleValueData(100D * number.getAsLong() / total.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    @Override
    public String getDescription() {
        return "The percent of users who accepted invitations";
    }


    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        return ((Expandable)basedMetric[1]).getExpandedValue(context);
    }

    @Override
    public String getExpandedField() {
        return ((Expandable)basedMetric[1]).getExpandedField();
    }
}
