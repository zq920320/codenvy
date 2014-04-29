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

analytics.presenter.HorizontalTablePresenter = function HorizontalTablePresenter() {};

analytics.presenter.HorizontalTablePresenter.prototype = new Presenter();

analytics.presenter.HorizontalTablePresenter.prototype.DEFAULT_ONE_PAGE_ROWS_COUNT = 10;

analytics.presenter.HorizontalTablePresenter.prototype.load = function() { 
    var presenter = this; 
    var model = presenter.model;
    var widgetName = presenter.widgetName;
    
    var view = presenter.view;
    var viewParams = view.getParams();       
    var modelParams = presenter.getModelParams(viewParams);
    
    var isPaginable = analytics.configuration.getProperty(widgetName, "isPaginable", false);   // default value is "false"

    //process pagination
    if (isPaginable) {
        // get page count
        model.pushDoneFunction(function(data) {            
            var onePageRowsCount = analytics.configuration.getProperty(widgetName, "onePageRowsCount", presenter.DEFAULT_ONE_PAGE_ROWS_COUNT);            
            var pageCount = Math.ceil(data / onePageRowsCount) ;
            
            var currentPageNumber = viewParams[widgetName] || 1;  // search on table page number in parameter "{modelViewName}={page_number}"            
            modelParams.page = currentPageNumber;
            modelParams.per_page = onePageRowsCount;
            
            model.setParams(modelParams);
            model.popDoneFunction();
            model.pushDoneFunction(function(data) {   
                var modelParams = presenter.getModelParams(viewParams);  // restore initial model params
                var doNotDisplayCSVButton = analytics.configuration.getProperty(widgetName, "doNotDisplayCSVButton", false);  // default value is "false"
                var csvButtonLink = (doNotDisplayCSVButton) 
                                    ? undefined
                                    : presenter.getLinkForExportToCsvButton();
                
                var table = data[0];  // there is only one table in data

                // make table columns linked 
                var columnLinkPrefixList = analytics.configuration.getProperty(widgetName, "columnLinkPrefixList");
                if (typeof columnLinkPrefixList != "undefined") {
                    for (var columnName in columnLinkPrefixList) {
                        table = view.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
                    }          
                }       
                
                modelParams[widgetName] = modelParams.page;
                delete modelParams.page;    // remove page parameter
                delete modelParams.per_page;    // remove page parameter
                
                if (pageCount > 1) {                   
                    // make table header as linked for sorting
                    table = presenter.addServerSortingLinks(table, widgetName, modelParams);  
                    
                    // print table
                    presenter.printTable(csvButtonLink, table);
                    
                    // print bottom page navigation
                    delete modelParams[widgetName];  // remove old page number
                    var queryString = "?" + analytics.util.constructUrlParams(modelParams);
                    view.printBottomPageNavigator(pageCount, currentPageNumber, queryString, widgetName, widgetName);
                    
                    view.loadTableHandlers(false);  // don't display client side sorting for table with pagination
                } else {
                    // print table
                    presenter.printTable(csvButtonLink, table, widgetName + "_table");

                    // display client sorting
                    var clientSortParams = analytics.configuration.getProperty(widgetName, "clientSortParams");
                    view.loadTableHandlers(true, clientSortParams, widgetName + "_table");
                }
                
                // finish loading widget
                analytics.views.loader.needLoader = false;
            })
            
            var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
            model.getAllResults(modelViewName);
        });
        
        model.setParams(modelParams);
        
        var modelMetricName = analytics.configuration.getProperty(widgetName, "modelMetricName");
        model.getMetricValue(modelMetricName);

    } else {
        model.setParams(modelParams);
        
        model.pushDoneFunction(function(data) {
            var csvButtonLink = presenter.getLinkForExportToCsvButton();

            // print table
            var table = data[0];  // there is only one table in data

            // make table columns linked
            var columnLinkPrefixList = analytics.configuration.getProperty(widgetName, "columnLinkPrefixList");
            if (typeof columnLinkPrefixList != "undefined") {
                for (var columnName in columnLinkPrefixList) {
                    table = view.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);
                }
            }

            presenter.printTable(csvButtonLink, table, widgetName + "_table");


            // display client sorting
            var clientSortParams = analytics.configuration.getProperty(widgetName, "clientSortParams");
            view.loadTableHandlers(true, clientSortParams, widgetName + "_table");
            
            // finish loading widget
            analytics.views.loader.needLoader = false;
        });

        var modelViewName = analytics.configuration.getProperty(widgetName, "modelViewName");
        model.getAllResults(modelViewName);
    }
};

analytics.presenter.HorizontalTablePresenter.prototype.printTable = function(csvButtonLink, table, tableId) { 
    var view = this.view;
    
    var widgetLabel = analytics.configuration.getProperty(this.widgetName, "widgetLabel");
    view.printWidgetHeader(widgetLabel, csvButtonLink);

    view.print("<div class='body'>");
    view.printTable(table, false, tableId);
        
    view.print("</div>");
}