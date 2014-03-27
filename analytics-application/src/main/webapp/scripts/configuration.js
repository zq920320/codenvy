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
        signupAnalysis: {
            widgetLabel: "Signup Analysis",
            presenterType: "ReportPresenter",
            modelViewName: "analysis",

            defaultModelParams: {
                "time_unit": "month"
            },

            isNeedToSaveInHistory: false,   // default value = true
        },

        factoryStatistics: {
            widgetLabel: "Factory Statistics",
            presenterType: "ReportPresenter",
            modelViewName: "factory-timeline",

            defaultModelParams: {
                "time_unit": "day"
            },

        },

        summaryReport: {
            widgetLabel: "Summary Report",
            presenterType: "ReportPresenter",
            modelViewName: "summary_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "columnsWithoutSorting": [1,2,3,4,5,6,7,8,9,10,11,12,13,14]
            },
        },

        workspaceReport: {
            widgetLabel: "Workspace Report",
            presenterType: "ReportPresenter",
            modelViewName: "workspace_report",

            defaultModelParams: {
                "time_unit": "month"
            },
        },

        userReport: {
            widgetLabel: "User Report",
            presenterType: "ReportPresenter",
            modelViewName: "user_report",

            defaultModelParams: {
                "time_unit": "month"
            },
        },

        engagementLevels: {
            widgetLabel: "Engagement Levels",
            presenterType: "ReportPresenter",
            modelViewName: "engagement_levels",

            forbiddenModelParams: ["time_unit"],
        },

        collaborationReport: {
            widgetLabel: "Collaboration Report",
            presenterType: "ReportPresenter",
            modelViewName: "collaboration_report",

            defaultModelParams: {
                "time_unit": "month"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "columnsWithoutSorting": [1,2,3,4,5,6,7,8,9,10,11,12,13,14]
            },
        },

        usageReport: {
            widgetLabel: "Usage Report",
            presenterType: "ReportPresenter",
            modelViewName: "usage_report",

            defaultModelParams: {
                "time_unit": "month"
            },
        },

        sessionReport: {
            widgetLabel: "Session Report",
            presenterType: "ReportPresenter",
            modelViewName: "session_report",

            defaultModelParams: {
                "time_unit": "month"
            },
        },

        projectReport: {
            widgetLabel: "Project Report",
            presenterType: "ReportPresenter",
            modelViewName: "project_report",

            defaultModelParams: {
                "time_unit": "month"
            },
        },

        topMetrics: {
            widgetLabel: "Top Metrics",
            presenterType: "TopMetricsPresenter",

            defaultModelParams: {
                "time_unit": "1day",
                "metric": "top_factory_sessions"
            },

            columnLinkPrefixList: {
                "Email": "/analytics/pages/user-view.jsp?user",
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "Factory": "/analytics/pages/factory-view.jsp?factory",
            }
        },

        /** for User View */
        users: {
            widgetLabel: "Users",
            presenterType: "UsersPresenter",
            modelViewName: "users",
            modelMetricName: "users_statistics",

            columnLinkPrefixList: {
                "Email": "/analytics/pages/user-view.jsp?user",
                "ID": "/analytics/pages/session-view.jsp?session_id",
            },
        
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "ascSortColumnNumber": 0
            },
        },

        userOverview: {
            widgetLabel: "User Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "user",
            isNeedToSaveInHistory: false,     // default value = true
        },

        userData: {
            widgetLabel: "User Statistics",
            presenterType: "TablePresenter",
            modelViewName: "user_data",
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "columnsWithoutSorting": ["_all"]
            },
        },

        userSessions: {
            widgetLabel: "Sessions",
            presenterType: "TablePresenter",
            modelViewName: "user_sessions",

            isPaginable: true,    // default value is "false"
            modelMetricName: "product_usage_sessions",

            isSortable: true,   // default value is "false"
            defaultSortParams: "-date",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "descSortColumnNumber": 2
            },
        },

        userWorkspaceList: {
            widgetLabel: "Workspaces",
            presenterType: "TablePresenter",
            modelViewName: "user_workspace_list",

            isPaginable: true,    // default value is "false"
            modelMetricName: "usage_time_by_workspaces",

            columnLinkPrefixList: {
                "Name": "/analytics/pages/workspace-view.jsp?ws"
            }
        },

        userFactories: {
            widgetLabel: "Factories",
            presenterType: "TablePresenter",
            modelViewName: "user_factories",

            isPaginable: true,    // default value is "false"
            modelMetricName: "created_factories",

            columnLinkPrefixList: {
                "Factory URL": "/analytics/pages/factory-view.jsp?factory",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
            }
        },

        userActivity: {
            widgetLabel: "User Logs",
            presenterType: "TablePresenter",
            modelViewName: "user_activity",

            isPaginable: true,    // default value is "false"
            modelMetricName: "users_activity",

            isSortable: true,   // default value is "false"
            defaultSortParams: "-date",

            doNotDisplayCSVButton: true,   // default value is "false"
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "descSortColumnNumber": 0
            },
        },

        userEvents: {
            widgetLabel: "User Action",
            presenterType: "TablePresenter",
            modelViewName: "users_events",
        },


        /** for Session View */
        sessions: {
            widgetLabel: "Sessions",
            presenterType: "SessionsPresenter",
            modelViewName: "session_overview",
            modelMetricName: "product_usage_sessions",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "descSortColumnNumber": 3
            },
        },

        sessionOverview: {
            widgetLabel: "Session Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "session_overview",
            isNeedToSaveInHistory: false,    // default value = true

            defaultModelParams: {
                "session_id": "unexisted_session_id"
            },

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
        },

        userSessionActivity: {
            widgetLabel: "Session Events",
            presenterType: "TablePresenter",
            modelViewName: "session_events",
            isNeedToSaveInHistory: false,   // default value = true

            defaultModelParams: {
                "session_id": "unexisted_session_id",
            },

            isPaginable: true,    // default value is "false"
            modelMetricName: "users_activity",
            onePageRowsCount: 30,

            isSortable: true,   // default value is "false"
            defaultSortParams: "+date",
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "descSortColumnNumber": 0
            },
        },

        /** for Workspace View */
        workspaces: {
            widgetLabel: "Workspaces",
            presenterType: "WorkspacesPresenter",
            modelViewName: "workspaces",
            modelMetricName: "workspaces_statistics",
            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "ascSortColumnNumber": 0
            },
        },

        workspaceOverview: {
            widgetLabel: "Workspace Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "workspace_overview",
            isNeedToSaveInHistory: false,   // default value = true
        },

        workspaceSessions: {
            widgetLabel: "Sessions",
            presenterType: "TablePresenter",
            modelViewName: "workspace_sessions",

            isPaginable: true,    // default value is "false"
            modelMetricName: "product_usage_sessions",

            isSortable: true,   // default value is "false"
            defaultSortParams: "-date",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "descSortColumnNumber": 2
            },
        },

        workspaceUserList: {
            widgetLabel: "Users",
            presenterType: "TablePresenter",
            modelViewName: "workspace_user_list",

            isPaginable: true,    // default value is "false"
            modelMetricName: "usage_time_by_users",

            columnLinkPrefixList: {
                "Name": "/analytics/pages/user-view.jsp?user",
            }
        },

        workspaceActivity: {
            widgetLabel: "Workspace Logs",
            presenterType: "TablePresenter",
            modelViewName: "workspace_activity",

            isPaginable: true,    // default value is "false"
            modelMetricName: "users_activity",

            isSortable: true,   // default value is "false"
            defaultSortParams: "-date",

            doNotDisplayCSVButton: true,   // default value is "false"
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            columnSortingParameters: {
                "descSortColumnNumber": 0
            },
        },

        /** for Factory View */
        factories: {
            widgetLabel: "Factories",
            presenterType: "FactoriesPresenter",
            modelViewName: "factories",
            modelMetricName: "factory_statistics",
            columnLinkPrefixList: {
                "Factory URL": "/analytics/pages/factory-view.jsp?factory"
            }
        },

        factoryOverview: {
            widgetLabel: "Factory Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "factory",
            isNeedToSaveInHistory: false,   // default value = true

            columnLinkPrefixList: {
                "Created By": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
            }
        },

        factorySessions: {
            widgetLabel: "Sessions",
            presenterType: "TablePresenter",
            modelViewName: "factory_sessions",

            isPaginable: true,    // default value is "false"
            modelMetricName: "product_usage_factory_sessions",

            isSortable: true,   // default value is "false"
            defaultSortParams: "-date",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
        },

        factoryUsers: {
            widgetLabel: "Users",
            presenterType: "TablePresenter",
            modelViewName: "factory_users",

            isPaginable: true,    // default value is "false"
            modelMetricName: "factory_users",

            isSortable: true,   // default value is "false"
            defaultSortParams: "-time",

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
            },
        },
    }

    var registeredModelParams = [
        "time_unit",
        "user",
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
        "factory",
        "encoded_factory",
    ];

    /** see method analytics.main.getParamsFromButtons()    */
    var registeredViewParams = [
        "user",              // users-view, factory-statistics, *-reports
        "domain",            // *-reports
        "user_company",      // users-view, *-reports
        "org_id",            // factory-statistics
        "affiliate_id",      // factory-statistics
        "user_first_name",   // users-view
        "user_last_name",    // users-view
        "ide",               // top-menu
        "metric",            // top-metrics
        "from_date",         // user-view, workspace-view
        "to_date",           // user-view, workspace-view
        "event",             // session-view
        "ws",                // workspaces-view
        "factory",           // factories-view
        "encoded_factory",   // factories-view
    ];

    /** url params which are passed from other pages */
    var crossPageParams = [
        "user",        // users-view > user-view; workspace-view > user-view; top metrics report > user-view
        "sort",        // users-view > users-view; workspaces-view > workspaces-view
        "page",        // user-view > user-view; users-view > users-view; workspaces-view > workspaces-view; workspace-view > workspace-view
        "session_id",  // user-view > session-view; workspace-view > session-view; top metrics report > session-view 
        "ws",          // workspaces-view > workspace-view; user-view > workspace-view
        "factory",     // factories-view > factory-view; user-view > factory-view; workspace-view > factory-view; top metrics report > factory-view
    ];

    /** Global parameters stored in Browser Storage  */
    var globalParams = [
        "ide",
    ];

    /** List of system messages of Analytics  */
    var systemMessagesList = [
        "User Did Not Enter Workspace",
    ];

    function getProperty(widgetName, propertyName, defaultValue) {
        var widgetProperty = widgetConfiguration[widgetName][propertyName];
        if (typeof widgetProperty == "undefined") {
            widgetProperty = defaultValue;
        }
        return widgetProperty;
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

    function isSystemMessage(message) {
        for (var i in systemMessagesList) {
            if (systemMessagesList[i] == message) {
                return true;
            }

            return false;
        }
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
        isSystemMessage: isSystemMessage,

    }
}