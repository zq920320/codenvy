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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;
import com.codenvy.analytics.metrics.Parameters.PassedDaysCount;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmitFilters(MetricFilter.WS)
public class TopFactoriesBy7Days extends AbstractTopFactories {

    public TopFactoriesBy7Days() {
        super(MetricType.TOP_FACTORIES_BY_7_DAYS, PassedDaysCount.BY_7_DAYS);
    }

    @Override
    public String getDescription() {
        return "The top factories with the same url sorted by overall duration of session in period of time during 7 days before today";
    }
}
