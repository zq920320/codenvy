/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
var analytics = analytics || {};
analytics.configuration = new Configuration();

function Configuration() {
    var widgetConfiguration = {
        factoryStatistics: {
            presenterType: "ReportPresenter",
            modelViewName: "factory-timeline",
            isNeedToSaveInHistory: true
        },
        
        timeline: {
            presenterType: "ReportPresenter",
            modelViewName: "timeline",
            isNeedToSaveInHistory: true
        },
        
        topMetrics: {
            presenterType: "TopMetricsPresenter",
            isNeedToSaveInHistory: true
        },
        
        usersProfiles: {
            presenterType: "UsersProfilesPresenter",
            modelViewName: "users-profiles",
            modelMetricName: "users_profiles",
            isNeedToSaveInHistory: true,            
        },
        
        userOverview: {
            presenterType: "VerticalTablePresenter",
            modelViewName: "user_profile",
            isNeedToSaveInHistory: false
        },

        userData: {
            presenterType: "TablePresenter",
            modelViewName: "user_data", 
            isNeedToSaveInHistory: true
        },
        
        userSessions: {
            presenterType: "TablePresenter",
            modelViewName: "user-sessions",
            isNeedToSaveInHistory: true,
            
            isPaginable: true,
            modelMetricName: "product_usage_sessions",
            
            isSortable: true,
            defaultSortParams: "-date"
        },
        
        userWorkspaceData: {
            presenterType: "TablePresenter",
            modelViewName: "user_workspace_data",
            isNeedToSaveInHistory: true,
            
            isPaginable: true,
            modelMetricName: "users_time_in_workspaces"
        },
        
        userActivity: {
            presenterType: "TablePresenter",
            modelViewName: "user_activity",   
            isNeedToSaveInHistory: true,
            
            isPaginable: true,
            modelMetricName: "users_activity",
            
            isSortable: true,
            defaultSortParams: "-date"
         }
    }
    
    /** 1 x 1 relation(viewParams,modelParams) **/
    var mapViewParamNamesIntoModelParamNames = {
         "timeGroup": "time_unit",
         "Email": "user",
         "Domain": "domain",
         "Company": "user_company",
         "Organization": "org_id",
         "Affiliate": "affiliate_id",

         "Email": "_id",   
         "First Name": "user_first_name", 
         "Last Name": "user_last_name",
         "Company": "user_company",
         "Job": "user_job",
         
         "sort": "sort",
         "page": "page",
         
    }
    
    function getProperty(widgetName, property) {
        return widgetConfiguration[widgetName][property];
    }
    
    function getWidgetNames() {
        var widgetNames = new Array();
        for (var widgetName in widgetConfiguration) {
            widgetNames[widgetNames.length] = widgetName;
        }
        
        return widgetNames;
    }
    
    function getModelParamName(viewParamName) {
        return mapViewParamNamesIntoModelParamNames[viewParamName];
    }

    function getViewParamName(modelParamName) {
        for (var i in mapViewParamNamesIntoModelParamNames) {
            if (mapViewParamNamesIntoModelParamNames[i] == modelParamName) {
                return i;
            }
        }

        return undefined;
    }
    
    /** ****************** API ********** */
    return {
        getProperty: getProperty,
        getWidgetNames: getWidgetNames,
        getModelParamName: getModelParamName,
        getViewParamName: getViewParamName,
    }
}