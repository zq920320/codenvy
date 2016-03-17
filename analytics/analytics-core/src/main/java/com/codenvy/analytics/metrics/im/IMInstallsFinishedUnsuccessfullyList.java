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
package com.codenvy.analytics.metrics.im;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;

/**
 * @author Alexander Reshetnyak
 */
@InternalMetric
@OmitFilters({MetricFilter.WS_ID, MetricFilter.PERSISTENT_WS})
public class IMInstallsFinishedUnsuccessfullyList extends AbstractListValueResulted {

    public IMInstallsFinishedUnsuccessfullyList() {
        super(MetricType.IM_INSTALLS_FINISHED_UNSUCCESSFULLY_LIST);
    }

    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.IM_INSTALLS_FINISHED_UNSUCCESSFULLY);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE,
                            USER,
                            IM_ARTIFACT,
                            IM_VERSION,
                            IM_TIME,
                            IM_USER_IP,
                            IM_ERROR_MESSAGE};
    }

    @Override
    public String getDescription() {
        return "The list of IM install finished unsuccessfully";
    }
}