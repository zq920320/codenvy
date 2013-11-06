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

import com.codenvy.analytics.datamodel.ValueData;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface Metric {

    /**
     * Returns the value of metric for given context.
     *
     * @param context
     *         the execution context
     * @throws IOException
     *         if any errors are occurred
     */
    ValueData getValue(Map<String, String> context) throws IOException;

    /** @return which data type is returned by metric */
    Class<? extends ValueData> getValueDataClass();

    /** @return the mandatory parameters have to be existed in the context */
    Set<Parameters> getParams();

    /** @return the name of the metric */
    String getName();

    /** @return the description of the metric */
    String getDescription();
}
