/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.analytics.services.reports;

import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.services.configuration.ParameterConfiguration;

import java.util.List;
import java.util.Set;

/**
 * @author Anatoliy Bazko
 */
public class OrgIdAsFilterContextModifier extends AbstractContextModifier {

    private static final String ORG_ID = "org-id";

    public OrgIdAsFilterContextModifier(List<ParameterConfiguration> parameters) {
        super(parameters);
    }

    @Override
    public Context update(Context context) {
        Set<String> params = getParameters(ORG_ID);

        String[] orgIds = new String[params.size()];
        params.toArray(orgIds);

        Context.Builder builder = new Context.Builder(context);
        builder.put(MetricFilter.ORG_ID, orgIds);
        return builder.build();
    }
}
