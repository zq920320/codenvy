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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

import javax.annotation.security.RolesAllowed;

/** @author Dmytro Nochevnov */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters({MetricFilter.WS, MetricFilter.PERSISTENT_WS})
public class TopReferrersBy1Day extends AbstractTopReferrers {

    public TopReferrersBy1Day() {
        super(MetricType.TOP_REFERRERS_BY_1DAY, 1);
    }

    @Override
    public String getDescription() {
        return "The top referrers sorted by overall duration of session in period of time during yesterday";
    }
}
