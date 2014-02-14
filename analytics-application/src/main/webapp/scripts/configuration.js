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

            defaultModelParams: {
                "time_unit": "day"
            },

            isNeedToSaveInHistory: true,
        },

        summaryReport: {
            presenterType: "TablePresenter",
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

        usersReport: {
            presenterType: "TablePresenter",
            modelViewName: "users_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },

        engagementLevels: {
            presenterType: "TablePresenter",
            modelViewName: "engagement_levels",
            isNeedToSaveInHistory: true,
        },
        
        collaborationReport: {
            presenterType: "TablePresenter",
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
        
        sessionsReport: {
            presenterType: "ReportPresenter",
            modelViewName: "sessions_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: true,
        },

        projectsReport: {
            presenterType: "ReportPresenter",
            modelViewName: "projects_report",

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

        usersProfiles: {
            presenterType: "UsersProfilesPresenter",
            modelViewName: "users-profiles",
            modelMetricName: "users_profiles",
            isNeedToSaveInHistory: true,
            firstColumnLinkPrefix: "user-view.jsp?user",
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
            modelViewName: "user-sessions",
            isNeedToSaveInHistory: true,

            isPaginable: true,
            modelMetricName: "product_usage_sessions",

            isSortable: true,
            defaultSortParams: "-date",
            
            firstColumnLinkPrefix: "session-view.jsp?session_id",
        },

        userWorkspaceData: {
            presenterType: "TablePresenter",
            modelViewName: "user_workspace_data",
            isNeedToSaveInHistory: true,

            isPaginable: true,
            modelMetricName: "users_time_in_workspaces",
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
            isNeedToSaveInHistory: true
        },

        analysis: {
            presenterType: "ReportPresenter",
            modelViewName: "analysis",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: false,
        },

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
            isNeedToSaveInHistory: true,

            defaultModelParams: {
                "session_id": "unexisted_session_id"
            },
            
            isPaginable: true,
            modelMetricName: "users_activity",
            onePageRowsCount: 30,

            isSortable: true,
            defaultSortParams: "-date",
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
    
    /** ****************** API ********** */
    return {
        getProperty: getProperty,
        getWidgetNames: getWidgetNames,
        setupDefaultModelParams: setupDefaultModelParams,
        isParamRegistered: isParamRegistered,
        isParamGlobal: isParamGlobal,
        getGlobalParamList: getGlobalParamList,
    }
}