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
public class FactorySessionsAbove10Min extends AbstractFactorySessions {

    protected FactorySessionsAbove10Min() {
        super(MetricType.FACTORY_SESSIONS_ABOVE_10_MIN, 10 * 60, Integer.MAX_VALUE, true, true);
    }

    @Override
    public String getDescription() {
        return "The number of sessions in temporary workspaces with duration greater than 10 minutes";
    }
}
