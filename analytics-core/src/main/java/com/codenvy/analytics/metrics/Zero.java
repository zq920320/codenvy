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
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author Dmytro Nochevnov */
@RolesAllowed({"system/admin", "system/manager"})
public class Zero extends AbstractMetric implements Expandable {
    public Zero() {
        super(MetricType.ZERO);
    }

    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        return LongValueData.DEFAULT;
    }

    @Override
    public String getDescription() {
        return "Metric which always returns 0 value.";
    }

    @Override
    public String getExpandedField() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ValueData getExpandedValue(Context context) throws IOException {
        return ListValueData.DEFAULT;
    }

}
