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

/** @author <a href="mailto:dnochevnov@codenvy.com">Dmytro Nochevnov</a> */
public class TopFactorySessionsByLifetime extends AbstractTopSessions {

    public TopFactorySessionsByLifetime() {
        super(MetricType.TOP_FACTORY_SESSIONS_BY_LIFETIME, -1);
    }

    @Override
    public String getDescription() {
        return "The top 100 factory sessions sorted by duration of session in period of time during all time from yesterday";
    }
}
