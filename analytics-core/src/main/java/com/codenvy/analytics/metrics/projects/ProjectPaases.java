/*
 * Copyright (C) 2013 Codenvy.
 */
package com.codenvy.analytics.metrics.projects;

import com.codenvy.analytics.metrics.AbstractMapValueResulted;
import com.codenvy.analytics.metrics.MetricType;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class ProjectPaases extends AbstractMapValueResulted {

    public static final String GAE              = "gae";
    public static final String AWS_BEANSTALK    = "aws:beanstalk";
    public static final String CLOUDFOUNDRY     = "cloudfoundry";
    public static final String TIER3_WEB_FABRIC = "tier3 web fabric";
    public static final String MANYMO           = "manymo";
    public static final String OPENSHIFT        = "openshift";
    public static final String HEROKU           = "heroku";
    public static final String APPFOG           = "appfog";
    public static final String CLOUDBEES        = "cloudbees";

    public ProjectPaases() {
        super(MetricType.PROJECT_PAASES);
    }

    public ProjectPaases(String metricName) {
        super(metricName);
    }

    @Override
    public String[] getTrackedFields() {
        return new String[]{GAE,
                            AWS_BEANSTALK,
                            CLOUDFOUNDRY,
                            TIER3_WEB_FABRIC,
                            MANYMO,
                            OPENSHIFT,
                            HEROKU,
                            APPFOG,
                            CLOUDBEES};
    }

    @Override
    public String getDescription() {
        return "The number of deployed projects on specific PaaS";
    }
}