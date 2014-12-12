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
analytics.configuration = new AnalyticsConfiguration();

function AnalyticsConfiguration() {
    var widgetConfiguration = {
        /** Reports */
        signupAnalysis: {
            widgetLabel: "Signup Analysis",
            presenterType: "ReportPresenter",
            modelViewName: "analysis",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
        },

        factoryReport: {
            widgetLabel: "Factory Report",
            presenterType: "ReportPresenter",
            modelViewName: "factory-timeline",

            defaultModelParams: {
                "time_unit": "day"
            },

            displayLineChart: true  // default is false
        },

        runnerReport: {
            widgetLabel: "Runner Report",
            presenterType: "ReportPresenter",
            modelViewName: "runner_timeline",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
        },

        builderReport: {
            widgetLabel: "Builder Report",
            presenterType: "ReportPresenter",
            modelViewName: "builder_timeline",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
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
            }
        },

        workspaceReport: {
            widgetLabel: "Workspace Report",
            presenterType: "ReportPresenter",
            modelViewName: "workspace_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
        },

        userReport: {
            widgetLabel: "User Report",
            presenterType: "ReportPresenter",
            modelViewName: "user_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
        },

        engagementLevels: {
            widgetLabel: "Engagement Levels",
            presenterType: "ReportPresenter",
            modelViewName: "engagement_levels",

            forbiddenModelParams: ["time_unit"],

            displayLineChart: true  // default is false
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

            displayLineChart: true  // default is false
        },

        usageReport: {
            widgetLabel: "Usage Report",
            presenterType: "ReportPresenter",
            modelViewName: "usage_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
        },

        sessionReport: {
            widgetLabel: "Session Report",
            presenterType: "ReportPresenter",
            modelViewName: "session_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
        },

        projectReport: {
            widgetLabel: "Project Report",
            presenterType: "ReportPresenter",
            modelViewName: "project_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            displayLineChart: true  // default is false
        },

        keyFeatureUsageReport: {
            widgetLabel: "Key Feature Usage Report",
            presenterType: "ReportPresenter",
            modelViewName: "key_feature_usage_report",

            defaultModelParams: {
                "time_unit": "month"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            displayLineChart: true  // default is false
        },

        topFactorySessions: {
            widgetLabel: "Top Factory Sessions",
            presenterType: "TopMetricsPresenter",
            modelViewName: "top_factory_sessions",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 0
            },

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "Factory": "/analytics/pages/factory-view.jsp?factory"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Aggregate Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "ID": "session_id"
                }
            },

            doNotLinkOnEmptyParameter: false  // default value = true,
            // true means that link shouldn't be added if at least one parameter is empty;
            // false means that link should be added without empty parameters
        },

        topFactories: {
            widgetLabel: "Top Factories",
            presenterType: "TopMetricsPresenter",
            modelViewName: "top_factories",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 3
            },

            columnLinkPrefixList: {
                "Factory": "/analytics/pages/factory-view.jsp?factory"
            },

            // see clientSortParams in the
            // TopMetricsPresenter::clientSortParams property

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "Workspaces Created": "temporary_workspaces_created",
                    "Accounts Created": "created_users_from_factory",
                    "Aggregate Time": "#SESSIONS",
                    "% Anon": "anonymous_factory_sessions",
                    "% Auth": "authenticated_factory_sessions",
                    "% Abandon": "abandoned_factory_sessions",
                    "% Convert": "converted_factory_sessions"
                },

                mapColumnToParameter: {
                    "Factory": "factory"
                }
            }
        },

        topReferrers: {
            widgetLabel: "Top Referrers",
            presenterType: "TopMetricsPresenter",
            modelViewName: "top_referrers",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 3
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "Workspaces Created": "temporary_workspaces_created",
                    "Aggregate Time": "#SESSIONS",
                    "% Anon": "anonymous_factory_sessions",
                    "% Auth": "authenticated_factory_sessions",
                    "% Abandon": "abandoned_factory_sessions",
                    "% Convert": "converted_factory_sessions"
                },

                mapColumnToParameter: {
                    "Referrer": "referrer"
                }
            }
        },

        topUsers: {
            widgetLabel: "Top Users",
            presenterType: "TopMetricsPresenter",
            modelViewName: "top_users",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user"
            },

            // see clientSortParams in the TopMetricsPresenter::clientSortParams property

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "1 Day": "#SESSIONS",
                    "7 Days": "#SESSIONS",
                    "30 Days": "#SESSIONS",
                    "60 Days": "#SESSIONS",
                    "90 Days": "#SESSIONS",
                    "1 Year": "#SESSIONS",
                    "Lifetime": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "User": "user"
                }
            }
        },

        topDomains: {
            widgetLabel: "Top Domains",
            presenterType: "TopMetricsPresenter",
            modelViewName: "top_domains",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            // see clientSortParams in the TopMetricsPresenter::clientSortParams property

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "1 Day": "#SESSIONS",
                    "7 Days": "#SESSIONS",
                    "30 Days": "#SESSIONS",
                    "60 Days": "#SESSIONS",
                    "90 Days": "#SESSIONS",
                    "1 Year": "#SESSIONS",
                    "Lifetime": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "Domain": "domain"
                }
            }
        },

        topCompanies: {
            widgetLabel: "Top Companies",
            presenterType: "TopMetricsPresenter",
            modelViewName: "top_companies",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            // see clientSortParams in the TopMetricsPresenter::clientSortParams property

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "1 Day": "#SESSIONS",
                    "7 Days": "#SESSIONS",
                    "30 Days": "#SESSIONS",
                    "60 Days": "#SESSIONS",
                    "90 Days": "#SESSIONS",
                    "1 Year": "#SESSIONS",
                    "Lifetime": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "Company": "user_company"
                }
            }
        },

        timeTrackingWorkspaces: {
            widgetLabel: " Time Tracking Workspaces",
            presenterType: "TopMetricsPresenter",
            modelViewName: "time_tracking_workspaces",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            clientSortParams: {
                "descSortColumnNumber": 1
            },

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "Workspace": "ws"
                }
            }

        },

        timeTrackingUsers: {
            widgetLabel: " Time Tracking Users",
            presenterType: "TopMetricsPresenter",
            modelViewName: "time_tracking_users",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            clientSortParams: {
                "descSortColumnNumber": 1
            },

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "User": "user"
                }
            }
        },

        timeTrackingSessions: {
            widgetLabel: " Time Tracking Sessions",
            presenterType: "TopMetricsPresenter",
            modelViewName: "time_tracking_sessions",

            defaultModelParams: {
                "passed_days_count": "by_1_day"
            },

            clientSortParams: {
                "descSortColumnNumber": 3
            },

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "ID": "session_id"
                }
            }
        },

        /** for Event View */
        events: {
            widgetLabel: "Events",
            presenterType: "EntryViewPresenter",
            modelViewName: "events",

            defaultModelParams: {
                "sort": "-date"
            },

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            }
        },

        /** for User View */
        users: {
            widgetLabel: "Users",
            presenterType: "EntryViewPresenter",
            modelViewName: "users",

            modelSummarizedMetricName: "users_statistics_list",

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },


            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS",
                    "Projects": "#PROJECTS",
                    "Factories": "#FACTORIES"
                },

                mapColumnToParameter: {
                    "User": "user"
                }
            },

            mapColumnToServerSortParam: {
                "Projects": "projects",
                "Sessions": "sessions",
                "Time": "time"
            }
        },

        usersOverview: {
            presenterType: "SummaryTablePresenter",
            modelViewName: "users_statistics_list",
            modelMetricName: "users_statistics",
            doNotDisplayCSVButton: true
        },

        userOverview: {
            widgetLabel: "User Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "user_overview",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/user-view.jsp?user"
            }
        },
        
        userStatistics: {
            widgetLabel: "User Statistics",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_statistics",
            
            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Projects": "#PROJECTS",
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS",
                    "Builds": "builds",
                    "Build Time": "builds_time",
                    "Runs": "runs",
                    "Run Time": "runs_time",
                    "Debugs": "debugs",
                    "Debug Time": "debugs_time",
                    "Deploys": "deploys",
                    "Factories": "#FACTORIES"
                }
            }
        },

        userSessions: {
            widgetLabel: "Sessions",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_sessions",

            isPaginable: true,    // default value is "false"

            defaultModelParams: {
                "sort": "-date"
            },

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
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time",
                "Referrer": "referrer"
            }
        },

        userWorkspaceList: {
            widgetLabel: "Workspaces",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_workspace_list",

            isPaginable: true,    // default value is "false"

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },

            mapColumnToServerSortParam: {
                "Sessions": "sessions",
                "Time": "time"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "Workspace": "ws"
                }
            }
        },

        userFactories: {
            widgetLabel: "Factories",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_factories",

            isPaginable: true,    // default value is "false"

            columnLinkPrefixList: {
                "Factory URL": "/analytics/pages/factory-view.jsp?factory",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Date": "date",
                "Factory URL": "factory",
                "Repository": "repository",
                "Project": "project",
                "Type": "project_type"
            }
        },

        userEvents: {
            widgetLabel: "User Events",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_events",

            isPaginable: true,    // default value is "false"
            onePageRowsCount: 30,

            defaultModelParams: {
                "sort": "+date"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            }
        },

        userProjects: {
            widgetLabel: "Projects",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "user_projects",

            isPaginable: true,    // default value is "false"

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
                "Type": "/analytics/pages/projects-view.jsp?project_type"
            },

            columnCombinedLinkConfiguration: {
                "Project": {
                    baseLink: "/analytics/pages/project-view.jsp",
                    mapColumnToParameter: {
                        "Project": "project",
                        "Workspace": "ws",
                        "User": "user"
                    }
                }
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Date": "date",
                "Project": "project",
                "Type": "project_type"
            }
        },

        /** for Session View */
        sessions: {
            widgetLabel: "Sessions",
            presenterType: "EntryViewPresenter",
            modelViewName: "session_overview",

            modelSummarizedMetricName: "product_usage_sessions_list",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "descSortColumnNumber": 3
            },

            defaultModelParams: {
                "sort": "-date"
            },

            mapColumnToServerSortParam: {
                "ID": "session_id",
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time",
                "Referrer": "referrer"
            }
        },

        sessionsOverview: {
            presenterType: "SummaryTablePresenter",
            modelViewName: "product_usage_sessions_list",
            modelMetricName: "product_usage_sessions",
            doNotDisplayCSVButton: true
        },
        
        sessionOverview: {
            widgetLabel: "Session Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "session_overview",

            defaultModelParams: {
                "session_id": "unexisted_session_id"
            },

            columnLinkPrefixList: {
                "ID": "/analytics/pages/session-view.jsp?session_id",                
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            }
        },

        userSessionActivity: {
            widgetLabel: "Session Events",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "session_events",

            defaultModelParams: {
                "session_id": "unexisted_session_id"
            },

            isPaginable: true,    // default value is "false"
            onePageRowsCount: 30,

            defaultModelParams: {
                "sort": "+date"
            },


            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            }
        },

        /** for Workspace View */
        workspaces: {
            widgetLabel: "Workspaces",
            presenterType: "EntryViewPresenter",
            modelViewName: "workspaces",

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            },

            modelSummarizedMetricName: "workspaces_statistics_list",

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            defaultModelParams: {
                "sort": "+ws"
            },

            mapColumnToServerSortParam: {
                "Projects": "projects",
                "Time": "time",
                "Sessions": "sessions",
                "Builds": "builds",
                "Build Time": "build_time",
                "Runs": "runs",
                "Run Time": "run_time",
                "Debugs": "debugs",
                "Debug Time": "debug_time",
                "Deploys": "deploys",
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Projects": "#PROJECTS",
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS",
                    "Builds": "builds",
                    "Build Time": "builds_time",
                    "Runs": "runs",
                    "Run Time": "runs_time",
                    "Debugs": "debugs",
                    "Debug Time": "debugs_time",
                    "Deploys": "deploys",
                },

                mapColumnToParameter: {
                    "Workspace": "ws"
                }
            }
        },

        workspacesOverview: {
            presenterType: "SummaryTablePresenter",
            modelViewName: "workspaces_statistics_list",
            modelMetricName: "workspaces_statistics",
            doNotDisplayCSVButton: true
        },
        
        workspaceOverview: {
            widgetLabel: "Workspace Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "workspace_overview",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/workspace-view.jsp?ws"
            }
        },
        
        workspaceStatistics: {
            widgetLabel: "Workspace Statistics",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "workspace_statistics",
            
            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Projects": "#PROJECTS",
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS",
                    "Builds": "builds",
                    "Build Time": "builds_time",
                    "Runs": "runs",
                    "Run Time": "runs_time",
                    "Debugs": "debugs",
                    "Debug Time": "debugs_time",
                    "Deploys": "deploys",
                }
            }
        },

        workspaceProjects: {
            widgetLabel: "Projects",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "workspace_projects",

            isPaginable: true,    // default value is "false"

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
                "Type": "/analytics/pages/projects-view.jsp?project_type"
            },

            columnCombinedLinkConfiguration: {
                "Project": {
                    baseLink: "/analytics/pages/project-view.jsp",
                    mapColumnToParameter: {
                        "Project": "project",
                        "Workspace": "ws",
                        "User": "user"
                    }
                }
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Date": "date",
                "Project": "project",
                "Type": "project_type"
            }
        },

        workspaceSessions: {
            widgetLabel: "Sessions",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "workspace_sessions",

            isPaginable: true,    // default value is "false"

            defaultModelParams: {
                "sort": "-date"
            },

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
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time"
            }
        },

        workspaceUserList: {
            widgetLabel: "Users",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "workspace_user_list",

            isPaginable: true,    // default value is "false"

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user"
            },

            mapColumnToServerSortParam: {
                "Sessions": "sessions",
                "Time": "time"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "User": "user"
                }
            }
        },

        /** for Project View */
        projects: {
            widgetLabel: "Projects",
            presenterType: "EntryViewPresenter",
            modelViewName: "projects",

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
                "User": "/analytics/pages/user-view.jsp?user",
                "Type": "/analytics/pages/projects-view.jsp?project_type"
            },

            modelSummarizedMetricName: "projects_statistics_list",

            columnCombinedLinkConfiguration: {
                "Project": {
                    baseLink: "/analytics/pages/project-view.jsp",
                    mapColumnToParameter: {
                        "Project": "project",
                        "Workspace": "ws",
                        "User": "user"
                    }
                }
            },

            defaultModelParams: {
                "sort": "-date"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Date": "date",
                "Project": "project",
                "Type": "project_type",
                "Builds": "builds",
                "Build Time": "build_time",
                "Runs": "runs",
                "Run Time": "run_time",
                "Debugs": "debugs",
                "Debug Time": "debug_time",
                "Deploys": "deploys",
            }
        },

        projectsOverview: {
            presenterType: "SummaryTablePresenter",
            modelViewName: "projects_statistics_list",
            modelMetricName: "projects_statistics",
            doNotDisplayCSVButton: true
        },
        
        projectOverview: {
            widgetLabel: "Project Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "project_overview",
            modelMetricName: "projects_statistics",

            isPaginable: true,

            defaultModelParams: {
                "sort": "-date"
            },

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws",
                "User": "/analytics/pages/user-view.jsp?user",
                "Type": "/analytics/pages/projects-view.jsp?project_type"
            },

            columnCombinedLinkConfiguration: {
                "Project": {
                    baseLink: "/analytics/pages/project-view.jsp",
                    mapColumnToParameter: {
                        "Project": "project",
                        "Workspace": "ws",
                        "User": "user"
                    }
                }
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            mapColumnToServerSortParam: {
                "Created Date": "date",
                "Project": "project",
                "Type": "project_type"
            }
        },

        projectStatistics: {
            widgetLabel: "Project Statistics",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "project_statistics"
        },


        projectEvents: {
            widgetLabel: "Project Events",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "project_events",

            isPaginable: true,    // default value is "false"
            onePageRowsCount: 30,

            defaultModelParams: {
                "sort": "+date"
            },

            /** @see DatabaseTable::makeTableSortable() method docs */
            clientSortParams: {
                "ascSortColumnNumber": 0
            },

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            }
        },

        /** for Accounts View */
        accounts: {
            widgetLabel: "Accounts",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "accounts",

            columnLinkPrefixList: {
                "ID": "/analytics/pages/account-view.jsp?account_id",
                "User": "/analytics/pages/user-view.jsp?user"
            }

        },

        accountOverview: {
            widgetLabel: "Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "account"
        },

        accountSubscriptions: {
            widgetLabel: "Subscriptions",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "account_subscriptions_list"
        },

        accountWorkspaces: {
            widgetLabel: "Workspaces",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "account_workspaces_list",

            columnLinkPrefixList: {
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            }
        },

        accountUsers: {
            widgetLabel: "Users",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "account_users_roles",

            isPaginable: true,    // default value is "false"

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user",
                "Workspace": "/analytics/pages/workspace-view.jsp?ws"
            }
        },


        /** for Factory View */
        factories: {
            widgetLabel: "Factories",
            presenterType: "EntryViewPresenter",
            modelViewName: "factories",

            columnLinkPrefixList: {
                "Factory URL": "/analytics/pages/factory-view.jsp?factory",
                "Project Type": "/analytics/pages/projects-view.jsp?project_type"
            },

            modelSummarizedMetricName: "factory_statistics_list",

            mapColumnToServerSortParam: {
                "Factory URL": "factory",
                "Sessions": "sessions",
                "Time": "time",
                "Runs": "runs",
                "Deploys": "deploys",
                "Builds": "builds",
                "Organization": "org_id"
            },

            defaultModelParams: {
                "sort": "+ws_created"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Workspaces Created": "temporary_workspaces_created",
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "Factory URL": "factory"
                }
            }
        },

        factoriesOverview: {
            presenterType: "SummaryTablePresenter",
            modelViewName: "factory_statistics_list",
            modelMetricName: "factory_statistics",
            doNotDisplayCSVButton: true
        },

        factoryOverview: {
            widgetLabel: "Factory Overview",
            presenterType: "VerticalTablePresenter",
            modelViewName: "factory_overview",

            columnLinkPrefixList: {
                "Created By": "/analytics/pages/user-view.jsp?user",
                "Project Type": "/analytics/pages/projects-view.jsp?project_type"
            },
            
            // replace empty column value on url parameter value with the same name
            urlParameterColumn: "Factory"
        },
        
        factoryStatistics: {
            widgetLabel: "Factory Statistics",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "factory_statistics",

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Workspaces Created": "temporary_workspaces_created",
                    "Sessions": "#SESSIONS",
                    "Known": "authenticated_factory_sessions",
                    "Converted": "converted_factory_sessions",
                    "Time": "#SESSIONS"
                }
            }
        },

        factorySessions: {
            widgetLabel: "Sessions",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "factory_sessions",

            isPaginable: true,    // default value is "false"

            defaultModelParams: {
                "sort": "-date"
            },

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
                "Start Time": "date",
                "End Time": "end_time",
                "Duration": "time",
                "Referrer": "referrer"
            }
        },

        factoryUsers: {
            widgetLabel: "Users",
            presenterType: "HorizontalTablePresenter",
            modelViewName: "factory_users",

            isPaginable: true,    // default value is "false"

            defaultModelParams: {
                "sort": "-time"
            },

            columnLinkPrefixList: {
                "User": "/analytics/pages/user-view.jsp?user"
            },

            mapColumnToServerSortParam: {
                "Sessions": "sessions",
                "Time": "time",
                "Runs": "runs",
                "Builds": "builds",
                "Deploys": "deploys"
            },

            columnDrillDownPageLinkConfiguration: {
                mapColumnNameToExpandableMetric: {
                    "Sessions": "#SESSIONS",
                    "Time": "#SESSIONS"
                },

                mapColumnToParameter: {
                    "User": "user"
                }
            }
        },

        // drill-down page
        drillDown: {
            widgetLabel: "Drill Down Report",
            presenterType: "DrillDownPresenter",
            isPaginable: true,    // default value is "false"

            columnLinkPrefixList: {
                "user": "/analytics/pages/user-view.jsp?user",
                "ws": "/analytics/pages/workspace-view.jsp?ws",
                "factory": "/analytics/pages/factory-view.jsp?factory",
                "session_id": "/analytics/pages/session-view.jsp?session_id",
                "project": "/analytics/pages/project-view.jsp?project"
            }
        }
    };

    var registeredModelParams = [
        "time_unit",
        "user",
        "aliases",
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
        "from_date",
        "to_date",
        "event",
        "ws",
        "factory",
        "referrer",
        "factory_id",
        "encoded_factory",
        "time_interval",
        "expanded_metric_name",
        "project",
        "project_type",
        "parameters",
        "action",
        "account_id",
        "data_universe",
        "passed_days_count"
     ];

    /** Global parameters stored in Browser Storage */
    var globalParams = [
        "ide",
        "data_universe",
        "ui_preferences",
        "data_preferences"
    ];

    /** Date params which should have "yyyymmdd" in model and "yyyy-mm-dd" format in view */
    var dateParams = [
        "from_date",
        "to_date"
    ];

    /**
     * Server configuration. Defined in the footer.jsp.
     */
    this.serverConfiguration = {};

    /** List of system messages of Analytics */
    var systemMessagesList = [
        "User Did Not Enter Workspace"
    ];


    /** Drill Down page parameters */
    var defaultDrillDownPageAddress = "/analytics/pages/drill-down.jsp";
    var mapExpandableMetricToDrillDownPageType = {
        /** USERS */
        "active_users": "#USERS",
        "active_users_from_beginning": "#USERS",
        "users_who_created_project": "#USERS",
        "users_who_built": "#USERS",
        "users_who_deployed": "#USERS",
        "users_who_deployed_to_paas": "#USERS",
        "users_who_invited": "#USERS",
        "created_users": "#USERS",
        "created_users_from_factory": "#USERS",
        "user_invite": "#USERS",
        "removed_users": "#USERS",
        "users_activity": "#USERS",
        "total_users": "#USERS",
        "users_added_to_workspaces_using_invitation": "#USERS",
        "non_active_users": "#USERS",

        "product_usage_users_above_300_min": "#USERS",
        "product_usage_users_between_10_and_60_min": "#USERS",
        "product_usage_users_between_60_and_300_min": "#USERS",

        "product_usage_condition_above_300_min": "#USERS",
        "product_usage_condition_below_120_min": "#USERS",
        "product_usage_condition_between_120_and_300_min": "#USERS",

        "new_active_users": "#USERS",
        "created_users_from_auth": "#USERS",
        "users_logged_in_with_form": "#USERS",
        "users_logged_in_with_github": "#USERS",
        "users_logged_in_with_google": "#USERS",
        "users_logged_in_with_form_percent": "#USERS",
        "users_logged_in_with_github_percent": "#USERS",
        "users_logged_in_with_google_percent": "#USERS",
        "returning_active_users": "#USERS",
        "product_usage_users_total": "#USERS",
        "product_usage_users_below_10_min": "#USERS",
        "users_logged_in_total": "#USERS",
        "users_accepted_invites_percent": "#USERS",
        "users_accepted_invites": "#USERS",
        "timeline_product_usage_condition_above_300_min": "#USERS",
        "timeline_product_usage_condition_below_120_min": "#USERS",
        "timeline_product_usage_condition_between_120_and_300_min": "#USERS",


        /** WORKSPACES */
        "active_workspaces": "#WORKSPACES",
        "created_workspaces": "#WORKSPACES",
        "temporary_workspaces_created": "#WORKSPACES",
        "destroyed_workspaces": "#WORKSPACES",
        "collaborative_sessions_started": "#WORKSPACES",
        "workspaces_where_users_have_several_factory_sessions": "#WORKSPACES",
        "workspaces_with_zero_factory_sessions_length": "#WORKSPACES",

        "total_workspaces": "#WORKSPACES",

        "new_active_workspaces": "#WORKSPACES",
        "returning_active_workspaces": "#WORKSPACES",
        "non_active_workspaces": "#WORKSPACES",


        /** PROJECTS */
        "builds": "#PROJECTS",
        "deploys": "#PROJECTS",
        "deploys_to_paas": "#PROJECTS",
        "runs": "#PROJECTS",
        "debugs": "#PROJECTS",
        "destroyed_projects": "#PROJECTS",
        "code_refactorings": "#PROJECTS",
        "code_completions": "#PROJECTS",
        "build_queue_terminations": "#PROJECTS",
        "run_queue_terminations": "#PROJECTS",
        "builds_time": "#PROJECTS",
        "debugs_time": "#PROJECTS",
        "runs_time": "#PROJECTS",
        "time_in_build_queue": "#PROJECTS",
        "time_in_run_queue": "#PROJECTS",
        "created_projects": "#PROJECTS",
        "projects": "#PROJECTS",

        "project_type_android": "#PROJECTS",
        "project_type_django": "#PROJECTS",
        "project_type_jar": "#PROJECTS",
        "project_type_javascript": "#PROJECTS",
        "project_type_jsp": "#PROJECTS",
        "project_type_mmp": "#PROJECTS",
        "project_type_nodejs": "#PROJECTS",
        "project_type_others": "#PROJECTS",
        "project_type_php": "#PROJECTS",
        "project_type_python": "#PROJECTS",
        "project_type_ruby": "#PROJECTS",
        "project_type_spring": "#PROJECTS",
        "project_type_war": "#PROJECTS",

        "project_paas_appfog": "#PROJECTS",
        "project_paas_aws": "#PROJECTS",
        "project_paas_cloudbees": "#PROJECTS",
        "project_paas_cloudfoundry": "#PROJECTS",
        "project_paas_gae": "#PROJECTS",
        "project_paas_heroku": "#PROJECTS",
        "project_paas_manymo": "#PROJECTS",
        "project_paas_openshift": "#PROJECTS",
        "project_paas_tier3": "#PROJECTS",

        "total_projects": "#PROJECTS",

        "project_no_paas_defined": "#PROJECTS",
        "project_paas_any": "#PROJECTS",

        "builds_finished": "#PROJECTS",
        "builds_with_timeout": "#PROJECTS",
        "builds_with_always_on": "#PROJECTS",
        "builds_finished_normally": "#PROJECTS",
        "builds_finished_by_timeout": "#PROJECTS",

        "runs_finished": "#PROJECTS",
        "runs_memory_usage_per_hour": "#PROJECTS",
        "runs_with_timeout": "#PROJECTS",
        "runs_with_always_on": "#PROJECTS",
        "runs_finished_by_user": "#PROJECTS",
        "runs_finished_by_timeout": "#PROJECTS",

        "change_project_type_action": "#PROJECTS",
        "close_project_action": "#PROJECTS",
        "delete_item_action": "#PROJECTS",
        "edit_custom_environments_action": "#PROJECTS",
        "environment_action": "#PROJECTS",
        "expand_editor_action": "#PROJECTS",
        "find_action_action": "#PROJECTS",
        "formatter_action": "#PROJECTS",
        "import_project_from_location_action": "#PROJECTS",
        "navigate_to_file_action": "#PROJECTS",
        "new_project_wizard_action": "#PROJECTS",
        "open_project_action": "#PROJECTS",
        "redirect_to_feedback_action": "#PROJECTS",
        "redirect_to_forums_action": "#PROJECTS",
        "redirect_to_help_action": "#PROJECTS",
        "redo_action": "#PROJECTS",
        "rename_item_action": "#PROJECTS",
        "save_action": "#PROJECTS",
        "save_all_action": "#PROJECTS",
        "show_about_action": "#PROJECTS",
        "show_preferences_action": "#PROJECTS",
        "undo_action": "#PROJECTS",
        "upload_file_action": "#PROJECTS",
        "new_css_file_action": "#PROJECTS",
        "new_less_file_action": "#PROJECTS",
        "new_html_file_action": "#PROJECTS",
        "new_java_script_file_action": "#PROJECTS",
        "build_action": "#PROJECTS",
        "clear_builder_console_action": "#PROJECTS",
        "clear_runner_console_action": "#PROJECTS",
        "custom_run_action": "#PROJECTS",
        "edit_images_action": "#PROJECTS",
        "get_logs_action": "#PROJECTS",
        "run_action": "#PROJECTS",
        "run_image_action": "#PROJECTS",
        "stop_action": "#PROJECTS",
        "view_recipe_action": "#PROJECTS",
        "sort_by_status_action": "#PROJECTS",
        "default_new_resource_action": "#PROJECTS",
        "new_file_action": "#PROJECTS",
        "new_folder_action": "#PROJECTS",
        "new_xml_file_action": "#PROJECTS",
        "shutdown_action": "#PROJECTS",
        "add_to_index_action": "#PROJECTS",
        "commit_action": "#PROJECTS",
        "delete_repository_action": "#PROJECTS",
        "fetch_action": "#PROJECTS",
        "history_action": "#PROJECTS",
        "init_repository_action": "#PROJECTS",
        "pull_action": "#PROJECTS",
        "import_project_from_git_hub_action": "#PROJECTS",
        "push_action": "#PROJECTS",
        "remove_from_index_action": "#PROJECTS",
        "reset_files_action": "#PROJECTS",
        "reset_to_commit_action": "#PROJECTS",
        "show_branches_action": "#PROJECTS",
        "show_git_url_action": "#PROJECTS",
        "show_merge_action": "#PROJECTS",
        "show_remote_action": "#PROJECTS",
        "show_status_action": "#PROJECTS",
        "manage_datasources_action": "#PROJECTS",
        "execute_sql_action": "#PROJECTS",
        "new_datasource_wizard_action": "#PROJECTS",
        "new_sql_file_action": "#PROJECTS",
        "sql_request_launcher_action": "#PROJECTS",
        "bower_install_action": "#PROJECTS",
        "export_config_action": "#PROJECTS",
        "import_from_config_action": "#PROJECTS",
        "permissions_action": "#PROJECTS",
        "share_factory_action": "#PROJECTS",
        "custom_grunt_run_action": "#PROJECTS",
        "npm_install_action": "#PROJECTS",
        "custom_build_action": "#PROJECTS",
        "debug_action": "#PROJECTS",
        "new_java_source_file_action": "#PROJECTS",
        "new_package_action": "#PROJECTS",
        "update_dependency_action": "#PROJECTS",

        /** SESSIONS */
        "factory_sessions_with_build": "#SESSIONS",
        "factory_sessions_with_deploy": "#SESSIONS",
        "factory_sessions_with_run": "#SESSIONS",
        "authenticated_factory_sessions": "#SESSIONS",
        "converted_factory_sessions": "#SESSIONS",
        "product_usage_sessions": "#SESSIONS",
        "factory_sessions": "#SESSIONS",
        "product_usage_factory_sessions": "#SESSIONS",
        "factory_product_usage_time_total": "#SESSIONS",

        "factory_sessions_with_build_percent": "#SESSIONS",
        "factory_sessions_with_deploy_percent": "#SESSIONS",
        "factory_sessions_with_run_percent": "#SESSIONS",

        "product_usage_sessions_above_60_min": "#SESSIONS",
        "product_usage_sessions_below_1_min": "#SESSIONS",
        "product_usage_sessions_between_10_and_60_min": "#SESSIONS",
        "product_usage_sessions_between_1_and_10_min": "#SESSIONS",

        "product_usage_time_above_60_min": "#SESSIONS",
        "product_usage_time_between_10_and_60_min": "#SESSIONS",
        "product_usage_time_between_1_and_10_min": "#SESSIONS",

        "factory_sessions_above_10_min": "#SESSIONS",
        "factory_sessions_below_10_min": "#SESSIONS",

        "abandoned_factory_sessions": "#SESSIONS",
        "non_factories_product_usage_sessions": "#SESSIONS",
        "product_usage_time_below_1_min": "#SESSIONS",
        "product_usage_time_total": "#SESSIONS",
        "anonymous_factory_sessions": "#SESSIONS",

        /** FACTORIES */
        "factory_used": "#FACTORIES",

        /** DEFAULT */
        "total_factories": "#DEFAULT",  // isn't 'FACTORIES' because total factory = created factory, and there could be no some created factories in 'product_usage_factory_sessions_list' collection
        "created_factories": "#DEFAULT"  // isn't 'FACTORIES' because there could be no some created factories in 'product_usage_factory_sessions_list' collection
    };
    var mapDrillDownPageTypeToAddress = {
        "#DEFAULT": defaultDrillDownPageAddress,
        "#USERS": "/analytics/pages/users-view.jsp",
        "#WORKSPACES": "/analytics/pages/workspaces-view.jsp",
        "#FACTORIES": "/analytics/pages/factories-view.jsp",
        "#PROJECTS": "/analytics/pages/projects-view.jsp",
        "#SESSIONS": "/analytics/pages/sessions-view.jsp"
    };

    var factoryUrlColumnNames = ["Factory URL", "Factory"];
    var workspaceColumnNames = ["Workspace"];
    var userColumnNames = ["User", "Created By"];
    var textColumnNames = [
        "Factory URL", 
        "Factory",
        "User", 
        "First Name",
        "Last Name", 
        "Company", 
        "Job", 
        "ID", 
        "Workspace", 
        "ws",
        "Referrer",
        "Organization", 
        "Project", 
        "Repository", 
        "State", 
        "Domain",
        "Type",
        "Project Type",
        "Event",
        "Date",
        "Start Time",
        "End Time",
        "Started",
        "Is Authenticated Session",
        "Is Converted Session"
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

    /**
     * Returns sub-property of property of widget.
     */
    function getSubProperty(widgetName, propertyName, subPropertyName, defaultValue) {
        var widgetProperty = widgetConfiguration[widgetName][propertyName];
        if (typeof widgetProperty == "undefined") {
            return defaultValue;
        }

        var widgetSubProperty = widgetProperty[subPropertyName];
        if (typeof widgetSubProperty == "undefined") {
            return defaultValue;
        }

        return widgetSubProperty;
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
    function isModelParamRegistered(modelParam) {
        return registeredModelParams.indexOf(modelParam) > -1;
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

    function getDrillDownPageAddress(metricName) {
        var drillDownPageType = mapExpandableMetricToDrillDownPageType[metricName];

        if (typeof drillDownPageType == "undefined") {
            return defaultDrillDownPageAddress;
        }

        return mapDrillDownPageTypeToAddress[drillDownPageType];
    }

    function getExpandableMetricName(widgetName, columnName) {
        var columnDrillDownPageLinkConfiguration = getProperty(widgetName, "columnDrillDownPageLinkConfiguration", {});

        var mapColumnNameToExpandableMetric = columnDrillDownPageLinkConfiguration["mapColumnNameToExpandableMetric"] || {};

        return mapColumnNameToExpandableMetric[columnName];
    }

    function isDrillDownPageType(pageType) {
        return typeof mapDrillDownPageTypeToAddress[pageType] != "undefined";
    }
    
    function getDrillDownPageAddressOnPageType(pageType) {
        return mapDrillDownPageTypeToAddress[pageType];
    }
    
    function isSpecificName(name, nameList) {
        if (typeof name != "string") {
            return false;
        }
        
        for (var i in nameList) {
            if (name.toLowerCase() == nameList[i].toLowerCase()) {
                return true;
            }
        }

        return false;
    }
    
    function isWorkspaceColumnName(columnName) {
        return isSpecificName(columnName, workspaceColumnNames);
    }

    function isUserColumnName(columnName) {
        return isSpecificName(columnName, userColumnNames);
    }

    function isFactoryUrlColumnName(columnName) {
        return isSpecificName(columnName, factoryUrlColumnNames);
    }
    
    function isTextColumnName(columnName) {
        return isSpecificName(columnName, textColumnNames);
    }
    

    /** ****************** API ********** */
    return {
        getProperty: getProperty,
        getSubProperty: getSubProperty,
        getWidgetNames: getWidgetNames,
        setupDefaultModelParams: setupDefaultModelParams,
        isModelParamRegistered: isModelParamRegistered,
        isParamGlobal: isParamGlobal,
        getGlobalParamList: getGlobalParamList,
        getServerProperty: getServerProperty,
        removeForbiddenModelParams: removeForbiddenModelParams,
        isDateParam: isDateParam,
        isSystemMessage: isSystemMessage,

        getDrillDownPageAddress: getDrillDownPageAddress,
        getExpandableMetricName: getExpandableMetricName,
        isDrillDownPageType: isDrillDownPageType,
        getDrillDownPageAddressOnPageType: getDrillDownPageAddressOnPageType,

        isFactoryUrlColumnName: isFactoryUrlColumnName,
        isWorkspaceColumnName: isWorkspaceColumnName,
        isUserColumnName: isUserColumnName,
        isTextColumnName: isTextColumnName
    }
}
