/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class MetricFactory {

    private static ConcurrentHashMap<MetricType, Metric> metrics = new ConcurrentHashMap<>();

    /**
     * Creates new metric or returns existed one.
     */
    public static Metric createMetric(MetricType metricType) {
        if (metrics.contains(metricType)) {
            return metrics.get(metricType);
        }

        Metric metric;
        switch (metricType) {
            case WORKSPACES_CREATED_NUMBER:
                metric = new WorkspacesCreatedNumberMetric();
                break;
            case WORKSPACES_CREATED_LIST:
                metric = new WorkspacesCreatedListMetric();
                break;
            case WORKSPACES_DESTROYED_NUMBER:
                metric = new WorkspacesDestoryedNumberMetric();
                break;
            case WORKSPACES_DESTROYED_LIST:
                metric = new WorkspacesDestroyedListMetric();
                break;
            case TOTAL_WORKSPACES_NUMBER:
                metric = new TotalWorkspacesMetric();
                break;
            case ACTIVE_WORKSPACES_NUMBER:
                metric = new ActiveWorkspacesNumberMetric();
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
            case USERS_INVITATIONS_SENT_NUMBER:
                metric = new UsersInvitationsSentNumberMetric();
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
            case ACTIVE_USERS_WORKAPCES_LIST:
                metric = new ActiveUsersWorkspacesListMetric();
                break;
            case USERS_CREATED_PROJECTS_NUMBER:
                metric = new UsersCreatedProjectsNumberMetric();
                break;
            case USERS_BUILT_PROJECTS_NUMBER:
                metric = new UsersBuiltProjectsNumberMetric();
                break;
            case USERS_DEPLOYED_PROJECTS_NUMBER:
                metric = new UsersDeployedProjectsNumberMetric();
                break;
            case USERS_DEPLOYED_PAAS_PROJECTS_NUMBER:
                metric = new UsersDeployedPaasProjectsNumberMetric();
                break;
            case USERS_ADDED_TO_WORKSPACE_LIST:
                metric = new UsersAddedToWorkspaceListMetric();
                break;
            case USERS_SSO_LOGGED_IN_LIST:
                metric = new UsersSsoLoggedInListMetric();
                break;
            case USERS_SSO_LOGGED_IN_TYPES:
                metric = new UsersSsoLoggedInTypesMetric();
                break;
            case USERS_SHELL_LAUNCHED_LIST:
                metric = new UsersShellLaunchedListMetric();
                break;
            case USERS_SHELL_LAUNCHED_NUMBER:
                metric = new UsersShellLaunchedNumberMetric();
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
            case USER_PROFILE:
                metric = new UsersProfileMetric();
                break;
            case USER_ACTIVITY:
                metric = new UsersActivityMetric();
                break;
            case USER_SESSIONS:
                metric = new UsersSessionsMetric();
                break;
            case USER_PROFILE_FIRSTNAME:
                metric = new UsersProfileFirstNameMetric();
                break;
            case USER_PROFILE_LASTNAME:
                metric = new UsersProfileLastNameMetric();
                break;
            case USER_PROFILE_EMAIL:
                metric = new UsersProfileEmailMetric();
                break;
            case USER_PROFILE_COMPANY:
                metric = new UsersProfileCompanyMetric();
                break;
            case USER_PROFILE_PHONE:
                metric = new UsersProfilePhoneMetric();
                break;
            case USERS_SEGMENT_ANALYSIS_CONDITION_1:
                metric = new UsersSegmentAnalysisCondition1();
                break;
            case USERS_SEGMENT_ANALYSIS_CONDITION_2:
                metric = new UsersSegmentAnalysisCondition2();
                break;
            case USERS_SEGMENT_ANALYSIS_CONDITION_3:
                metric = new UsersSegmentAnalysisCondition3();
                break;
            case USERS_UPDATE_PROFILE_LIST:
                metric = new UsersUpdateProfileList();
                break;
            case USERS_COMPLETED_PROFILE:
                metric = new UsersCompletedProfile();
                break;
            case PRODUCT_USAGE_TIME_TOTAL:
                metric = new ProductUsageTimeTotalMetric();
                break;
            case PRODUCT_USAGE_TIME_0_10:
                metric = new ProductUsageTime0_10Metric();
                break;
            case PRODUCT_USAGE_TIME_10_60:
                metric = new ProductUsageTime10_60Metric();
                break;
            case PRODUCT_USAGE_TIME_60_MORE:
                metric = new ProductUsageTime60_MoreMetric();
                break;
            case PRODUCT_USAGE_TIME_LIST:
                metric = new ProductUsageTimeListlMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_USERS_BY_1DAY:
                metric = new ProductUsageTimeTopUsersBy1DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_USERS_BY_7DAY:
                metric = new ProductUsageTimeTopUsersBy7DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_USERS_BY_30DAY:
                metric = new ProductUsageTimeTopUsersBy30DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_USERS_BY_60DAY:
                metric = new ProductUsageTimeTopUsersBy60DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_USERS_BY_90DAY:
                metric = new ProductUsageTimeTopUsersBy90DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_USERS_BY_365DAY:
                metric = new ProductUsageTimeTopUsersBy365DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_USERS_BY_LIFETIME:
                metric = new ProductUsageTimeTopUsersByLifeTimeMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_1DAY:
                metric = new ProductUsageTimeTopCompaniesBy1DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_7DAY:
                metric = new ProductUsageTimeTopCompaniesBy7DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_30DAY:
                metric = new ProductUsageTimeTopCompaniesBy30DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_60DAY:
                metric = new ProductUsageTimeTopCompaniesBy60DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_90DAY:
                metric = new ProductUsageTimeTopCompaniesBy90DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_365DAY:
                metric = new ProductUsageTimeTopCompaniesBy365DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_COMPANIES_BY_LIFETIME:
                metric = new ProductUsageTimeTopCompaniesByLifeTimeMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_1DAY:
                metric = new ProductUsageTimeTopDomainsBy1DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_7DAY:
                metric = new ProductUsageTimeTopDomainsBy7DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_30DAY:
                metric = new ProductUsageTimeTopDomainsBy30DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_60DAY:
                metric = new ProductUsageTimeTopDomainsBy60DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_90DAY:
                metric = new ProductUsageTimeTopDomainsBy90DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_365DAY:
                metric = new ProductUsageTimeTopDomainsBy365DayMetric();
                break;
            case PRODUCT_USAGE_TIME_TOP_DOMAINS_BY_LIFETIME:
                metric = new ProductUsageTimeTopDomainsByLifeTimeMetric();
                break;
            case PRODUCT_USAGE_USER_SESSIONS_NUMBER:
                metric = new ProductUsageUserSessionsNumberMetric();
                break;
            case PRODUCT_USAGE_USER_SESSIONS_NUMBER_0_10:
                metric = new ProductUsageUserSessionsNumber0_10Metric();
                break;
            case PRODUCT_USAGE_USER_SESSIONS_NUMBER_10_60:
                metric = new ProductUsageUserSessionsNumber10_60Metric();
                break;
            case PRODUCT_USAGE_USER_SESSIONS_NUMBER_60_MORE:
                metric = new ProductUsageUserSessionsNumber60_MoreMetric();
                break;
            case PROJECTS_CREATED_NUMBER:
                metric = new ProjectsCreatedNumberMetric();
                break;
            case PROJECTS_DESTROYED_LIST:
                metric = new ProjectsDestroyedListMetric();
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
            case ACTIVE_PROJECTS_LIST:
                metric = new ActiveProjectsListMetric();
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
            case PROJECT_TYPE_JAVA_WAR_NUMBER:
                metric = new ProjectCreatedTypeJavaWarNumberMetric();
                break;
            case PROJECT_TYPE_JAVA_JSP_NUMBER:
                metric = new ProjectCreatedTypeJavaJspNumberMetric();
                break;
            case PROJECT_TYPE_JAVA_SPRING_NUMBER:
                metric = new ProjectCreatedTypeJavaSpringNumberMetric();
                break;
            case PROJECT_TYPE_PHP_NUMBER:
                metric = new ProjectCreatedTypeJavaPhpNumberMetric();
                break;
            case PROJECT_TYPE_PYTHON_NUMBER:
                metric = new ProjectCreatedTypePythonNumberMetric();
                break;
            case PROJECT_TYPE_JAVASCRIPT_NUMBER:
                metric = new ProjectCreatedTypeJavaScriptNumberMetric();
                break;
            case PROJECT_TYPE_RUBY_NUMBER:
                metric = new ProjectCreatedTypeRubyNumberMetric();
                break;
            case PROJECT_TYPE_MMP_NUMBER:
                metric = new ProjectCreatedTypeMmpNumberMetric();
                break;
            case PROJECT_TYPE_NODEJS_NUMBER:
                metric = new ProjectCreatedTypeNodejsNumberMetric();
                break;
            case PROJECT_TYPE_ANDROID_NUMBER:
                metric = new ProjectCreatedTypeAndroidNumberMetric();
                break;
            case PROJECT_TYPE_OTHERS_NUMBER:
                metric = new ProjectCreatedTypeOthersNumberMetric();
                break;
            case PROJECTS_DEPLOYED_LIST:
                metric = new ProjectsDeployedListMetric();
                break;
            case PROJECTS_DEPLOYED_LOCAL_LIST:
                metric = new ProjectsDeployedLocalListMetric();
                break;
            case PROJECTS_DEPLOYED_PAAS_LIST:
                metric = new ProjectsDeployedPaasListMetric();
                break;
            case PROJECTS_DEPLOYED_NUMBER:
                metric = new ProjectsDeployedNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPES:
                metric = new PaasDeploymentTypesMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_AWS_NUMBER:
                metric = new PaasDeploymentTypeAwsNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_APPFOG_NUMBER:
                metric = new PaasDeploymentTypeAppFogNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_CLOUDFOUNDRY_NUMBER:
                metric = new PaasDeploymentTypeCloudFoundryNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_CLOUDBEES_NUMBER:
                metric = new PaasDeploymentTypeCloudBeesNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_GAE_NUMBER:
                metric = new PaasDeploymentTypeGaeNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_HEROKU_NUMBER:
                metric = new PaasDeploymentTypeHerokuNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_LOCAL_NUMBER:
                metric = new PaasDeploymentTypeLocalNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_OPENSHIFT_NUMBER:
                metric = new PaasDeploymentTypeOpenShiftNumberMetric();
                break;
            case PAAS_DEPLOYMENT_TYPE_TIER3_NUMBER:
                metric = new PaasDeploymentTypeTier3NumberMetric();
                break;
            case JREBEL_USAGE_LIST:
                metric = new JRebelUsageListMetric();
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
    public static Metric createMetric(String name) {
        MetricType metricType = MetricType.valueOf(name.toUpperCase());
        return createMetric(metricType);
    }
}
