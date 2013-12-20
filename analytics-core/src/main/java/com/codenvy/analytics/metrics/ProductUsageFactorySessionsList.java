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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProductUsageFactorySessionsList extends AbstractListValueResulted {

    public static final String USER                  = "user";
    public static final String WS                    = "ws";
    public static final String TIME                  = "time";
    public static final String ORG_ID                = "orgId";
    public static final String AFFILIATE_ID          = "affiliateId";
    public static final String CONVERTED             = "conv";
    public static final String AUTHENTICATED         = "auth";
    public static final String REFERRER              = "referrer";
    public static final String FACTORY               = "factory";
    public static final String AUTHENTICATED_SESSION = "authenticated_factory_session";
    public static final String CONVERTED_SESSION     = "converted_factory_session";

    public ProductUsageFactorySessionsList() {
        super(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{TIME,
                            REFERRER,
                            FACTORY,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION};
    }

    @Override
    public String getDescription() {
        return "Factory sessions";
    }
}
