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

import com.codenvy.analytics.storage.MongoDataLoader;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageSessions extends AbstractListValueResulted {

    public static final String WS         = "ws";
    public static final String USER       = "user";
    public static final String TIME       = "time";
    public static final String START_TIME = "start_time";
    public static final String END_TIME   = "end_time";
    public static final String SESSION_ID = "session_id";

    public ProductUsageSessions() {
        super(MetricType.PRODUCT_USAGE_SESSIONS);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            USER,
                            TIME,
                            START_TIME,
                            END_TIME,
                            SESSION_ID};
    }

    @Override
    public String getStorageTableBaseName() {
        return super.getStorageTableBaseName() + MongoDataLoader.EXT_COLLECTION_NAME_SUFFIX;
    }

    @Override
    public String getDescription() {
        return "Users' sessions";
    }
}
