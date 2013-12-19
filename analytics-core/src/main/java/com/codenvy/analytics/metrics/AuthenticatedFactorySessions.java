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
public class AuthenticatedFactorySessions extends AbstractLongValueResulted {

    public AuthenticatedFactorySessions() {
        super(MetricType.AUTHENTICATED_FACTORY_SESSIONS);
    }

    @Override
    public String getStorageTableBaseName() {
        return MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST.name().toLowerCase() +
               MongoDataLoader.EXT_COLLECTION_NAME_SUFFIX;
    }

    @Override
    public boolean isSupportMultipleTables() {
        return false;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{ProductUsageFactorySessionsList.AUTHENTICATED_SESSION};
    }

    @Override
    public String getDescription() {
        return "The number sessions in temporary workspaces with registered users";
    }
}
