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

package com.codenvy.analytics.services;

import com.codenvy.analytics.services.model.MetricPojo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:dkuleshov@codenvy.com">Dmitry Kuleshov</a> */
public interface MetricHandler {
    public String getMetricValue(String metricName, Map<String, String> metricContext) throws IOException;

    public MetricPojo getMetricInfo(String metricName);

    public List<MetricPojo> getAllMetricsInfo();
}
