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
if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenter = analytics.presenter || {};

analytics.presenter.ReportPresenter = function ReportPresenter() {};

analytics.presenter.ReportPresenter.prototype = new Presenter();

/** Drill Down page parameters */
analytics.presenter.ReportPresenter.prototype.DEFAULT_DRILL_DOWN_PAGE_ADDRESS = "/analytics/pages/drill-down.jsp";
analytics.presenter.ReportPresenter.prototype.mapExpandedMetricToDrillDownPageType = {
    "active_users": "USERS",
    "users_who_created_project": "USERS",
    "users_who_built": "USERS",
    "users_who_deployed": "USERS",
    "users_who_deployed_to_paas": "USERS", 
    "users_who_invited": "USERS",
    "users_who_launched_shell": "USERS",
    "created_users": "USERS",
    "created_users_from_factory": "USERS",
    "user_invite": "USERS",
    "removed_users": "USERS",
    
    "active_workspaces": "WORKSPACES",
    "created_workspaces": "WORKSPACES",
    "temporary_workspaces_created": "WORKSPACES",
    "destroyed_workspaces": "WORKSPACES",
    "shell_launched": "WORKSPACES",
    "collaborative_sessions_started": "WORKSPACES",
    
    "builds": "PROJECTS",
    "deploys": "PROJECTS",
    "deploys_to_paas": "PROJECTS",
    "runs": "PROJECTS",
    "debugs": "PROJECTS",
    "destroyed_projects": "PROJECTS",
    "code_completions": "PROJECTS",
    "build_queue_terminations": "PROJECTS",
    "runner_total_time": "PROJECTS",
    "run_queue_terminations": "PROJECTS",
    "builds_time": "PROJECTS",
    "debugs_time": "PROJECTS",
    "runs_time": "PROJECTS",
    "time_in_build_queue": "PROJECTS",
    "time_in_run_queue": "PROJECTS",
    
    "factory_sessions_with_build": "SESSIONS",
    "factory_sessions_with_deploy": "SESSIONS",
    "factory_sessions_with_run": "SESSIONS",
    "authenticated_factory_sessions": "SESSIONS",
    "converted_factory_sessions": "SESSIONS",
    
};

analytics.presenter.ReportPresenter.prototype.mapDrillDownPageTypeToDrillDownPageAddress = {
    "USERS": "/analytics/pages/users-view.jsp?sort=%2Buser&",
    "WORKSPACES": "/analytics/pages/workspaces-view.jsp?sort=%2Bws&",
    "FACTORIES": "/analytics/pages/factories-view.jsp?sort=%2Bws_created&",
    "PROJECTS": "/analytics/pages/projects-view.jsp?",
    "SESSIONS": "/analytics/pages/sessions-view.jsp?sort=-date&",
}

analytics.presenter.ReportPresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    // get list of expandable metrics of report
    model.pushDoneFunction(function(data) {
        var viewParams = view.getParams();
        var modelParams = presenter.getModelParams(viewParams);
        model.setParams(modelParams);

        var expandableMetricPerSection = data;
        
        // get report data
        model.popDoneFunction();
        model.pushDoneFunction(function(data) {
            var doNotDisplayCSVButton = analytics.configuration.getProperty(presenter.widgetName, "doNotDisplayCSVButton", false);  // default value is "false" 
            var csvButtonLink = (doNotDisplayCSVButton)
                                ? undefined
                                : presenter.getLinkForExportToCsvButton();     
            var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
            view.printWidgetHeader(widgetLabel, csvButtonLink);
    
            view.print("<div class='body'>");
                
            for (var i in data) {
                var table = data[i];
                
                // add links to drill down page
                table = presenter.linkMetricValueWithDrillDownPage(table, i, expandableMetricPerSection, modelParams);            
                
                view.printTable(table, true);
            }
            
            var clientSortParams = analytics.configuration.getProperty(presenter.widgetName, "clientSortParams");
            view.loadTableHandlers(true, clientSortParams);   
    
            view.print("</div>");
            
            // finish loading widget
            analytics.views.loader.needLoader = false;
        });
    
        var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
        
    	model.getModelViewData(modelViewName);
    });

    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getExpandableMetricList(modelViewName);
};

/**
 * expandableMetricPerSection format: 
 * [
 * {"1": "total_factories",   // first section (first row with "0" key is title as usual in reports, and so is absent)
 *  "2": "created_factories",
 *  ...},
 *  
 * {},                        // second section (first row with "0" key is title as usual in reports, and so is absent)
 * 
 * {"2": "active_workspaces", // third section (first row with "0" key is title as usual in reports, and so is absent)
 *  "5": "active_users",
 *  ...},
 *  
 *  ...
 *  ]
 */
analytics.presenter.ReportPresenter.prototype.linkMetricValueWithDrillDownPage = function(table, tableNumber, expandableMetricPerSection, modelParams) { 
    // setup top date of expanded value due to date of generation of report
    modelParams["to_date"] = modelParams["to_date"] || analytics.configuration.getServerProperty("reportGenerationDate");
    
    for (var rowNumber = 0; rowNumber < table.rows.length; rowNumber++) {
        // check if there is expandable metric in row
        var metricName = expandableMetricPerSection[tableNumber][rowNumber + 1];  // taking into account absent title row
        if (typeof metricName != "undefined") {            
            for (var columnNumber = 1; columnNumber < table.rows[rowNumber].length; columnNumber++) {
                var columnValue = table.rows[rowNumber][columnNumber];
                
                // don't display link to empty drill down page
                if (! this.isEmptyValue(columnValue)) {
                    var timeInterval = columnNumber - 1;
                    var drillDownPageLink = this.getDrillDownPageLink(timeInterval, metricName, columnValue, modelParams);                
                    
                    table.rows[rowNumber][columnNumber] = "<a href='" + drillDownPageLink + "'>" + columnValue + "</a>";
                }
            }
        }        
    }
    
    return table;
}

analytics.presenter.ReportPresenter.prototype.getDrillDownPageLink = function(timeInterval, metricName, metricValue, modelParams) {
    var drillDownPageLink = this.getDrillDownPageAddress(metricName) + analytics.util.constructUrlParams(modelParams);
    drillDownPageLink += "&" + this.TIME_INTERVAL_PARAMETER + "=" + timeInterval;
    drillDownPageLink += "&" + this.METRIC_ORIGINAL_NAME_VIEW_PARAMETER + "=" + metricName;
    
    return drillDownPageLink;
}

/** 
 * @returns true if value = "0".
 * */
analytics.presenter.ReportPresenter.prototype.isEmptyValue = function(value) {
    return value == "0"  // 0 numeric value
           || value == "00:00:00";   // 0 time value
}

analytics.presenter.ReportPresenter.prototype.getDrillDownPageAddress = function(metricName) {
    var drillDownPageType = this.mapExpandedMetricToDrillDownPageType[metricName];

    if (typeof drillDownPageType == "undefined") {
        return this.DEFAULT_DRILL_DOWN_PAGE_ADDRESS;
    }
        
    return this.mapDrillDownPageTypeToDrillDownPageAddress[drillDownPageType];
}