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
package com.codenvy.analytics.metrics.sessions.factory;

import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.*;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class ProductUsageFactorySessionsList extends AbstractListValueResulted {
    public ProductUsageFactorySessionsList() {
        super(MetricType.PRODUCT_USAGE_FACTORY_SESSIONS_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{SESSION_ID,
                            USER,
                            WS,
                            DATE,
                            TIME,
                            REFERRER,
                            FACTORY,
                            AUTHENTICATED_SESSION,
                            CONVERTED_SESSION};
    }

    @Override
    public ValueData postEvaluation(ValueData valueData, Context clauses) throws IOException {
        ReadBasedMetric metric = (ReadBasedMetric)MetricFactory.getMetric(MetricType.PRODUCT_USAGE_SESSIONS_LIST);
        return metric.postEvaluation(valueData, clauses);
    }

    @Override
    public String getDescription() {
        return "Factory sessions";
    }
}
