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
        if (metrics.contains(metricType)) {
            return metrics.get(metricType);
        }

        Metric metric;
        switch (metricType) {
            case WORKSPACES_CREATED_NUMBER:
                metric = new WorkspacesCreatedNumberMetric();
                break;
            case WORKSPACES_DESTROYED_NUMBER:
                metric = new WorkspacesDestoryedNumberMetric();
                break;
            case TOTAL_WORKSPACES_NUMBER:
                metric = new TotalWorkspacesMetric();
                break;
            case ACTIVE_WORKSPACES_NUMBER:
                metric = new ActiveWorkspacesNumberMetric();
                break;
            case ACTIVE_WORKSPACES_PERCENT:
                metric = new ActiveWorkspacesPercentMetric();
                break;
            case USERS_CREATED_LIST:
                metric = new UsersCreatedListMetric();
                break;
            case USERS_DESTROYED_LIST:
                metric = new UsersDestroyedListMetric();
                break;
            case USERS_CREATED_NUMBER:
                metric = new UsersCreatedNumberMetric();
                break;
            case USERS_DESTROYED_NUMBER:
                metric = new UsersDestroyedNumberMetric();
                break;
            case TOTAL_USERS_NUMBER:
                metric = new TotalUsersMetric();
                break;
            case ACTIVE_USERS_NUMBER:
                metric = new ActiveUsersNumberMetric();
                break;
            case ACTIVE_USERS_WORKAPCES_SET:
                metric = new ActiveUsersWorkspacesSetMetric();
                break;
            case ACTIVE_USERS_PERCENT:
                metric = new ActiveUsersPercentMetric();
                break;
            case USERS_CREATED_PROJECTS_NUMBER:
                metric = new UsersCreatedProjectsNumberMetric();
                break;
            case USERS_CREATED_PROJECTS_PERCENT:
                metric = new UsersCreatedProjectsPercentMetric();
                break;
            case USERS_ADDED_TO_WORKSPACE:
                metric = new UsersAddedToWorkspaceMetric();
                break;
            case USERS_ADDED_TO_WORKSPACE_FROM_WEBSITE_PERCENT:
                metric = new UsersAddedToWsFromWebsitePercentMetric();
                break;
            case USERS_ADDED_TO_WORKSPACE_FROM_INVITE_PERCENT:
                metric = new UsersAddedToWsFromInvitePercentMetric();
                break;
            case USERS_SSO_LOGGED_IN:
                metric = new UsersSsoLoggedInMetric();
                break;
            case USERS_SSO_LOGGED_IN_USING_GOOGLE_PERCENT:
                metric = new UsersSsoLoggedInUsingGooglePercentMetric();
                break;
            case USERS_SSO_LOGGED_IN_USING_GITHUB_PERCENT:
                metric = new UsersSsoLoggedInUsingGithubPercentMetric();
                break;
            case USERS_SSO_LOGGED_IN_USING_FORM_PERCENT:
                metric = new UsersSsoLoggedInUsingFormPercentMetric();
                break;
            case PRODUCT_USAGE_TIME_TOTAL:
                metric = new ProductUsageTimeTotalMetric();
                break;
            case PRODUCT_USAGE_TIME_LIST:
                metric = new ProductUsageTimeListlMetric();
                break;
            case PRODUCT_USAGE_USER_SESSIONS_NUMBER:
                metric = new ProductUsageUserSessionsNumberMetric();
                break;
            case PROJECTS_CREATED_NUMBER:
                metric = new ProjectsCreatedNumberMetric();
                break;
            case PROJECTS_UNIQUE_BUILT_NUMBER:
                metric = new BuiltProjectsNumberMetric();
                break;
            case PROJECTS_UNIQUE_BUILT_PERCENT:
                metric = new BuiltUniqueProjectsPercentMetric();
                break;
            case PROJECTS_DESTROYED_NUMBER:
                metric = new ProjectsDestroyedNumberMetric();
                break;
            case TOTAL_PROJECTS_NUMBER:
                metric = new TotalProjectsMetric();
                break;
            case ACTIVE_PROJECTS_NUMBER:
                metric = new ActiveProjectsNumberMetric();
                break;
            case ACTIVE_PROJECTS_SET:
                metric = new ActiveProjectsSetMetric();
                break;
            case ACTIVE_PROJECTS_PERCENT:
                metric = new ActiveProjectsPercentMetric();
                break;
            case PROJECTS_CREATED_LIST:
                metric = new ProjectsCreatedListMetric();
                break;
            case PROJECTS_BUILT_LIST:
                metric = new ProjectsBuiltListMetric();
                break;
            case PROJECTS_BUILT_NUMBER:
                metric = new ProjectsBuiltNumberMetric();
                break;
            case PROJECTS_CREATED_TYPES:
                metric = new ProjectsCreatedTypesMetric();
                break;
            case PROJECT_TYPE_JAVA_JAR_NUMBER:
                metric = new ProjectCreatedTypeJavaJarNumberMetric();
                break;
            case PROJECT_TYPE_JAVA_JAR_PERCENT:
                metric = new ProjectCreatedTypeJavaJarPercentMetric();
                break;
            case PROJECT_TYPE_JAVA_WAR_NUMBER:
                metric = new ProjectCreatedTypeJavaWarNumberMetric();
                break;
            case PROJECT_TYPE_JAVA_WAR_PERCENT:
                metric = new ProjectCreatedTypeJavaWarPercentMetric();
                break;
            case PROJECT_TYPE_JAVA_JSP_NUMBER:
                metric = new ProjectCreatedTypeJavaJspNumberMetric();
                break;
            case PROJECT_TYPE_JAVA_JSP_PERCENT:
                metric = new ProjectCreatedTypeJavaJspPercentMetric();
                break;
            case PROJECT_TYPE_JAVA_SPRING_NUMBER:
                metric = new ProjectCreatedTypeJavaSpringNumberMetric();
                break;
            case PROJECT_TYPE_JAVA_SPRING_PERCENT:
                metric = new ProjectCreatedTypeJavaSpringPercentMetric();
                break;
            case PROJECT_TYPE_PHP_NUMBER:
                metric = new ProjectCreatedTypeJavaPhpNumberMetric();
                break;
            case PROJECT_TYPE_PHP_PERCENT:
                metric = new ProjectCreatedTypeJavaPhpPercentMetric();
                break;
            case PROJECT_TYPE_PYTHON_NUMBER:
                metric = new ProjectCreatedTypePythonNumberMetric();
                break;
            case PROJECT_TYPE_PYTHON_PERCENT:
                metric = new ProjectCreatedTypePythonPercentMetric();
                break;
            case PROJECT_TYPE_JAVASCRIPT_NUMBER:
                metric = new ProjectCreatedTypeJavaScriptNumberMetric();
                break;
            case PROJECT_TYPE_JAVASCRIPT_PERCENT:
                metric = new ProjectCreatedTypeJavaScriptPercentMetric();
                break;
            case PROJECT_TYPE_RUBY_NUMBER:
                metric = new ProjectCreatedTypeRubyNumberMetric();
                break;
            case PROJECT_TYPE_RUBY_PERCENT:
                metric = new ProjectCreatedTypeRubyPercentMetric();
                break;
            case PROJECT_TYPE_MMP_NUMBER:
                metric = new ProjectCreatedTypeMmpNumberMetric();
                break;
            case PROJECT_TYPE_MMP_PERCENT:
                metric = new ProjectCreatedTypeMmpPercentMetric();
                break;
            case PROJECT_TYPE_NODEJS_NUMBER:
                metric = new ProjectCreatedTypeNodejsNumberMetric();
                break;
            case PROJECT_TYPE_NODEJS_PERCENT:
                metric = new ProjectCreatedTypeNodejsPercentMetric();
                break;
            case PROJECT_TYPE_ANDROID_NUMBER:
                metric = new ProjectCreatedTypeAndroidNumberMetric();
                break;
            case PROJECT_TYPE_ANDROID_PERCENT:
                metric = new ProjectCreatedTypeAndroidPercentMetric();
                break;
            case PROJECT_TYPE_OTHERS_NUMBER:
                metric = new ProjectCreatedTypeOthersNumberMetric();
                break;
            case PROJECT_TYPE_OTHERS_PERCENT:
                metric = new ProjectCreatedTypeOthersPercentMetric();
                break;
            case PROJECTS_DEPLOYED_LIST:
                metric = new ProjectsDeployedListMetric();
                break;
            case PROJECTS_DEPLOYED_NUMBER:
                metric = new ProjectsDeployedNumberMetric();
                break;
            case PROJECTS_UNIQUE_DEPLOYED_NUMBER:
                metric = new ProjectsUniqueDeployedNumberMetric();
                break;
            case PROJECTS_UNIQUE_DEPLOYED_PERCENT:
                metric = new ProjectsUniqueDeployedPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPES:
                metric = new PaasDeploymentTypesMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_AWS_NUMBER:
                metric = new PaasDeploymentTypeAwsNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_AWS_PERCENT:
                metric = new PaasDeploymentTypeAwsPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_APPFOG_NUMBER:
                metric = new PaasDeploymentTypeAppFogNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_APPFOG_PERCENT:
                metric = new PaasDeploymentTypeAppFogPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_CLOUDFOUNDRY_NUMBER:
                metric = new PaasDeploymentTypeCloudFoundryNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_CLOUDFOUNDRY_PERCENT:
                metric = new PaasDeploymentTypeCloudFoundryPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_CLOUDBEES_NUMBER:
                metric = new PaasDeploymentTypeCloudBeesNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_CLOUDBEES_PERCENT:
                metric = new PaasDeploymentTypeCloudBeesPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_GAE_NUMBER:
                metric = new PaasDeploymentTypeGaeNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_GAE_PERCENT:
                metric = new PaasDeploymentTypeGaePercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_HEROKU_NUMBER:
                metric = new PaasDeploymentTypeHerokuNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_HEROKU_PERCENT:
                metric = new PaasDeploymentTypeHerokuPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_LOCAL_NUMBER:
                metric = new PaasDeploymentTypeLocalNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_LOCAL_PERCENT:
                metric = new PaasDeploymentTypeLocalPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_OPENSHIFT_NUMBER:
                metric = new PaasDeploymentTypeOpenShiftNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_OPENSHIFT_PERCENT:
                metric = new PaasDeploymentTypeOpenShiftPercentMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_TIER3_NUMBER:
                metric = new PaasDeploymentTypeTier3NumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_TIER3_PERCENT:
                metric = new PaasDeploymentTypeTier3PercentMetric();
                break;
            case JREBEL_USAGE_LIST:
                metric = new JRebelUsageListMetric();
                break;
            case JREBEL_USAGE_ELIGIBLE:
                metric = new JRebelUsageEligibleMetric();
                break;
            case JREBEL_USAGE_PERCENT:
                metric = new JRebelUsagePercentMetric();
                break;
            case JREBEL_USER_PROFILE_INFO_GATHERING:
                metric = new JrebelUserProfileInfoGatheringMetric();
                break;
            case INVITATIONS_SENT_LIST:
                metric = new InvitationsSentListMetric();
                break;
            case INVITATIONS_SENT_NUMBER:
                metric = new InvitationsSentNumberMetric();
                break;
            case INVITATIONS_ACCEPTED_PERCENT:
                metric = new InvitationsAcceptedPercentMetric();
                break;
            default:
                throw new IllegalArgumentException("Unknown metric type " + metricType);
        }

        Metric currentValue = metrics.putIfAbsent(metricType, metric);

        if (currentValue != null) {
            return currentValue;
        } else {
            return metric;
        }
    }

    /**
     * Creates new metric or returns existed one.
     */
    public static Metric createMetric(String name) throws IOException {
        MetricType metricType = MetricType.valueOf(name.toUpperCase());
        return createMetric(metricType);
    }
}
