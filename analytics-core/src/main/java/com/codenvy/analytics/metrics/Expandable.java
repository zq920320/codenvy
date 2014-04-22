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

import com.mongodb.DBObject;

/** @author Dmytro Nochevnov */
public interface Expandable {
    /**
     * @return the field which consists of values of expanded metric.
     */
    String getExpandedValueField();
 
    /**
     * @returns list of specific DB operations to get list of documents used to calculate numeric value of metric.
     * @see generic com.codenvy.analytics.metrics.ReadBasedMetric.getSpecificDBOperations(Context) method
     */
    DBObject[] getSpecificExpandedDBOperations(Context clauses);
}