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
            case USERS_SSO_LOGGED_IN:
                metric = new UsersSsoLoggedInMetric();
                break;
            case USERS_SSO_LOGGED_IN_USING_GOOGLE:
                metric = new UsersSsoLoggedInUsingGoogleMetric();
                break;
            case USERS_SSO_LOGGED_IN_USING_GITHUB:
                metric = new UsersSsoLoggedInUsingGithubMetric();
                break;
            case USERS_SSO_LOGGED_IN_USING_FORM:
                metric = new UsersSsoLoggedInUsingFormMetric();
                break;
            case PRODUCT_USAGE_TIME:
                metric = new ProductUsageTimeMetric();
                break;
            case PROJECTS_CREATED:
                metric = new ProjectsCreatedMetric();
                break;
            case PROJECTS_DESTROYED:
                metric = new ProjectsDestroyedMetric();
                break;
            case TOTAL_RPOJECTS:
                metric = new TotalProjectsMetric();
                break;
            case PROJECT_CREATED_TYPES:
                metric = new ProjectCreatedTypesMetric();
                break;
            case PROJECT_TYPE_JAVA_JAR:
                metric = new ProjectCreatedTypeJavaJarMetric();
                break;
            case PROJECT_TYPE_JAVA_WAR:
                metric = new ProjectCreatedTypeJavaWarMetric();
                break;
            case PROJECT_TYPE_JAVA_JSP:
                metric = new ProjectCreatedTypeJavaJspMetric();
                break;
            case PROJECT_TYPE_JAVA_SPRING:
                metric = new ProjectCreatedTypeJavaSpring();
                break;
            case PROJECT_TYPE_PHP:
                metric = new ProjectCreatedTypeJavaPhpMetric();
                break;
            case PROJECT_TYPE_PYTHON:
                metric = new ProjectCreatedTypePythonMetric();
                break;
            case PROJECT_TYPE_JAVASCRIPT:
                metric = new ProjectCreatedTypeJavaScriptMetric();
                break;
            case PROJECT_TYPE_RUBY:
                metric = new ProjectCreatedTypeRubyMetric();
                break;
            case PROJECT_TYPE_MMP:
                metric = new ProjectCreatedTypeMmpMetric();
                break;
            case PROJECT_TYPE_GROOVY:
                metric = new ProjectCreatedTypeGroovyMetric();
                break;
            case PROJECT_TYPE_OTHERS:
                metric = new ProjectCreatedTypeOthersMetric();
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
