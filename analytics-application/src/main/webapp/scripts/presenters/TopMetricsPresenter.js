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

analytics.presenter.TopMetricsPresenter.prototype.mapPassedDaysCountToClientSortParams = {
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
            table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams, modelViewName);
            
            // make table columns linked 
            var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList");
            if (typeof columnLinkPrefixList != "undefined") {
                for (var columnName in columnLinkPrefixList) {
                    table = presenter.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
                }          
            }             
            view.printTable(table, false);
        }

        var clientSortParams = presenter.getColumnSortingParameters(presenter.widgetName, modelParams.passed_days_count);
        view.loadTableHandlers(true, clientSortParams);

        view.print("</div>");
        
        // finish loading widget
        analytics.views.loader.needLoader = false;
    });

    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getModelViewData(modelViewName);
}

/**
 * Get client sorting parameters from configuration of widget or mapPassedDaysCountToClientSortParams map.
 */
analytics.presenter.TopMetricsPresenter.prototype.getColumnSortingParameters = function(widgetName, passedDaysCount) {   
    return analytics.configuration.getProperty(widgetName, "clientSortParams") 
            || this.mapPassedDaysCountToClientSortParams[passedDaysCount]
            || {};
}

analytics.presenter.TopMetricsPresenter.prototype.linkTableValuesWithDrillDownPage = function(widgetName, table, modelParams, modelViewName) {
    var modelParams = analytics.util.clone(modelParams);
        
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
            table = this.linkColumnValuesWithDrillDownPage(table, 
                                      columnIndex, 
                                      modelViewName, 
                                      sourceColumnIndexes, 
                                      mapColumnToParameter, 
                                      doNotLinkOnEmptyParameter,
                                      expandedMetricName,
                                      modelParams);
        }
    }          
        
    return table;
}

/**
 * Insert drill down page link in the rows of column.  
 */
analytics.presenter.TopMetricsPresenter.prototype.linkColumnValuesWithDrillDownPage = function(table, 
                                                                          columnIndex, 
                                                                          modelViewName, 
                                                                          sourceColumnIndexes,
                                                                          mapColumnToParameter, 
                                                                          doNotLinkOnEmptyParameter,
                                                                          expandedMetricName,
                                                                          modelParams) {
    for (var i = 0; i < table.rows.length; i++) {
        var row = table.rows[i];
        
        var columnNameValue = this.getColumnNameValue(row[columnIndex]);
        
        if (! this.isEmptyValue(columnNameValue)) {
            modelParams = this.updatePassedDaysCount(columnIndex, modelViewName, modelParams);
            
            var drillDownPageLink = this.getDrillDownPageLink(expandedMetricName, modelParams);
            
            // calculation combined link like "ws=...&project=..."
            if (sourceColumnIndexes.length > 0) {
                drillDownPageLink += "&" + this.getUrlParamsForCombineColumnLink(row, 
                                                                                 sourceColumnIndexes, 
                                                                                 mapColumnToParameter, 
                                                                                 doNotLinkOnEmptyParameter);
            }
            
            row[columnIndex] = "<a href='" + drillDownPageLink + "'>" + columnNameValue + "</a>";
        }
    }

    return table;
}

/**
 * Fix "passed_days_count" parameter in case of time columns of "Top Users", "Top Domains", "Top Companies" reports.
 */
analytics.presenter.TopMetricsPresenter.prototype.updatePassedDaysCount = function(columnIndex, modelViewName, modelParams) {
    if (columnIndex > 1 
            && (modelViewName == "top_users" 
            || modelViewName == "top_domains"
            || modelViewName == "top_companies")) {
           
           var passedDaysCountIndex = columnIndex - 2;  // don't take into account first two columns
           modelParams.passed_days_count = Object.keys(this.mapPassedDaysCountToClientSortParams)[passedDaysCountIndex]; 
    }
    
    return modelParams;
}