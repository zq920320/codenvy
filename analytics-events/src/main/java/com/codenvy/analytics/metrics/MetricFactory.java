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
            case PERCENT_USERS_ADDED_TO_WORKSPACE_FROM_WEBSITE:
                metric = new PercentUsersAddedToWsFromWebsiteMetric();
                break;
            case PERCENT_USERS_ADDED_TO_WORKSPACE_FROM_INVITE:
                metric = new PercnetUsersAddedToWsFromInviteMetric();
                break;
            case USERS_SSO_LOGGED_IN:
                metric = new UsersSsoLoggedInMetric();
                break;
            case PERCENT_USERS_SSO_LOGGED_IN_USING_GOOGLE:
                metric = new UsersSsoLoggedInUsingGoogleMetric();
                break;
            case PERCENT_USERS_SSO_LOGGED_IN_USING_GITHUB:
                metric = new UsersSsoLoggedInUsingGithubMetric();
                break;
            case PERCENT_USERS_SSO_LOGGED_IN_USING_FORM:
                metric = new UsersSsoLoggedInUsingFormMetric();
                break;
            case PRODUCT_USAGE_TIME:
                metric = new ProductUsageTimeMetric();
                break;
            case PROJECTS_CREATED:
                metric = new ProjectsCreatedMetric();
                break;
            case BUILT_PROJECTS:
                metric = new BuiltProjectsMetric();
                break;
            case PERCENT_BUILT_PROJECTS:
                metric = new PercentBuiltProjectsMetric();
                break;
            case PROJECTS_DESTROYED:
                metric = new ProjectsDestroyedMetric();
                break;
            case TOTAL_PROJECTS:
                metric = new TotalProjectsMetric();
                break;
            case ACTIVE_PROJECTS:
                metric = new ActiveProjectsMetric();
                break;
            case PERCENT_ACTIVE_PROJECTS:
                metric = new PercentActiveProjectsMetric();
                break;
            case PERCENT_INACTIVE_PROJECTS:
                metric = new PercentInactiveProjectsMetric();
                break;
            case PROJECT_CREATED_TYPES:
                metric = new ProjectCreatedTypesMetric();
                break;
            case PERCENT_PROJECT_TYPE_JAVA_JAR:
                metric = new ProjectCreatedTypeJavaJarMetric();
                break;
            case PERCENT_PROJECT_TYPE_JAVA_WAR:
                metric = new ProjectCreatedTypeJavaWarMetric();
                break;
            case PERCENT_PROJECT_TYPE_JAVA_JSP:
                metric = new ProjectCreatedTypeJavaJspMetric();
                break;
            case PERCENT_PROJECT_TYPE_JAVA_SPRING:
                metric = new ProjectCreatedTypeJavaSpring();
                break;
            case PERCENT_PROJECT_TYPE_PHP:
                metric = new ProjectCreatedTypeJavaPhpMetric();
                break;
            case PERCENT_PROJECT_TYPE_PYTHON:
                metric = new ProjectCreatedTypePythonMetric();
                break;
            case PERCENT_PROJECT_TYPE_JAVASCRIPT:
                metric = new ProjectCreatedTypeJavaScriptMetric();
                break;
            case PERCENT_PROJECT_TYPE_RUBY:
                metric = new ProjectCreatedTypeRubyMetric();
                break;
            case PERCENT_PROJECT_TYPE_MMP:
                metric = new ProjectCreatedTypeMmpMetric();
                break;
            case PERCENT_PROJECT_TYPE_GROOVY:
                metric = new ProjectCreatedTypeGroovyMetric();
                break;
            case PERCENT_PROJECT_TYPE_OTHERS:
                metric = new ProjectCreatedTypeOthersMetric();
                break;
            case PAAS_DEPLOYEMNT_TYPES:
                metric = new PaasDeploymentTypesMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_AWS:
                metric = new PaasDeploymentTypeAwsMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_APPFOG:
                metric = new PaasDeploymentTypeAppFogMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_CLOUDFOUNDRY:
                metric = new PaasDeploymentTypeCloudFoundryMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_CLOUDBESS:
                metric = new PaasDeploymentTypeCloudBeesMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_GAE:
                metric = new PaasDeploymentTypeGaeMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_HEROKU:
                metric = new PaasDeploymentTypeHerokuMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_LOCAL:
                metric = new PaasDeploymentTypeLocalMetric();
                break;
            case PERCENT_PAAS_DEPLOYEMNT_TYPE_OPENSHIFT:
                metric = new PaasDeploymentTypeOpenShiftMetric();
                break;
            case JREBEL_ELIGIBLE:
                metric = new JrebelElibigleMetric();
                break;
            case JREBEL_USAGE:
                metric = new JrebelUsageMetric();
                break;
            case PERCENT_JREBEL_USAGE:
                metric = new PercentJrebelConvertMetric();
                break;
            case INVITATIONS_SENT:
                metric = new InvitationsSentMetric();
                break;
            case PERCENT_INVITATIONS_ACTIVATED:
                metric = new PercentInvitationsActivatedMetric();
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
