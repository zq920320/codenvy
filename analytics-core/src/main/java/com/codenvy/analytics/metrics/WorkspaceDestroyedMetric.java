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

import com.codenvy.analytics.datamodel.LongValueData;
import com.codenvy.analytics.datamodel.ValueData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class WorkspaceDestroyedMetric extends ReadBasedMetric {

    public WorkspaceDestroyedMetric() {
        super(MetricType.WORKSPACE_DESTROYED);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return new HashSet<>(Arrays.asList(new Parameters[]{Parameters.FROM_DATE, Parameters.TO_DATE}));
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of destroyed persistent workspaces";
    }
}
