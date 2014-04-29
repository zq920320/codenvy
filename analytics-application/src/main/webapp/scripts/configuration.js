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
            clientSortParams: {
                "columnsWithoutSorting": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
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
            clientSortParams: {
                "columnsWithoutSorting": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14]
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
            },
            
            // see clientSortParams in the
            // TopMetricsPresenter::clientSortParams property
        },

        /** for Event View */
        events: {
            widgetLabel: "Events",
            presenterType: "EntryViewPresenter",
            modelViewName: "events",
            modelMetricName: "users_activity",

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

//            mapColumnToServerSortParam: {
//                "Email": "user",
//                "First Name": "user_first_name",
//                "Last Name": "user_last_name",
//                "Company": "user_company",
//                "Job": "user_job",
//                "# Sessions": "sessions",
//                "Usage Time": "time",
//                "# Projects": "projects",
//            },
        },

        /** for User View */
        users: {
            widgetLabel: "Users",
            presenterType: "EntryViewPresenter",
            modelViewName: "users",
            modelMetricName: "users_statistics",

            columnLinkPrefixList: {
                "Email": "/analytics/pages/user-view.jsp?user",
                "ID": "/analytics/pages/session-view.jsp?session_id",
            },
        
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },
            
            mapColumnToServerSortParam: {
                "Email": "user",
                "First Name": "user_first_name",
                "Last Name": "user_last_name",
                "Company": "user_company",
                "Job": "user_job",
                "# Sessions": "sessions",
                "Usage Time": "time",
                "# Projects": "projects",
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
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_data",
        },

        userSessions: {
            widgetLabel: "Sessions",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_sessions",

            isPaginable: true,    // default value is "false"
            modelMetricName: "product_usage_sessions",
            
            defaultServerSortParams: "-date",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 2
            },
            
            mapColumnToServerSortParam: {
                "ID": "session_id",
                "Workspace": "ws",
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time",
            },
        },

        userWorkspaceList: {
            widgetLabel: "Workspaces",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_workspace_list",

            isPaginable: true,    // default value is "false"
            modelMetricName: "usage_time_by_workspaces",

            columnLinkPrefixList: {
                "Name": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            mapColumnToServerSortParam: {
                "Name": "ws",
                "Sessions": "sessions",
                "Time": "time",
            },
        },

        userFactories: {
            widgetLabel: "Factories",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_factories",

            isPaginable: true,    // default value is "false"
            modelMetricName: "created_factories",

            columnLinkPrefixList: {
                "Factory URL": "/analytics/pages/factory-view.jsp?factory",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 0
            },
            
            mapColumnToServerSortParam: {
                "Date": "date",
                "Factory URL": "factory",
                "Workspace": "ws",
                "Repository": "repository",
                "Project": "project",
                "Type": "project_type",
            }
        },

        userProjects: {
            widgetLabel: "Projects",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_projects",

            isPaginable: true,    // default value is "false"
            modelMetricName: "projects",

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
                "Project": "/analytics/pages/project-view.jsp?project",
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Date": "date",
                "Workspace": "ws",
                "Project": "project",
                "Type": "project_type",
            }
        },

        /** for Session View */
        sessions: {
            widgetLabel: "Sessions",
            presenterType: "EntryViewPresenter",
            modelViewName: "session_overview",
            modelMetricName: "product_usage_sessions",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 3
            },
            
            mapColumnToServerSortParam: {
                "ID": "session_id",
                "User": "user",
                "Workspace": "ws",
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time",
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
            presenterType: "HorizontalTablePresenter",
            modelViewName: "session_events",
            isNeedToSaveInHistory: false,   // default value = true

            defaultModelParams: {
                "session_id": "unexisted_session_id",
            },

            isPaginable: true,    // default value is "false"
            modelMetricName: "users_activity",
            onePageRowsCount: 30,

            defaultServerSortParams: "+date",
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },
        },

        /** for Workspace View */
        workspaces: {
            widgetLabel: "Workspaces",
            presenterType: "EntryViewPresenter",
            modelViewName: "workspaces",
            modelMetricName: "workspaces_statistics",
            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },
            
            mapColumnToServerSortParam: {
                "Workspace": "ws",
                "Time": "time",
                "Sessions": "sessions",
                "# Runs": "runs",
                "# Debugs": "debugs",
                "# Builds": "builds",
                "# Deploys": "deploys",
                "Invites": "invites",
                "Joined Users": "joined_users",
            },
        },

        workspaceOverview: {
            widgetLabel: "Workspace Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "workspace_overview",
            isNeedToSaveInHistory: false,   // default value = true
        },

        workspaceProjects: {
            widgetLabel: "Projects",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "workspace_projects",

            isPaginable: true,    // default value is "false"
            modelMetricName: "projects",

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Project": "/analytics/pages/project-view.jsp?project",
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Date": "date",
                "User": "user",
                "Project": "project",
                "Type": "project_type",
            }
        },

        workspaceSessions: {
            widgetLabel: "Sessions",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "workspace_sessions",

            isPaginable: true,    // default value is "false"
            modelMetricName: "product_usage_sessions",

            defaultServerSortParams: "-date",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 2
            },
            
            mapColumnToServerSortParam: {
                "ID": "session_id",
                "User": "user",
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time",
            },
        },

        workspaceUserList: {
            widgetLabel: "Users",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "workspace_user_list",

            isPaginable: true,    // default value is "false"
            modelMetricName: "usage_time_by_users",

            columnLinkPrefixList: {
                "Name": "/analytics/pages/user-view.jsp?user",
            },
            
            mapColumnToServerSortParam: {
                "Name": "user",
                "Sessions": "sessions",
                "Time": "time",
            },
        },

        /** for Project View */
        projects: {
            widgetLabel: "Projects",
            presenterType: "EntryViewPresenter",
            modelViewName: "projects",
            modelMetricName: "projects",
            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
                "User": "/analytics/pages/user-view.jsp?user",
                "Project": "/analytics/pages/project-view.jsp?project",
            },

            columnCombinedLinkConfiguration: {
                "Project": {                   
                    baseLink: "/analytics/pages/project-view.jsp",
                    mapColumnToParameter: {
                        "Project": "project",
                        "Workspace": "ws",
                        "User": "user",
                    }
                }
            },
            
            defaultServerSortParams: "-date",

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Date": "date",
                "Project": "project",
                "Type": "project_type",
                "Workspace": "ws",
                "User": "user",
            },
        },

        projectOverview: {
            widgetLabel: "Project Overview",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "project",
            modelMetricName: "projects",
            isNeedToSaveInHistory: false,   // default value = true

            isPaginable: true,

            defaultServerSortParams: "-date",

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
                "User": "/analytics/pages/user-view.jsp?user",
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Created Date": "date",
                "Project": "project",
                "Type": "project_type",
                "Workspace": "ws",
                "User": "user",
            },

        },

        projectStatistics: {
            widgetLabel: "Project Statistics",
            presenterType: "VerticalTablePresenter",
            modelViewName: "project_statistics",
        },

        /** for Accounts View */
        accounts: {
            widgetLabel: "Accounts",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "accounts",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/account-view.jsp?account_id",
                "Owner": "/analytics/pages/user-view.jsp?user",
            },
        },

        /** for Account View */
        accountOverview: {
            widgetLabel: "Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "account",

            columnLinkPrefixList: {
                "Email": "/analytics/pages/user-view.jsp?user",
            },
        },

        accountSubscriptions: {
            widgetLabel: "Subscriptions",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "account_subscriptions_list",
        },

        accountWorkspaces: {
            widgetLabel: "Workspaces",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "account_workspaces_list",

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
            },
        },

        accountUsers: {
            widgetLabel: "Users",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "account_users_list",

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
            },
        },


        /** for Factory View */
        factories: {
            widgetLabel: "Factories",
            presenterType: "EntryViewPresenter",
            modelViewName: "factories",
            modelMetricName: "factory_statistics",
            columnLinkPrefixList: {
                "Factory URL": "/analytics/pages/factory-view.jsp?factory"
            },
            
            mapColumnToServerSortParam: {
                "Factory URL": "factory",
                "Project Type": "project_type",
                "Clicks": "ws_created",
                "Sessions": "sessions",
                "Time": "time",
                "# Runs": "runs",
                "# Deploys": "deploys",
                "# Builds": "builds",
                "Organization": "org_id",
            },
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
            presenterType: "HorizontalTablePresenter",
            modelViewName: "factory_sessions",

            isPaginable: true,    // default value is "false"
            modelMetricName: "product_usage_factory_sessions",

            defaultServerSortParams: "-date",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },
            
            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 3
            },
            
            mapColumnToServerSortParam: {
                "ID": "session_id",
                "User": "user",
                "Workspace": "ws",
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time",
            },
        },

        factoryUsers: {
            widgetLabel: "Users",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "factory_users",

            isPaginable: true,    // default value is "false"
            modelMetricName: "factory_users",

            defaultServerSortParams: "-time",

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
            },
            
            mapColumnToServerSortParam: {
                "User": "user",
                "# Sessions": "sessions",
                "Duration": "time",
                "# Runs": "run",
                "# Builds": "build",
                "# Deploys": "deploy",
            },
        },
        
        // drill-down page
        drillDown: {
            widgetLabel: "Drill Down Report",
            presenterType: "DrillDownPresenter",
            isPaginable: true,    // default value is "false"
            
            columnLinkPrefixList: {
                "user": "/analytics/pages/user-view.jsp?user",
                "ws": "/analytics/pages/workspace-view.jsp?ws"                
            },
        }
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
        "time_interval",
        "expanded_metric_name",
        "project",
        "project_type",
        "action",
        "parameters",
        "account_id",
        "data_universe",
    ];

    /** see method analytics.main.getParamsFromButtons() */
    var registeredViewParams = [
        "user",              // users-view, factory-statistics, *-reports
        "domain",            // *-reports
        "user_company",      // users-view, *-reports
        "org_id",            // factory-statistics
        "affiliate_id",      // factory-statistics
        "user_first_name",   // users-view
        "user_last_name",    // users-view
        "ide",               // top-menu
        "data_universe",     // top-menu
        "metric",            // top-metrics
        "from_date",         // user-view, workspace-view
        "to_date",           // user-view, workspace-view
        "event",             // session-view
        "action",            // session-view
        "event_parameter_name",    // session-view
        "event_parameter_value",   // session-view
        "ws",                // workspaces-view
        "factory",           // factories-view
        "encoded_factory",   // factories-view
        "expanded_metric_name",    // all entry views 
        "time_interval",   // drill down page
        "project",           // projects-view
        "project_type",      // projects-view
        "account_id",        // account-view, accounts-view
    ];

    /** url params which are passed from other pages */
    var crossPageParams = [
        "user",        // users-view > user-view; workspace-view > user-view;
                        // top metrics report > user-view
        "sort",        // users-view > users-view; workspaces-view >
                        // workspaces-view
        "page",        // user-view > user-view; users-view > users-view;
                        // workspaces-view > workspaces-view; workspace-view >
                        // workspace-view
        "session_id",  // user-view > session-view; workspace-view >
                        // session-view; top metrics report > session-view
        "ws",          // workspaces-view > workspace-view; user-view >
                        // workspace-view
        "factory",     // factories-view > factory-view; user-view >
                        // factory-view; workspace-view > factory-view; top
                        // metrics report > factory-view
        "project",      // projects-view > project-view
    ];
    
    /** Global parameters stored in Browser Storage */
    var globalParams = [
        "ide",
    ];

    /** Date params which should have "yyyymmdd" in model and "yyyy-mm-dd" format in view */
    var dateParams = [
        "from_date",
        "to_date",
    ];

    /** 
     * Server configuration. Defined in the footer.jsp.
     */
    this.serverConfiguration = {};
    
    /** List of system messages of Analytics */
    var systemMessagesList = [
        "User Did Not Enter Workspace",
    ];
    
    /**
     * Returns property of widget.
     */
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
     * Add model params which are undefined in modelParams and defined in
     * widgetConfiguration[widgetName]["defaultModelParams"] property
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
     * Get server configuration.
     */
    function getServerProperty(propertyName, defaultValue) {
        var serverProperty = this.serverConfiguration[propertyName];
        if (typeof serverProperty == "undefined") {
            serverProperty = defaultValue;
        }
        return serverProperty;
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
     * Return modelParams which had been cleared from forbidden params defined
     * in the configuration of widget with widgetName
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

    /**
     * Return true only if paramName consists in dataParams list.
     */
    function isDateParam(paramName) {
        for (var i = 0; i < dateParams.length; i++) {
            if (dateParams[i] == paramName) {
                return true;
            }
        }
        
        return false;
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
        getServerProperty: getServerProperty,
        getViewParamsWithNullValues: getViewParamsWithNullValues,
        removeForbiddenModelParams: removeForbiddenModelParams,
        getCrossPageParamsList: getCrossPageParamsList,
        isDateParam: isDateParam,
        isSystemMessage: isSystemMessage,

    }
}