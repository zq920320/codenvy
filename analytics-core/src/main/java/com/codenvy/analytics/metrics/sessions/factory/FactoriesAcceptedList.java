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

import com.codenvy.analytics.metrics.AbstractListValueResulted;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.OmittedFilters;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
@OmittedFilters({MetricFilter.USER})
public class FactoriesAcceptedList extends AbstractListValueResulted {

    public FactoriesAcceptedList() {
        super(MetricType.FACTORIES_ACCEPTED_LIST);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{WS,
                            FACTORY,
                            ORG_ID,
                            AFFILIATE_ID,
                            REFERRER};
    }

    @Override
    public String getDescription() {
        return "The list of accepted factories";
    }
}
