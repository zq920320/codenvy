/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
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
import java.util.regex.Pattern;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface Metric {

    Pattern REGISTERED_USER = Pattern.compile("^(?!(ANONYMOUSUSER_|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    Pattern ANONYMOUS_USER = Pattern.compile("^(ANONYMOUSUSER_).*", Pattern.CASE_INSENSITIVE);
    Pattern PERSISTENT_WS  = Pattern.compile("^(?!(TMP-|DEFAULT)).*", Pattern.CASE_INSENSITIVE);
    Pattern TEMPORARY_WS   = Pattern.compile("^(TMP-).*", Pattern.CASE_INSENSITIVE);

    /**
     * Returns the value of metric.
     *
     * @param context
     *         the execution context, for the most cases it isn't needed to modify it. It is used as a parameter to get
     *         value of other metrics
     * @throws IOException
     *         if any errors are occurred
     */
    ValueData getValue(Context context) throws IOException;

    /**
     * @return which type of {@link ValueData} the {@link #getValue(com.codenvy.analytics.metrics.Context)} method
     * returns. The possible variants are:
     * {@link com.codenvy.analytics.datamodel.StringValueData}
     * {@link com.codenvy.analytics.datamodel.LongValueData}
     * {@link com.codenvy.analytics.datamodel.DoubleValueData}
     * {@link com.codenvy.analytics.datamodel.SetValueData}
     * {@link com.codenvy.analytics.datamodel.ListValueData}
     * {@link com.codenvy.analytics.datamodel.MapValueData}
     */
    Class<? extends ValueData> getValueDataClass();

    /** @return the name of the metric */
    String getName();

    /** @return the description of the metric */
    String getDescription();
}
