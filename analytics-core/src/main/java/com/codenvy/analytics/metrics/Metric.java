/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.scripts.executor.pig.PigScriptExecutor;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.tools.FileObject;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface Metric {
    /**
     * Returns value metric for given context.
     * 
     * @param context the metric context, the same as used in {@link PigScriptExecutor} for script execution and in {@link FileObject} for
     *            object instantiation
     * @throws IOException if any errors are occurred
     */
    public ValueData getValue(Map<String, String> context) throws IOException;

    /**
     * @return the {@link MetricType} associated with
     */
    public MetricType getType();

    /** @return list of mandatory parameters that have to be passed to the script */
    public abstract Set<MetricParameter> getParams();
}
