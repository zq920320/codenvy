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
package com.codenvy.analytics.metrics.ide_usage;

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
public class CodeCompletions extends CalculatedMetric implements Expandable {

    public CodeCompletions() {
        super(MetricType.CODE_COMPLETIONS, new MetricType[]{MetricType.CODE_COMPLETIONS_BASED_ON_EVENT,
                                                            MetricType.CODE_COMPLETIONS_BASED_ON_IDE_USAGES});
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        LongValueData value1 = ValueDataUtil.getAsLong(basedMetric[0], context);
        LongValueData value2 = ValueDataUtil.getAsLong(basedMetric[1], context);
        return value1.add(value2);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public String getDescription() {
        return "The number of code completion actions";
    }

    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        ValueData value1 = ((Expandable)basedMetric[0]).getExpandedValue(context);
        ValueData value2 = ((Expandable)basedMetric[1]).getExpandedValue(context);
        return value1.add(value2);
    }

    @Override
    public String getExpandedField() {
        return ((Expandable)basedMetric[0]).getExpandedField();
    }
}

