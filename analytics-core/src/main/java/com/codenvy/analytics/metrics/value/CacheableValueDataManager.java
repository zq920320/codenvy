/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value;

import com.codenvy.analytics.metrics.MetricType;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class CacheableValueDataManager extends FSValueDataManager {

    private final int                    MAX_ENTRIES = 10;
    private final Map<String, ValueData> cache;

    public CacheableValueDataManager(MetricType metricType) {
        super(metricType);
        cache = initCache();
    }

    @SuppressWarnings("serial")
    private Map<String, ValueData> initCache() {
        return new LinkedHashMap<String, ValueData>(MAX_ENTRIES) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, ValueData> eldest) {
                return size() > MAX_ENTRIES;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueData load(Map<String, String> uuid) throws IOException {
        ValueData value = cache.get(uuid.toString());
        return value != null ? value : super.load(uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void store(ValueData value, Map<String, String> uuid) throws IOException {
        super.store(value, uuid);
        cache.put(uuid.toString(), value);
    }
}
