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

import com.codenvy.analytics.metrics.AbstractSetValueResulted;
import com.codenvy.analytics.metrics.MetricType;

import javax.annotation.security.RolesAllowed;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
@RolesAllowed({"system/admin", "system/manager"})
public class CreatedFactoriesSet extends AbstractSetValueResulted {

    public static final String WS           = "ws";
    public static final String USER         = "user";
    public static final String FACTORY      = "factory";
    public static final String ORG_ID       = "org_id";
    public static final String AFFILIATE_ID = "affiliate_id";
    public static final String REPOSITORY   = "repository";

    public CreatedFactoriesSet() {
        super(MetricType.CREATED_FACTORIES_SET, FACTORY);
    }

    @Override
    public String getDescription() {
        return "The list of created factories.";
    }
}
