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
import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class UsersLoggedInTotal extends CalculatedMetric implements Expandable {

    public UsersLoggedInTotal() {
        super(MetricType.USERS_LOGGED_IN_TOTAL, new MetricType[]{MetricType.USERS_LOGGED_IN_WITH_FORM,
                                                                 MetricType.USERS_LOGGED_IN_WITH_GITHUB,
                                                                 MetricType.USERS_LOGGED_IN_WITH_GOOGLE});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData form = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData github = ValueDataUtil.getAsLong(basedMetric[1], context);
        LongValueData google = ValueDataUtil.getAsLong(basedMetric[2], context);
        return new LongValueData(form.getAsLong() + github.getAsLong() + google.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The total number of login";
    }

    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        ValueData result = ListValueData.DEFAULT;
        for (Metric metric : basedMetric) {
            ValueData expandedValue = ((Expandable)metric).getExpandedValue(context);
            result = result.add(expandedValue);
        }

        return result;
    }

    @Override
    public String getExpandedField() {
        return ((Expandable)basedMetric[0]).getExpandedField();
    }
}
