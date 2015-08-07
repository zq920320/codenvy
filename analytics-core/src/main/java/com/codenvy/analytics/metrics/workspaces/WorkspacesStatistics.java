/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.metrics.workspaces;

import com.codenvy.analytics.metrics.AbstractActiveEntities;
import com.codenvy.analytics.metrics.MetricType;

/**
 * @author Anatoliy Bazko
 */
public class WorkspacesStatistics extends AbstractActiveEntities {

    public WorkspacesStatistics() {
        super(MetricType.WORKSPACES_STATISTICS,
              MetricType.WORKSPACES_STATISTICS_LIST,
              WS);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "The number of workspaces in statistics";
    }
}
