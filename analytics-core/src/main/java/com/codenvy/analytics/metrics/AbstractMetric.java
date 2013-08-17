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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class AbstractMetric implements Metric {

    protected final MetricType metricType;

    AbstractMetric(MetricType metricType) {
        this.metricType = metricType;
    }

    /** Preparation unique sequences to identify stored value. */
    protected LinkedHashMap<String, String> makeUUID(Map<String, String> context) throws IOException {
        LinkedHashMap<String, String> keys = new LinkedHashMap<>();

        for (MetricParameter param : getParams()) {
            String paramKey = param.name();
            String paramValue = context.get(paramKey);

            if (paramKey.equals(MetricParameter.RESULT_DIR.name())) {
                continue;
            } else if (paramValue == null) {
                throw new IOException("There is no parameter " + paramKey + " in context");
            }

            keys.put(paramKey, paramValue);
        }

        return keys;
    }

    /** @return true if context contains filter parameter and false otherwise */
    protected boolean isFilterExists(Map<String, String> context) {
        for (MetricFilter filterKey : MetricFilter.values()) {
            if (context.containsKey(filterKey.name())) {
                return true;
            }
        }

        return false;
    }
}
