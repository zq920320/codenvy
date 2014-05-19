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
    "1day": {
        "descSortColumnNumber": 2
    },
    
    "7day": {
        "descSortColumnNumber": 3               
    },   
    
    "30day": {
        "descSortColumnNumber": 4
    },   
    
    "60day": {
        "descSortColumnNumber": 5
    },   
    
    "90day": {
        "descSortColumnNumber": 6
    },
    
    "365day": {
        "descSortColumnNumber": 7
    },
    
    "lifetime": {
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
            table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams);
            
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
};

analytics.presenter.TopMetricsPresenter.prototype.getModelViewName = function(modelParams) {
    var databaseTableMetricPrefix = modelParams.metric.toLowerCase();
    var databaseTableTimeunitSuffix = modelParams.time_unit.toLowerCase();
    return databaseTableMetricPrefix + "_by_" + databaseTableTimeunitSuffix;
}

/**
 * Try to find out parameters by metric, then by time_unit.
 */
analytics.presenter.TopMetricsPresenter.prototype.getColumnSortingParameters = function(metric, time_unit) {
    return this.clientSortParams[metric] || this.clientSortParams[time_unit] || {};
}