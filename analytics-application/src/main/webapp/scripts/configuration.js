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
        /** Reports */
        analysis: {
            presenterType: "ReportPresenter",
            modelViewName: "analysis",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: false,
        },
        
        factoryStatistics: {
            presenterType: "ReportPresenter",
            modelViewName: "factory-timeline",

            defaultModelParams: {
                "time_unit": "day"
            },

            isNeedToSaveInHistory: true,
        },

        summaryReport: {
            presenterType: "ReportPresenter",
            modelViewName: "summary_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },

        workspaceReport: {
            presenterType: "ReportPresenter",
            modelViewName: "workspace_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },

        userReport: {
            presenterType: "ReportPresenter",
            modelViewName: "user_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },

        engagementLevels: {
            presenterType: "ReportPresenter",
            modelViewName: "engagement_levels",
            
            forbiddenModelParams: ["time_unit"],
            
            isNeedToSaveInHistory: true,
        },
        
        collaborationReport: {
            presenterType: "ReportPresenter",
            modelViewName: "collaboration_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },        
        
        usageReport: {
            presenterType: "ReportPresenter",
            modelViewName: "usage_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },
        
        sessionReport: {
            presenterType: "ReportPresenter",
            modelViewName: "session_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },

        projectReport: {
            presenterType: "ReportPresenter",
            modelViewName: "project_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },        
        
        topMetrics: {
            presenterType: "TopMetricsPresenter",

            defaultModelParams: {
                "time_unit": "1day",
                "metric": "top_factory_sessions"
            },

            isNeedToSaveInHistory: true,
        },

        /** for User View */
        usersProfiles: {
            presenterType: "UsersProfilesPresenter",
            modelViewName: "users_profiles",
            modelMetricName: "users_profiles",
            isNeedToSaveInHistory: true,
            firstColumnLinkPrefix: "/analytics/pages/user-view.jsp?user",
        },

        userOverview: {
            widgetLabel: "User Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "user_profile",
            isNeedToSaveInHistory: false,
        },

        userData: {
            presenterType: "TablePresenter",
            modelViewName: "user_data",
            isNeedToSaveInHistory: true,
        },

        userSessions: {
            presenterType: "TablePresenter",
            modelViewName: "user_sessions",
            isNeedToSaveInHistory: true,

            isPaginable: true,
            modelMetricName: "product_usage_sessions",

            isSortable: true,
            defaultSortParams: "-date",
            
            firstColumnLinkPrefix: "/analytics/pages/session-view.jsp?event=~session-started%2C~session-finished%2C~session-factory-started%2C~session-factory-stopped&session_id",
        },

        userWorkspaceList: {
            presenterType: "TablePresenter",
            modelViewName: "user_workspace_list",
            isNeedToSaveInHistory: true,

            isPaginable: true,
            modelMetricName: "usage_time_by_workspaces",
            
            firstColumnLinkPrefix: "/analytics/pages/workspace-view.jsp?ws",
        },

        userActivity: {
            presenterType: "TablePresenter",
            modelViewName: "user_activity",
            isNeedToSaveInHistory: true,
            
            isPaginable: true,
            modelMetricName: "users_activity",

            isSortable: true,
            defaultSortParams: "-date",
        },

        userEvents: {
            presenterType: "TablePresenter",
            modelViewName: "users_events",
            isNeedToSaveInHistory: true,
        },
        

        /** for Session View */
        sessionOverview: {
            widgetLabel: "Session Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "session_overview",
            isNeedToSaveInHistory: false,
            
            defaultModelParams: {
                "session_id": "unexisted_session_id"
            },
        },

        userSessionActivity: {
            presenterType: "TablePresenter",
            modelViewName: "session_events",
            isNeedToSaveInHistory: false,

            defaultModelParams: {
                "session_id": "unexisted_session_id",
            },
            
            isPaginable: true,
            modelMetricName: "users_activity",
            onePageRowsCount: 30,

            isSortable: true,
            defaultSortParams: "+date",
        },        

        /** for Workspace View */
        workspaces: {
            presenterType: "WorkspacesPresenter",
            modelViewName: "workspaces",
            modelMetricName: "total_workspaces",
            isNeedToSaveInHistory: true,
            firstColumnLinkPrefix: "/analytics/pages/workspace-view.jsp?ws",
        },

        workspaceOverview: {
            widgetLabel: "Workspace Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "workspace_overview",
            isNeedToSaveInHistory: false,
        },

        workspaceSessions: {
            presenterType: "TablePresenter",
            modelViewName: "workspace_sessions",
            isNeedToSaveInHistory: true,

            isPaginable: true,
            modelMetricName: "product_usage_sessions",

            isSortable: true,
            defaultSortParams: "-date",
            
            firstColumnLinkPrefix: "/analytics/pages/session-view.jsp?event=~session-started%2C~session-finished%2C~session-factory-started%2C~session-factory-stopped&session_id",
        },

        workspaceUserList: {
            presenterType: "TablePresenter",
            modelViewName: "workspace_user_list",
            isNeedToSaveInHistory: true,

            isPaginable: true,
            modelMetricName: "usage_time_by_workspaces",
            
            firstColumnLinkPrefix: "/analytics/pages/user-view.jsp?user",
        },

        workspaceActivity: {
            presenterType: "TablePresenter",
            modelViewName: "workspace_activity",
            isNeedToSaveInHistory: true,
            
            isPaginable: true,
            modelMetricName: "users_activity",

            isSortable: true,
            defaultSortParams: "-date",
        },

        workspaceUserEvents: {
            presenterType: "TablePresenter",
            modelViewName: "workspace_users_events",
            isNeedToSaveInHistory: true,
        },
    }

    var registeredModelParams = [
        "time_unit",
        "user",
        "_id",
        "domain",
        "user_company",
        "org_id",
        "affiliate_id",
        "user_first_name",
        "user_last_name",
        "sort",
        "page",
        "session_id",
        "ide",
        "metric",
        "from_date",
        "to_date",
        "event",
        "ws",
    ];

    /** see method analytics.main.getParamsFromButtons()    */
    var registeredViewParams = [
        "user",              // factory-statistics, *-reports
        "domain",            // *-reports
        "user_company",      // users-profiles, *-reports
        "org_id",            // factory-statistics
        "affiliate_id",      // factory-statistics
        "user_first_name",   // users-profiles
        "user_last_name",    // users-profiles
        "ide",               // top-menu
        "metric",            // top-metrics
        "from_date",         // user-view, workspace-view
        "to_date",           // user-view, workspace-view
        "event",             // session-view
        "ws",                // workspaces-view
        "session_id",        // workspaces-view
    ];
    
    /** url params which are passed from other pages */
    var crossPageParams = [
        "user",        // users-profiles > user-view; workspace-view > user-view
        "sort",        // users-profiles > users-profiles; workspaces-view > workspaces-view
        "page",        // user-view > user-view; users-profiles > users-profiles; workspaces-view > workspaces-view; workspace-view > workspace-view
        "session_id",  // user-view > session-view; workspace-view > session-view
        "ws",          // workspaces-view > workspace-view; user-view > workspace-view
    ];
    
    var globalParams = [
        "ide",
    ];
    
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

    /**
     * Add model params which are undefined in modelParams and defined in widgetConfiguration[widgetName]["defaultModelParams"] property
     */
    function setupDefaultModelParams(widgetName, modelParams) {
        var defaultModelParams = widgetConfiguration[widgetName]["defaultModelParams"];

        if (typeof defaultModelParams != "undefined") {
            for (var paramName in defaultModelParams) {
                if (typeof modelParams[paramName] == "undefined") {
                    modelParams[paramName] = defaultModelParams[paramName];
                }
            }
        }

        return modelParams;
    }

    /**
     * Verify if modelParam is presence in registeredModelParams array
     */
    function isParamRegistered(modelParam) {
        for (var i in registeredModelParams) {
            if (modelParam == registeredModelParams[i]) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Verify if param is global
     */
    function isParamGlobal(param) {
        for (var i in globalParams) {
            if (param == globalParams[i]) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Return array with global param names
     */
    function getGlobalParamList() {
        return globalParams;
    }
    
    /**
     * Return registered params with all values = null
     */
    function getViewParamsWithNullValues() {
        var params = {};
        for (var i in registeredViewParams) {
            var paramName = registeredViewParams[i];
            params[paramName] = null;
        }
        
        return params;
    }
    
    /**
     * Return modelParams which had been cleared from forbidden params defined in the configuration of widget with widgetName
     */
    function removeForbiddenModelParams(widgetName, modelParams) {
        var forbiddenModelParams = widgetConfiguration[widgetName]["forbiddenModelParams"];  

        if (typeof forbiddenModelParams != "undefined") {
            for (var i in forbiddenModelParams) {
                var forbiddenModelParam = forbiddenModelParams[i];
                if (typeof modelParams[forbiddenModelParam] != "undefined") {
                    delete modelParams[forbiddenModelParam];
                }
            }
        }

        return modelParams;
    }
    
    function getCrossPageParamsList() {
        return crossPageParams;
    }
    
    /** ****************** API ********** */
    return {
        getProperty: getProperty,
        getWidgetNames: getWidgetNames,
        setupDefaultModelParams: setupDefaultModelParams,
        isParamRegistered: isParamRegistered,
        isParamGlobal: isParamGlobal,
        getGlobalParamList: getGlobalParamList,
        getViewParamsWithNullValues: getViewParamsWithNullValues,
        removeForbiddenModelParams: removeForbiddenModelParams,
        getCrossPageParamsList: getCrossPageParamsList, 
    }
}