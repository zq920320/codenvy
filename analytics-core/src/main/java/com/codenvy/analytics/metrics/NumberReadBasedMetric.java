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

import com.codenvy.analytics.metrics.value.FSValueDataManager;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * It is supposed to read precalculated {@link com.codenvy.analytics.metrics.value.ValueData} from storage.
 *
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class NumberReadBasedMetric extends ReadBasedMetric {

    private final MetricType basedMetric;

    NumberReadBasedMetric(MetricType metricType, MetricType basedMetric) {
        super(metricType);
        this.basedMetric = basedMetric;
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData read(MetricType metricType, LinkedHashMap<String, String> uuid) throws IOException {
        return FSValueDataManager.loadNumber(basedMetric, uuid);
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Parameters> getParams() {
        return MetricFactory.createMetric(basedMetric).getParams();
    }
}

