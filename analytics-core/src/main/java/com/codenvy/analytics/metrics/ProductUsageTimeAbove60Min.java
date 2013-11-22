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
public class ProductUsageTimeAbove60Min extends AbstractProductUsageTime {

    public ProductUsageTimeAbove60Min() {
        super(MetricType.PRODUCT_USAGE_TIME_ABOVE_60_MIN, 60, Integer.MAX_VALUE, false, true);
    }

    @Override
    public String getDescription() {
        return "The total time of all sessions in persistent workspaces with duration more than 60 minutes";
    }
}
