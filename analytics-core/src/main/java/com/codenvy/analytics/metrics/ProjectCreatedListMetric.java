/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ListListStringValueData;
import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.wrapper.DetailsProjectCreatedWrapper;
import com.codenvy.analytics.scripts.ScriptType;

import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectCreatedListMetric extends ScriptBasedMetric {

    ProjectCreatedListMetric() {
        super(MetricType.PROJECTS_CREATED_LIST);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScriptType getScriptType() {
        return ScriptType.DETAILS_PROJECT_CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueData doFilter(ValueData valueData, Map<String, String> context) {
        String userName = context.get(USER_FILTER_PARAM);
        if (userName == null) {
            return valueData;
        }

        ListListStringValueData listVD = (ListListStringValueData)valueData;
        DetailsProjectCreatedWrapper wrapper = new DetailsProjectCreatedWrapper(listVD);

        return wrapper.getProjectsByUser(userName);
    }
}
