/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

import java.io.IOException;

import com.codenvy.analytics.datamodel.ListValueData;

/** @author Dmytro Nochevnov */
public interface Expandable {
    /**
     * @return the field which consists of values of expanded metric.
     */
    String getExpandedValueField();
    
    /**
     * Returns an expanded list of documents used to calculate numeric value returned by getValue() method.
     *
     * @param context
     *         the execution context, for the most cases it isn't needed to modify it. It is used as a parameter to get
     *         value of other metrics
     * @throws IOException
     *         if any errors are occurred
     */
    ListValueData getExpandedValue(Context context) throws IOException;    
}