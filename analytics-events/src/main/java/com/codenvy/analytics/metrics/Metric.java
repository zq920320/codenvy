/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.FileObject;
import com.codenvy.analytics.scripts.ScriptExecutor;

import java.io.IOException;
import java.util.Map;


/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface Metric {

    /**
     * Returns value metric for given context.
     * 
     * @param context the metric context, the same as used in {@link ScriptExecutor} for script execution and in {@link FileObject} for
     *            object instantiation
     * @throws IOException if any errors are occurred
     */
    public String getValue(Map<String, String> context) throws IOException;

    /**
     * @return the metric title, it is used
     */
    public String getTitle();
}
