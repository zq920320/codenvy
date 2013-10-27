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


package com.codenvy.analytics.old_metrics;

import com.codenvy.analytics.old_metrics.value.FSValueDataManager;
import com.codenvy.analytics.old_metrics.value.ValueData;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * It is supposed to read precalculated {@link ValueData} from storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class ValueReadBasedMetric extends ReadBasedMetric {

    ValueReadBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData read(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        return FSValueDataManager.loadValue(metricType, uuid);
    }
}

