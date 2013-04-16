/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MetricFactory {

    private static ConcurrentHashMap<MetricType, Metric> metrics = new ConcurrentHashMap<MetricType, Metric>();

    /**
     * Creates new metric or returns existed one.
     */
    public static Metric createMetric(MetricType metricType) throws IOException {
        Metric metric = metrics.get(metricType);

        if (metric != null) {
            return metric;
        }

        switch (metricType) {
            case WORKSPACES_CREATED:
                metric = new WorkspacesCreatedMetric();
                break;
            case WORKSPACES_DESTROYED:
                metric = new WorkspacesDestoryedMetric();
                break;
            case TOTAL_WORKSPACES:
                metric = new TotalWorkspacesMetric();
                break;
            case ACTIVE_WORKSPACES:
                metric = new ActiveWorkspacesMetric();
                break;
            case PERCENT_ACTIVE_WORKSPACES:
                metric = new PercentActiveWorkspacesMetric();
                break;
            case PERCENT_INACTIVE_WORKSPACES:
                metric = new PercentInactiveWorkspacesMetric();
                break;
            case USERS_CREATED:
                metric = new UsersCreatedMetric();
                break;
            case USERS_DESTROYED:
                metric = new UsersDestroyedMetric();
                break;
            case TOTAL_USERS:
                metric = new TotalUsersMetric();
                break;
            case ACTIVE_USERS:
                metric = new ActiveUsersMetric();
                break;
            case PERCENT_ACTIVE_USERS:
                metric = new PercentActiveUsersMetric();
                break;
            case PERCENT_INACTIVE_USERS:
                metric = new PercentInactiveUsersMetric();
                break;
            case USERS_CREATED_PROJECTS:
                metric = new UsersCreatedProjectsMetric();
                break;
            case PERCENT_USERS_CREATED_PROJECTS:
                metric = new PercentUsersCreatedProjectsMetric();
                break;
            case USERS_ADDED_TO_WORKSPACE:
                metric = new UsersAddedToWorkspaceMetric();
                break;
            case USERS_ADDED_TO_WORKSPACE_FROM_WEBSITE:
                metric = new UsersAddedToWsFromWebsiteMetric();
                break;
            case USERS_ADDED_TO_WORKSPACE_FROM_INVITE:
                metric = new UsersAddedToWsFromInviteMetric();
                break;

            default:
                throw new IllegalArgumentException("Unknown metric type " + metricType);
        }

        metrics.putIfAbsent(metricType, metric);
        return metric;
    }

    /**
     * Creates new metric or returns existed one.
     */
    public static Metric createMetric(String name) throws IOException {
        MetricType metricType = MetricType.valueOf(name.toUpperCase());
        return createMetric(metricType);
    }
}
