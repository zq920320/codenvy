/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.io.IOException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MetricFactory {

    /**
     * Creates new metric or returns existed one.
     */
    public static Metric createMetric(MetricType metricType) throws IOException {
        switch (metricType) {
            case WORKSPACES_CREATED_NUMBER:
                return new WorkspacesCreatedNumberMetric();
            case WORKSPACES_DESTROYED_NUMBER:
                return new WorkspacesDestoryedNumberMetric();
            case TOTAL_WORKSPACES_NUMBER:
                return new TotalWorkspacesMetric();
            case ACTIVE_WORKSPACES_NUMBER:
                return new ActiveWorkspacesNumberMetric();
            case ACTIVE_WORKSPACES_SET:
                return new ActiveWorkspacesSetMetric();
            case ACTIVE_WORKSPACES_PERCENT:
                return new ActiveWorkspacesPercentMetric();
            case USERS_CREATED_SET:
                return new UsersCreatedSetMetric();
            case USERS_CREATED_NUMBER:
                return new UsersCreatedNumberMetric();
            case USERS_DESTROYED_NUMBER:
                return new UsersDestroyedNumberMetric();
            case TOTAL_USERS_NUMBER:
                return new TotalUsersMetric();
            case ACTIVE_USERS_NUMBER:
                return new ActiveUsersNumberMetric();
            case ACTIVE_USERS_SET:
                return new ActiveUsersSetMetric();
            case ACTIVE_USERS_PERCENT:
                return new ActiveUsersPercentMetric();
            case USERS_CREATED_PROJECTS_NUMBER:
                return new UsersCreatedProjectsNumberMetric();
            case USERS_CREATED_PROJECTS_PERCENT:
                return new UsersCreatedProjectsPercentMetric();
            case USERS_ADDED_TO_WORKSPACE:
                return new UsersAddedToWorkspaceMetric();
            case USERS_ADDED_TO_WORKSPACE_FROM_WEBSITE_PERCENT:
                return new UsersAddedToWsFromWebsitePercentMetric();
            case USERS_ADDED_TO_WORKSPACE_FROM_INVITE_PERCENT:
                return new UsersAddedToWsFromInvitePercentMetric();
            case USERS_SSO_LOGGED_IN:
                return new UsersSsoLoggedInMetric();
            case USERS_SSO_LOGGED_IN_USING_GOOGLE_PERCENT:
                return new UsersSsoLoggedInUsingGooglePercentMetric();
            case USERS_SSO_LOGGED_IN_USING_GITHUB_PERCENT:
                return new UsersSsoLoggedInUsingGithubPercentMetric();
            case USERS_SSO_LOGGED_IN_USING_FORM_PERCENT:
                return new UsersSsoLoggedInUsingFormPercentMetric();
            case PRODUCT_USAGE_TIME_TOTAL:
                return new ProductUsageTimeTotalMetric();
            case PROJECTS_CREATED_NUMBER:
                return new ProjectsCreatedNumberMetric();
            case BUILT_PROJECTS_NUMBER:
                return new BuiltProjectsNumberMetric();
            case BUILT_PROJECTS_PERCENT:
                return new BuiltProjectsPercentMetric();
            case PROJECTS_DESTROYED_NUMBER:
                return new ProjectsDestroyedNumberMetric();
            case TOTAL_PROJECTS_NUMBER:
                return new TotalProjectsMetric();
            case ACTIVE_PROJECTS_NUMBER:
                return new ActiveProjectsNumberMetric();
            case ACTIVE_PROJECTS_SET:
                return new ActiveProjectsSetMetric();
            case ACTIVE_PROJECTS_PERCENT:
                return new ActiveProjectsPercentMetric();
            case PROJECTS_CREATED_LIST:
                return new ProjectCreatedListMetric();
            case PROJECTS_CREATED_TYPES:
                return new ProjectsCreatedTypesMetric();
            case PROJECT_TYPE_JAVA_JAR_NUMBER:
                return new ProjectCreatedTypeJavaJarNumberMetric();
            case PROJECT_TYPE_JAVA_JAR_PERCENT:
                return new ProjectCreatedTypeJavaJarPercentMetric();
            case PROJECT_TYPE_JAVA_WAR_NUMBER:
                return new ProjectCreatedTypeJavaWarNumberMetric();
            case PROJECT_TYPE_JAVA_WAR_PERCENT:
                return new ProjectCreatedTypeJavaWarPercentMetric();
            case PROJECT_TYPE_JAVA_JSP_NUMBER:
                return new ProjectCreatedTypeJavaJspNumberMetric();
            case PROJECT_TYPE_JAVA_JSP_PERCENT:
                return new ProjectCreatedTypeJavaJspPercentMetric();
            case PROJECT_TYPE_JAVA_SPRING_NUMBER:
                return new ProjectCreatedTypeJavaSpringNumberMetric();
            case PROJECT_TYPE_JAVA_SPRING_PERCENT:
                return new ProjectCreatedTypeJavaSpringPercentMetric();
            case PROJECT_TYPE_PHP_NUMBER:
                return new ProjectCreatedTypeJavaPhpNumberMetric();
            case PROJECT_TYPE_PHP_PERCENT:
                return new ProjectCreatedTypeJavaPhpPercentMetric();
            case PROJECT_TYPE_PYTHON_NUMBER:
                return new ProjectCreatedTypePythonNumberMetric();
            case PROJECT_TYPE_PYTHON_PERCENT:
                return new ProjectCreatedTypePythonPercentMetric();
            case PROJECT_TYPE_JAVASCRIPT_NUMBER:
                return new ProjectCreatedTypeJavaScriptNumberMetric();
            case PROJECT_TYPE_JAVASCRIPT_PERCENT:
                return new ProjectCreatedTypeJavaScriptPercentMetric();
            case PROJECT_TYPE_RUBY_NUMBER:
                return new ProjectCreatedTypeRubyNumberMetric();
            case PROJECT_TYPE_RUBY_PERCENT:
                return new ProjectCreatedTypeRubyPercentMetric();
            case PROJECT_TYPE_MMP_NUMBER:
                return new ProjectCreatedTypeMmpNumberMetric();
            case PROJECT_TYPE_MMP_PERCENT:
                return new ProjectCreatedTypeMmpPercentMetric();
            case PROJECT_TYPE_NODEJS_NUMBER:
                return new ProjectCreatedTypeNodejsNumberMetric();
            case PROJECT_TYPE_NODEJS_PERCENT:
                return new ProjectCreatedTypeNodejsPercentMetric();
            case PROJECT_TYPE_OTHERS_NUMBER:
                return new ProjectCreatedTypeOthersNumberMetric();
            case PROJECT_TYPE_OTHERS_PERCENT:
                return new ProjectCreatedTypeOthersPercentMetric();
            case APP_DEPLOYED_LIST:
                return new AppDeployedListMetric();
            case APP_DEPLOYED_NUMBER:
                return new AppDeployedNumberMetric();
            case PAAS_DEPLOYMENT_TYPES:
                return new PaasDeploymentTypesMetric();
            case PAAS_DEPLOYMENT_TYPE_AWS_NUMBER:
                return new PaasDeploymentTypeAwsNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_AWS_PERCENT:
                return new PaasDeploymentTypeAwsPercentMetric();
            case PAAS_DEPLOYMENT_TYPE_APPFOG_NUMBER:
                return new PaasDeploymentTypeAppFogNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_APPFOG_PERCENT:
                return new PaasDeploymentTypeAppFogPercentMetric();
            case PAAS_DEPLOYMENT_TYPE_CLOUDFOUNDRY_NUMBER:
                return new PaasDeploymentTypeCloudFoundryNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_CLOUDFOUNDRY_PERCENT:
                return new PaasDeploymentTypeCloudFoundryPercentMetric();
            case PAAS_DEPLOYMENT_TYPE_CLOUDBEES_NUMBER:
                return new PaasDeploymentTypeCloudBeesNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_CLOUDBEES_PERCENT:
                return new PaasDeploymentTypeCloudBeesPercentMetric();
            case PAAS_DEPLOYMENT_TYPE_GAE_NUMBER:
                return new PaasDeploymentTypeGaeNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_GAE_PERCENT:
                return new PaasDeploymentTypeGaePercentMetric();
            case PAAS_DEPLOYMENT_TYPE_HEROKU_NUMBER:
                return new PaasDeploymentTypeHerokuNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_HEROKU_PERCENT:
                return new PaasDeploymentTypeHerokuPercentMetric();
            case PAAS_DEPLOYMENT_TYPE_LOCAL_NUMBER:
                return new PaasDeploymentTypeLocalNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_LOCAL_PERCENT:
                return new PaasDeploymentTypeLocalPercentMetric();
            case PAAS_DEPLOYMENT_TYPE_OPENSHIFT_NUMBER:
                return new PaasDeploymentTypeOpenShiftNumberMetric();
            case PAAS_DEPLOYMENT_TYPE_OPENSHIFT_PERCENT:
                return new PaasDeploymentTypeOpenShiftPercentMetric();
            case PAAS_DEPLOYMENT_TYPE_TIER3_NUMBER:
                return new PaasDeploymentTypeTier3NumberMetric();
            case PAAS_DEPLOYMENT_TYPE_TIER3_PERCENT:
                return new PaasDeploymentTypeTier3PercentMetric();
            case JREBEL_ELIGIBLE:
                return new JrebelElibigleMetric();
            case JREBEL_USAGE:
                return new JrebelUsageMetric();
            case JREBEL_USAGE_PERCENT:
                return new JrebelUsagePercentMetric();
            case INVITATIONS_SENT:
                return new InvitationsSentMetric();
            case INVITATIONS_ACCEPTED_PERCENT:
                return new InvitationsAcceptedPercentMetric();

            default:
                throw new IllegalArgumentException("Unknown metric type " + metricType);
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
