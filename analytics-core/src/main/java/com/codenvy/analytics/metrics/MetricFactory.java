/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */

package com.codenvy.analytics.metrics;

import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class MetricFactory {

    private static ConcurrentHashMap<MetricType, Metric> metrics = new ConcurrentHashMap<>();

    /** Creates new metric or returns existed one. */
    public static Metric createMetric(MetricType metricType) {
        if (metrics.contains(metricType)) {
            return metrics.get(metricType);
        }

        Metric metric;
        switch (metricType) {
            case FILE_MANIPULATION:
                metric = new FileManipulationMetric();
                break;
            case TENANT_CREATED:
                metric = new TenantCreatedMetric();
                break;
            case TENANT_DESTROYED:
                metric = new TenantDestroyedMetric();
                break;
            case TOTAL_WORKSPACES:
                metric = new TotalWorkspacesMetric();
                break;
            case USER_REMOVED:
                metric = new UserRemovedMetric();
                break;
            case USER_CREATED:
                metric = new UserCreatedMetric();
                break;
            case USERS_SENT_INVITE_ONCE:
                metric = new UsersInvitationsSentNumberMetric();
                break;
            case TOTAL_USERS:
                metric = new TotalUsersMetric();
                break;
            case ACTIVE_USERS_SET:
                metric = new ActiveUsersSetMetric();
                break;
            case ACTIVE_WS_SET:
                metric = new ActiveWsSetMetric();
                break;
            case ACTIVE_USERS:
                metric = new ActiveUsersNumberMetric();
                break;
            case ACTIVE_WS:
                metric = new ActiveWsNumberMetric();
                break;
            case USERS_CREATED_PROJECT_ONCE:
                metric = new UsersCreatedProjectOnceMetric();
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
            case USER_ADDED_TO_WORKSPACE:
                metric = new UserAddedToWsMetric();
                break;
            case USER_ADDED_TO_WORKSPACE_INVITE:
                metric = new UserAddedToWsInviteMetric();
                break;
            case USERS_SHELL_LAUNCHED_LIST:
                metric = new UsersShellLaunchedListMetric();
                break;
            case USERS_SHELL_LAUNCHED_NUMBER:
                metric = new UsersShellLaunchedNumberMetric();
                break;
            case USER_SSO_LOGGED_IN:
                metric = new UserSSOLoggedInMetric();
                break;
            case USER_LOGIN_GITHUB:
                metric = new UserLoginGithubMetric();
                break;
            case USER_LOGIN_FORM:
                metric = new UserLoginFormMetric();
                break;
            case USER_LOGIN_GOOGLE:
                metric = new UserLoginGoogleMetric();
                break;
            case USER_LOGIN_TOTAL:
                metric = new UserLoginTotalMetric();
                break;
            case USER_LOGIN_GITHUB_PERCENT:
                metric = new UserLoginGithubPercentMetric();
                break;
            case USER_LOGIN_FORM_PERCENT:
                metric = new UserLoginFormPercentMetric();
                break;
            case USER_LOGIN_GOOGLE_PERCENT:
                metric = new UserLoginGooglePercentMetric();
                break;
            case USER_CODE_REFACTOR:
                metric = new UserCodeRefactorMetric();
                break;
            case USER_CODE_COMPLETE:
                metric = new UserCodeCompleteMetric();
                break;
            case USER_PROFILE:
                metric = new UsersProfileMetric();
                break;
            case USER_ACTIVITY:
                metric = new UsersActivityMetric();
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
            case PRODUCT_USAGE_TIME_0_10:
                metric = new ProductUsageTime010Metric();
                break;
            case PRODUCT_USAGE_TIME_TOTAL:
                metric = new ProductUsageTimeTotalMetric();
                break;
            case PRODUCT_USAGE_TIME_10_60:
                metric = new ProductUsageTime1060Metric();
                break;
            case PRODUCT_USAGE_TIME_60_MORE:
                metric = new ProductUsageTime60MoreMetric();
                break;
            case PRODUCT_USAGE_TIME:
                metric = new ProductUsageTimeMetric();
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
            case PRODUCT_USAGE_SESSIONS_0_10:
                metric = new ProductUsageSessions010Metric();
                break;
            case PRODUCT_USAGE_SESSIONS_10_60:
                metric = new ProductUsageSessions1060Metric();
                break;
            case PRODUCT_USAGE_SESSIONS_60_MORE:
                metric = new ProductUsageSessions60MoreMetric();
                break;
            case PRODUCT_USAGE_SESSIONS_TOTAL:
                metric = new ProductUsageSessionsTotalMetric();
                break;
            case PROJECT_CREATED_TYPES:
                metric = new ProjectsCreatedTypesMetric();
                break;
            case PROJECT_CREATED:
                metric = new ProjectsCreatedMetric();
                break;
            case PROJECT_DESTROYED:
                metric = new ProjectDestroyedMetric();
                break;
            case TOTAL_PROJECTS:
                metric = new TotalProjectsMetric();
                break;
            case PROJECTS_BUILT_LIST:
                metric = new ProjectsBuiltListMetric();
                break;
            case PROJECTS_BUILT_NUMBER:
                metric = new ProjectsBuiltNumberMetric();
                break;
            case BUILD_STARTED:
                metric = new BuildStartedMetric();
                break;
            case RUN_STARTED:
                metric = new RunStartedMetric();
                break;
            case DEBUG_STARTED:
                metric = new DebugStarted();
                break;
            case PROJECT_TYPE_JAR:
                metric = new ProjectCreatedTypeJarMetric();
                break;
            case PROJECT_TYPE_WAR:
                metric = new ProjectCreatedWarNumber();
                break;
            case PROJECT_TYPE_JSP:
                metric = new ProjectCreatedTypeJspMetric();
                break;
            case PROJECT_TYPE_SPRING:
                metric = new ProjectCreatedSpringMetric();
                break;
            case PROJECT_TYPE_PHP:
                metric = new ProjectCreatedPhpMetric();
                break;
            case PROJECT_TYPE_PYTHON:
                metric = new ProjectCreatedPythonMetric();
                break;
            case PROJECT_TYPE_JAVASCRIPT:
                metric = new ProjectCreatedJavaScriptMetric();
                break;
            case PROJECT_TYPE_RUBY:
                metric = new ProjectCreatedRuby();
                break;
            case PROJECT_TYPE_MMP:
                metric = new ProjectCreatedMMPMetric();
                break;
            case PROJECT_TYPE_NODEJS:
                metric = new ProjectCreatedNodejsMetric();
                break;
            case PROJECT_TYPE_ANDROID:
                metric = new ProjectCreatedAndroidMetric();
                break;
            case PROJECT_TYPE_OTHERS:
                metric = new ProjectCreatedOthersMetric();
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
            case USER_INVITE:
                metric = new UserInviteMetric();
                break;
            case PROJECT_CREATED_USER_ACTIVE:
                metric = new ProjectCreatedUserActiveMetric();
                break;
            case USER_INVITE_ACTIVE:
                metric = new UserInviteActiveMetric();
                break;
            case USER_ACCEPT_INVITE:
                metric = new UserAcceptInviteMetric();
                break;
            case USER_ACCEPT_INVITE_PERCENT:
                metric = new UserAcceptInvitePercentMetric();
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

    /** Creates new metric or returns existed one. */
    public static Metric createMetric(String name) {
        MetricType metricType = MetricType.valueOf(name.toUpperCase());
        return createMetric(metricType);
    }
}
