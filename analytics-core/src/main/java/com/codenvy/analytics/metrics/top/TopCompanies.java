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
package com.codenvy.analytics.metrics.top;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;

/** @author Anatoliy Bazko */
public class TopCompanies extends AbstractTopEntitiesTime {
    public TopCompanies() {
        super(MetricType.TOP_COMPANIES,
              new MetricType[]{MetricType.PRODUCT_COMPANIES_TIME},
              MetricFilter.USER_COMPANY);
    }
    
    @Override
    public String getDescription() {
        return "Top 100 companies by time working in product during last days";
    }
}
