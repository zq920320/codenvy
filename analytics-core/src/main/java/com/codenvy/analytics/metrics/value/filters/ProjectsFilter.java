/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics.value.filters;

import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.value.ListListStringValueData;

/**
 * <li>0 - the workspace name</li><br>
 * <li>1 - the user name</li><br>
 * <li>2 - the project name</li><br>
 * <li>3 - the project type</li><br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsFilter extends AbstractFilter {

    public ProjectsFilter(ListListStringValueData valueData) {
        super(valueData);
    }

    /**
     * @return the unique projects only
     */
    public ListListStringValueData getUniqueProjects() {
        return getUniqueActions(MetricFilter.FILTER_WS, MetricFilter.FILTER_PROJECT_NAME, MetricFilter.FILTER_PROJECT_TYPE);
    }

    protected int getIndex(MetricFilter key) throws IllegalArgumentException {
        switch (key) {
            case FILTER_WS:
            case FILTER_USER:
            case FILTER_PROJECT_NAME:
            case FILTER_PROJECT_TYPE:
                return key.ordinal();
            default:
                throw new IllegalArgumentException();
        }
    }
}
