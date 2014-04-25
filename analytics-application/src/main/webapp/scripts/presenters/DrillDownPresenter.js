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

analytics.presenter.DrillDownPresenter = function DrillDownPresenter() {};

analytics.presenter.DrillDownPresenter.prototype = new Presenter();

analytics.presenter.DrillDownPresenter.prototype.load = function() { 
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);

    model.setParams(modelParams);
    
    var pageCount = 1;

    model.pushDoneFunction(function(data) {        
        var table = data[0];  // there should be at most one table in data
        
        // check on empty answer from server
        if (typeof table == "undefined") {
            // finish loading widget
            analytics.views.loader.needLoader = false;
            return;
        }
        
        var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
        view.printWidgetHeader(widgetLabel);            
        
        view.print("<div class='body'>");
        
        // make table columns linked 
        var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList");
        if (typeof columnLinkPrefixList != "undefined") {
            for (var columnName in columnLinkPrefixList) {
                table = view.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
            }                
        }
        
        if (pageCount > 1) {
            // make table header as linked for sorting
            var mapColumnToServerSortParam = analytics.configuration.getProperty(presenter.widgetName, "mapColumnToServerSortParam", undefined);
            table = presenter.addServerSortingLinks(table, presenter.widgetName, viewParams, mapColumnToServerSortParam, true);                
            
            // print table
            view.printTable(table, false);
           
            view.loadTableHandlers(false);  // don't display client side sorting for table with pagination
        } else {
            // print table
            view.printTable(table, false); 
            
            var clientSortParams = analytics.configuration.getProperty(presenter.widgetName, "clientSortParams");
            view.loadTableHandlers(true, clientSortParams);  // use client side sorting commands instead of links for server side sorting
        }
        
        view.print("</div>");
        
        // finish loading widget
        analytics.views.loader.needLoader = false;
    });
    
    model.setParams(modelParams);
    
    var modelExpandedMetricName = viewParams[presenter.METRIC_ORIGINAL_NAME_VIEW_PARAMETER];
    model.getExpandedMetricValue(modelExpandedMetricName);        
}