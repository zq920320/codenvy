/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.edits;

import com.codenvy.analytics.datamodel.DoubleValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.datamodel.ValueDataUtil;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.tasks.AbstractTasksMetric;
import com.codenvy.analytics.pig.udf.CalculateGigabyteRamHours;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author Dmytro Nochevnov */
@RolesAllowed(value = {"user", "system/admin", "system/manager"})
public class EditsGigabyteRamHours extends AbstractTasksMetric {
    public static final long EDITOR_MEMORY_USAGE_MB = 25;

    public EditsGigabyteRamHours() {
        super(MetricType.EDITS_GIGABYTE_RAM_HOURS, MetricType.EDITS_TIME);
    }

    @Override
    public ValueData getValue(Context context) throws IOException {
        long editorUsageTime = ValueDataUtil.getAsLong(basedMetric[0], context).getAsLong();
        return new DoubleValueData(CalculateGigabyteRamHours.calculateGigabiteRamHours(EDITOR_MEMORY_USAGE_MB, editorUsageTime));
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return DoubleValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The editor memory usage in GB RAM on hour";
    }
}
