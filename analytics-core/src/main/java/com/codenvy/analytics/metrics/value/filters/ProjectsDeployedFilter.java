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
 * <li>4 - the paas</li><br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class ProjectsDeployedFilter extends ProjectsFilter {

    public ProjectsDeployedFilter(ListListStringValueData valueData) {
        super(valueData);
    }
    
    /** {@inheritDoc} */
    @Override
    protected int getIndex(MetricFilter key) throws IllegalArgumentException {
        switch (key) {
            case FILTER_WS:
            case FILTER_USER:
            case FILTER_PROJECT_NAME:
            case FILTER_PROJECT_TYPE:
            case FILTER_PROJECT_PAAS:
                return key.ordinal();
            default:
                throw new IllegalArgumentException();
        }
    }
}
