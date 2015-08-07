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
package com.codenvy.analytics.metrics.accounts;

import com.codenvy.analytics.datamodel.ListValueData;
import com.codenvy.analytics.datamodel.MapValueData;
import com.codenvy.analytics.datamodel.StringValueData;
import com.codenvy.analytics.datamodel.ValueData;
import com.codenvy.analytics.metrics.Context;
import com.codenvy.analytics.metrics.InternalMetric;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.RequiredFilter;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.eclipse.che.api.core.rest.shared.dto.Link;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@InternalMetric
@RequiredFilter(MetricFilter.ACCOUNT_ID)
public class AccountFactoriesList extends AbstractAccountMetric {

    public AccountFactoriesList() {
        super(MetricType.ACCOUNT_FACTORIES_LIST);
    }

    /** {@inheritDoc} */
    @Override
    public ValueData getValue(Context context) throws IOException {
        String accountId = context.getAsString(MetricFilter.ACCOUNT_ID);
        List<Link> linkFactories = getFactoriesByAccountId(accountId);

        List<ValueData> l = FluentIterable.from(linkFactories).transform(new Function<Link, ValueData>() {
            @Override
            public ValueData apply(Link link) {
                Map<String, ValueData> m = new HashMap<>(2);
                m.put("factory", StringValueData.valueOf(link.getHref()));
                m.put("parameters", StringValueData.valueOf(link.getParameters().toString()));
                return MapValueData.valueOf(m);
            }
        }).toList();

        return ListValueData.valueOf(l);
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return "Factories";
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends ValueData> getValueDataClass() {
        return ListValueData.class;
    }
}