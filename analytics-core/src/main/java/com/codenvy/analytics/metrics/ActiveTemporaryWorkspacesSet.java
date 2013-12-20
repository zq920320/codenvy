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
public class ActiveTemporaryWorkspacesSet extends AbstractSetValueResulted {

    public ActiveTemporaryWorkspacesSet() {
        super(MetricType.ACTIVE_TEMPORARY_WORKSPACES_SET);
    }

    @Override
    public boolean isSupportMultipleTables() {
        return false;
    }

    @Override
    public String getStorageTableBaseName() {
        return MetricType.ACCEPTED_FACTORIES_LIST.name().toLowerCase() + MongoDataLoader.EXT_COLLECTION_NAME_SUFFIX;
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{AcceptedFactoriesList.TMP_WS};
    }

    @Override
    public String getDescription() {
        return "Temporary workspaces";
    }
}
