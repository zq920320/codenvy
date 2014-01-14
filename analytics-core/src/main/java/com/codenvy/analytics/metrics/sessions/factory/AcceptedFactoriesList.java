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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class AcceptedFactoriesList extends AbstractListValueResulted {

    public static final String TMP_WS       = "ws";
    public static final String USER         = "user";
    public static final String FACTORY      = "factory";
    public static final String ORG_ID       = "org_id";
    public static final String AFFILIATE_ID = "affiliate_id";
    public static final String REFERRER     = "referrer";

    public AcceptedFactoriesList() {
        super(MetricType.ACCEPTED_FACTORIES_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{USER,
                            FACTORY,
                            ORG_ID,
                            AFFILIATE_ID,
                            REFERRER};
    }

    @Override
    public String getDescription() {
        return "The list of accepted factories";
    }
}
