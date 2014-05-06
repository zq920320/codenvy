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

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.CalculatedMetric;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.Expandable;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.Parameters;
import com.codenvy.analytics.metrics.Context.Builder;

import javax.annotation.security.RolesAllowed;

import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class NonActiveWorkspaces extends CalculatedMetric implements Expandable {

    public NonActiveWorkspaces() {
        super(MetricType.NON_ACTIVE_WORKSPACES, new MetricType[]{MetricType.ACTIVE_WORKSPACES,
                                                                 MetricType.TOTAL_WORKSPACES});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData active = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData total = ValueDataUtil.getAsLong(basedMetric[1], context);

        return new LongValueData(total.getAsLong() - active.getAsLong());
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "Non-active workspaces";
    }
    
    @Override
    public String getExpandedValueField() {
        return WS;
    }

    @Override
    public ListValueData getExpandedValue(Context context) throws IOException {
        ListValueData active = ((Expandable) basedMetric[0]).getExpandedValue(context);
        
        // get all documents from start date of logging to date defined in context
        context = new Builder(context)
                      .remove(Parameters.FROM_DATE)
                      .build();
        ListValueData total = ((Expandable) basedMetric[0]).getExpandedValue(context);
        
        ListValueData result = total.doSubtract(active);
        
        return result;
    }
}
