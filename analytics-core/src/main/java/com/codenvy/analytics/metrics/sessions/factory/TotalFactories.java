/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

package com.codenvy.analytics.metrics.sessions.factory;


import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;

/** @author Dmytro Nochevnov */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class TotalFactories extends CumulativeMetric {

    public TotalFactories() {
        super(MetricType.TOTAL_FACTORIES,
              MetricFactory.getMetric(MetricType.CREATED_FACTORIES),
              MetricFactory.getMetric(MetricType.ZERO));        
    }

    @Override
    public String getDescription() {
        return "The total number of created factories";
    }

    @Override
    public String getExpandedField() {
        return FACTORY;
    }
}
