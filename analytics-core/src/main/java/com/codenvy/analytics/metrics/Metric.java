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
