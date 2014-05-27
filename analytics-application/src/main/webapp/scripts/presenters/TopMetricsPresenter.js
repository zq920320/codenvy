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

analytics.presenter.TopMetricsPresenter = function TopMetricsPresenter() {};

analytics.presenter.TopMetricsPresenter.prototype = new Presenter();

analytics.presenter.TopMetricsPresenter.prototype.clientSortParams = {
    "top_factory_sessions": {
        "descSortColumnNumber": 0
    },
    
    "top_factories": {
        "descSortColumnNumber": 3
    },
    
    "top_referrers": {
        "descSortColumnNumber": 3
    },
    
    // next parameters are dedicated to top_users, top_domains and top_companies reports only
    "by_1_day": {
        "descSortColumnNumber": 2
    },
    
    "by_7_days": {
        "descSortColumnNumber": 3               
    },   
    
    "by_30_days": {
        "descSortColumnNumber": 4
    },   
    
    "by_60_days": {
        "descSortColumnNumber": 5
    },   
    
    "by_90_days": {
        "descSortColumnNumber": 6
    },
    
    "by_365_days": {
        "descSortColumnNumber": 7
    },
    
    "by_lifetime": {
        "descSortColumnNumber": 8
    },
}

analytics.presenter.TopMetricsPresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    var modelParams = presenter.getModelParams(view.getParams())

    presenter.modelViewName = presenter.getModelViewName(modelParams);

    // save metric and time_unit params for client side sorting
    presenter.metric = modelParams.metric;
    presenter.time_unit = modelParams.time_unit;
    
    // remove unnecessary params 
    delete modelParams.metric;
    delete modelParams.time_unit;
    
    model.setParams(modelParams);
    
    model.pushDoneFunction(function(data) {
        // default value is "false"
        var doNotDisplayCSVButton = analytics.configuration.getProperty(presenter.widgetName, "doNotDisplayCSVButton", false);
        var csvButtonLink = (doNotDisplayCSVButton) 
                            ? undefined
                            : presenter.getLinkForExportToCsvButton(presenter.modelViewName);  
        
        var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
        view.printWidgetHeader(widgetLabel, csvButtonLink);            
        
        view.print("<div class='body'>");
        
        for (var tableIndex in data) {
            var table = data[tableIndex];
            
            // add links to drill down page
            table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams, presenter.time_unit);
            
            // make table columns linked 
            var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList");
            if (typeof columnLinkPrefixList != "undefined") {
                for (var columnName in columnLinkPrefixList) {
                    table = presenter.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
                }          
            }             
            view.printTable(table, false);
        }

        var clientSortParams = presenter.getColumnSortingParameters(presenter.metric, presenter.time_unit);
        view.loadTableHandlers(true, clientSortParams);

        view.print("</div>");
        
        // finish loading widget
        analytics.views.loader.needLoader = false;
    });

    model.getModelViewData(presenter.modelViewName);
}

analytics.presenter.TopMetricsPresenter.prototype.getModelViewName = function(modelParams) {
    var databaseTableMetricPrefix = modelParams.metric.toLowerCase();
    var databaseTableTimeunitSuffix = modelParams.time_unit.toLowerCase();
    return databaseTableMetricPrefix + "_" + databaseTableTimeunitSuffix;
}

/**
 * Try to find out parameters by metric, then by time_unit.
 */
analytics.presenter.TopMetricsPresenter.prototype.getColumnSortingParameters = function(metric, timeUnit) {
    return this.clientSortParams[metric] || this.clientSortParams[timeUnit] || {};
}

analytics.presenter.TopMetricsPresenter.prototype.linkTableValuesWithDrillDownPage = function(widgetName, table, modelParams, timeUnit) {
    var modelParams = analytics.util.clone(modelParams);
    modelParams.time_unit = timeUnit;
    
    // setup top date of expanded value due to date of generation of report
    modelParams["to_date"] = modelParams["to_date"] || analytics.configuration.getServerProperty("reportGenerationDate");
    
    var mapColumnToParameter = analytics.configuration.getSubProperty(widgetName, 
                                                                      "columnDrillDownPageLinkConfiguration", 
                                                                      "mapColumnToParameter", 
                                                                      {});
    
    var doNotLinkOnEmptyParameter = analytics.configuration.getSubProperty(widgetName, 
                                                                           "columnDrillDownPageLinkConfiguration", 
                                                                           "doNotLinkOnEmptyParameter", 
                                                                           true);
    
    // calculate source column indexes for combine links
    var sourceColumnIndexes = [];
    for (var sourceColumnName in mapColumnToParameter) {
        var sourceColumnIndex = analytics.util.getColumnIndexByColumnName(table.columns, sourceColumnName);
        sourceColumnIndexes.push(sourceColumnIndex);
    }
       
    for (var columnIndex = 0; columnIndex < table.columns.length; columnIndex++) {
        var columnName = table.columns[columnIndex];            
        
        var expandedMetricName = analytics.configuration.getExpandableMetricName(widgetName, columnName);
        if (typeof expandedMetricName != "undefined") {
            for (var i = 0; i < table.rows.length; i++) {
                var columnValue = table.rows[i][columnIndex];
                
                if (! this.isEmptyValue(columnValue)) {
                    var timeInterval;
                    if (metric == "top_users" 
                         || metric == "top_domains"
                         || metric == "top_companies" ) {
                        var timeInterval = columnNumber;  // don't take into account first two columns
                    }
                    
                    var drillDownPageLink = this.getDrillDownPageLink(expandedMetricName, modelParams, timeInterval);
                    
                    // calculation combined link like "ws=...&project=..."
                    if (sourceColumnIndexes.length > 0) {
                        drillDownPageLink += "&" + this.getUrlParamsForCombineColumnLink(table.rows[i], 
                                                                                         sourceColumnIndexes, 
                                                                                         mapColumnToParameter, 
                                                                                         doNotLinkOnEmptyParameter);
                    }
                    
                    table.rows[i][columnIndex] = "<a href='" + drillDownPageLink + "'>" + columnValue + "</a>";
                }
            }
        }
    }          
        
    return table;
}