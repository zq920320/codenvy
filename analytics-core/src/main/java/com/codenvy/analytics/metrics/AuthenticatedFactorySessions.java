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

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class AuthenticatedFactorySessions extends AbstractActiveEntities {

    public AuthenticatedFactorySessions() {
        super(MetricType.AUTHENTICATED_FACTORY_SESSIONS, MetricType.FACTORY_SESSIONS_LIST);
    }

    @Override
    public ValueData getValue(Map<String, String> context) throws IOException {
        MetricFilter.AUTHENTICATED_FACTORY_SESSION.put(context, "true");
        return super.getValue(context);
    }


    @Override
    public String getDescription() {
        return "The number sessions in temporary workspaces with registered users";
    }
}
