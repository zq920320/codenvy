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


import com.codenvy.analytics.metrics.CumulativeMetric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class TotalWorkspaces extends CumulativeMetric {

    public TotalWorkspaces() {
        super(MetricType.TOTAL_WORKSPACES,
              MetricFactory.getMetric(MetricType.CREATED_WORKSPACES),
              MetricFactory.getMetric(MetricType.DESTROYED_WORKSPACES));
    }

    @Override
    public String getDescription() {
        return "The total number of persistent workspaces";
    }
}
