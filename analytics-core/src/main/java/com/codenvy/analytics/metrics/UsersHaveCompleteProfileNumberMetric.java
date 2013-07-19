/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.LongValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.filters.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class UsersHaveCompleteProfileNumberMetric extends CalculateBasedMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersHaveCompleteProfileNumberMetric.class);
    private final ActiveUsersWorkspacesListMetric basedMetric;

    public UsersHaveCompleteProfileNumberMetric() {
        super(MetricType.USERS_HAVE_COMPLETE_PROFILE_NUMBER);
        basedMetric =
                (ActiveUsersWorkspacesListMetric)MetricFactory.createMetric(MetricType.ACTIVE_USERS_WORKAPCES_LIST);
    }

    /** {@inheritDoc} */
    @Override
    protected ValueData evaluate(Map<String, String> context) throws IOException {
        ValueData valueData = basedMetric.getValue(context);
        Filter filter = basedMetric.createFilter(valueData);

        Set<String> availables = filter.getAvailable(MetricFilter.FILTER_USER);

        long count = 0;
        for (String user : availables) {
            context.put(MetricParameter.ALIAS.name(), user);

            UsersProfileMetric profile = (UsersProfileMetric)MetricFactory.createMetric(MetricType.USER_PROFILE);
            ListListStringValueData profileValue = (ListListStringValueData)profile.getValue(context);

            // TODO
            if (profileValue.getAll().size() == 0) {
                LOGGER.warn("skipped for " + user);
                continue;
            }

            if (!profile.getCompany(profileValue).isEmpty() &&
                !profile.getFirstName(profileValue).isEmpty() &&
                !profile.getLastName(profileValue).isEmpty() &&
                !profile.getPhone(profileValue).isEmpty() &&
                !profile.getJob(profileValue).isEmpty()) {
                count++;
            }
        }

        return new LongValueData(count);
    }

    /** {@inheritDoc} */
    @Override
    protected Class<? extends ValueData> getValueDataClass() {
        return LongValueData.class;
    }

    /** {@inheritDoc} */
    @Override
    public Set<MetricParameter> getParams() {
        return basedMetric.getParams();
    }
}
