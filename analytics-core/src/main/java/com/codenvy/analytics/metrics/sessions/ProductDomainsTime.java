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
package com.codenvy.analytics.metrics.sessions;

import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/** @author Anatoliy Bazko */
@RolesAllowed({"system/admin", "system/manager"})
public class ProductDomainsTime extends AbstractProductTime {

    public ProductDomainsTime() {
        super(MetricType.PRODUCT_DOMAINS_TIME);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{DOMAIN,
                            TIME,
                            SESSIONS};
    }

    @Override
    public String getDescription() {
        return "Domains' time spent in product";
    }
}
