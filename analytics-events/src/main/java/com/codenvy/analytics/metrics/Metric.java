/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;

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
     * @param context the metric context, the same as used in {@link ScriptExecutor} for script execution and in {@link FileObject} for
     *            object instantiation
     * @throws IOException if any errors are occurred
     */
    public Object getValue(Map<String, String> context) throws IOException;

    /** @return list of mandatory parameters that have to be passed to the script */
    public abstract Set<ScriptParameters> getMandatoryParams();

    /** @return list of additional parameters (not mandatory) that might be passed to the script */
    public abstract Set<ScriptParameters> getAdditionalParams();

    /**
     * @return the metric title, it is used
     */
    public String getTitle();
}
