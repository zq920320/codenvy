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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface Metric {

    /**
     * Returns value metric for given context.
     *
     * @param context
     *         execution context
     * @throws IOException
     *         if any errors are occurred
     */
    ValueData getValue(Map<String, String> context) throws IOException;

    /** @return what data type is represented in result */
    Class<? extends ValueData> getValueDataClass();

    /** @return list of mandatory parameters that have to be passed to the script */
    Set<MetricParameter> getParams();
}
