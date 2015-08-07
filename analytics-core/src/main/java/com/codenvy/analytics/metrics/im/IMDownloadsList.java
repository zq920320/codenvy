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
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmitFilters;
import com.codenvy.analytics.metrics.ReadBasedSummariziable;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/** @author Anatoliy Bazko */
@InternalMetric
@OmitFilters({MetricFilter.WS_ID, MetricFilter.PERSISTENT_WS})
public class IMDownloadsList extends AbstractListValueResulted implements ReadBasedSummariziable {
    public static final String ARTIFACT_CODENVY                = "codenvy";
    public static final String ARTIFACT_INSTALL_CODENVY_SINGLE = "install_codenvy";
    public static final String ARTIFACT_INSTALL_CODENVY_MULTI  = "install_codenvy_multi";
    public static final String ARTIFACT_INSTALL_CLI            = "install_im_cli";

    public static final String CODENVY_BINARIES      = "codenvy";
    public static final String INSTALL_SINGLE_SCRIPT = "single-node script";
    public static final String INSTALL_MULTI_SCRIPT  = "multi-node script";
    public static final String INSTALL_IM_SCRIPT     = "im";

    public IMDownloadsList() {
        super(MetricType.IM_DOWNLOADS_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public String getStorageCollectionName() {
        return getStorageCollectionName(MetricType.IM_DOWNLOADS);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Download statistics";
    }

    /** {@inheritDoc} */
    @Override
    public String[] getTrackedFields() {
        return new String[]{DATE,
                            USER,
                            IM_ARTIFACT,
                            IM_VERSION,
                            CODENVY_BINARIES,
                            INSTALL_SINGLE_SCRIPT,
                            INSTALL_MULTI_SCRIPT,
                            INSTALL_IM_SCRIPT};
    }

    /** {@inheritDoc} */
    @Override
    public DBObject[] getSpecificSummarizedDBOperations(Context clauses) {
        DBObject group = new BasicDBObject();
        group.put(ID, null);
        group.put(CODENVY_BINARIES, new BasicDBObject("$sum", "$" + ARTIFACT_CODENVY));
        group.put(INSTALL_SINGLE_SCRIPT, new BasicDBObject("$sum", "$" + ARTIFACT_INSTALL_CODENVY_SINGLE));
        group.put(INSTALL_MULTI_SCRIPT, new BasicDBObject("$sum", "$" + ARTIFACT_INSTALL_CODENVY_MULTI));
        group.put(INSTALL_IM_SCRIPT, new BasicDBObject("$sum", "$" + ARTIFACT_INSTALL_CLI));
        return new DBObject[]{new BasicDBObject("$group", group)};
    }
}
