/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.ScriptType;
import com.codenvy.analytics.scripts.executor.ScriptExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public abstract class ScriptBasedMetric extends AbstractMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptBasedMetric.class);

    protected static final ConcurrentHashMap<String, AtomicBoolean> executions = new ConcurrentHashMap<>();

    ScriptBasedMetric(MetricType metricType) {
        super(metricType);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Calculation " + getType() + " with context " + context.toString());
        }

        return evaluate(context);
    }

    protected ValueData evaluate(Map<String, String> context) throws IOException {
        String executionKey = metricType.name() + makeUUID(context).toString();

        acquire(executionKey);
        try {
            return executeScript(context);
        } finally {
            release(executionKey);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return getScriptType().getParams();
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return getScriptType().getValueDataClass();
    }

    protected ValueData executeScript(Map<String, String> context) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Script execution " + getScriptType() + " with context " + context.toString());
        }

        ScriptExecutor sExecutor = getScriptExecutor();
        return sExecutor.executeAndReturn(getScriptType(), context);
    }

    protected ScriptExecutor getScriptExecutor() {
        return ScriptExecutor.INSTANCE;
    }

    protected void acquire(String executionKey) {
        AtomicBoolean monitor = new AtomicBoolean(true);

        AtomicBoolean currentValue = executions.putIfAbsent(executionKey, monitor);
        if (currentValue != null) {
            synchronized (currentValue) {
                while (!currentValue.get()) {
                    try {
                        currentValue.wait();
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    protected void release(String executionKey) {
        AtomicBoolean monitor = executions.remove(executionKey);
        if (monitor != null) {
            monitor.set(false);

            synchronized (monitor) {
                monitor.notifyAll();
            }
        }
    }

    /** @return corresponding {@link ScriptType} for metric calculation. */
    abstract protected ScriptType getScriptType();
}
