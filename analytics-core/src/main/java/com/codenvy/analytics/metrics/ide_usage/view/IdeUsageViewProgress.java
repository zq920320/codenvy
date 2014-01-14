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
package com.codenvy.analytics.metrics.ide_usage.view;

import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.ide_usage.AbstractIdeUsage;
import com.codenvy.analytics.metrics.ide_usage.IdeUsageTypes;

/** @author Alexander Reshetnyak */
public class IdeUsageViewProgress extends AbstractIdeUsage {

    public IdeUsageViewProgress() {
        super(MetricType.IDE_USAGE_VIEW_PROGRESS, new String[]{IdeUsageTypes.VIEW_PROGRESS});
    }

    @Override
    public String getDescription() {
        return "The number of 'View->Progress' operations";
    }
}
