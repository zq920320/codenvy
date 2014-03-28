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
    
    var viewParams = presenter.view.getParams();       
    var modelParams = presenter.getModelParams(viewParams);
    
    var isPaginable = analytics.configuration.getProperty(widgetName, "isPaginable", false);   // default value is "false"

    //process pagination
    if (isPaginable) {
        // get page count
        model.pushDoneFunction(function(data) {
            var model = presenter.model;
            var viewParams = presenter.view.getParams();
            presenter.modelParams = presenter.getModelParams(viewParams);
            var modelParams = presenter.modelParams;
            
            var onePageRowsCount = analytics.configuration.getProperty(presenter.widgetName, "onePageRowsCount", presenter.DEFAULT_ONE_PAGE_ROWS_COUNT);            
            presenter.pageCount = Math.ceil(data / onePageRowsCount) ;
            
            presenter.currentPageNumber = viewParams[presenter.widgetName] || 1;  // search on table page number in parameter "{modelViewName}={page_number}"            
            modelParams.page = presenter.currentPageNumber;
            modelParams.per_page = onePageRowsCount;
            
            model.setParams(modelParams);
            model.popDoneFunction();
            model.pushDoneFunction(function(data) {   
                var view = presenter.view;
                var modelParams = presenter.modelParams;
                var doNotDisplayCSVButton = analytics.configuration.getProperty(presenter.widgetName, "doNotDisplayCSVButton", false);  // default value is "false"
                var csvButtonLink = (doNotDisplayCSVButton) 
                                    ? undefined
                                    : presenter.getLinkForExportToCsvButton();
                
                var table = data[0];  // there is only one table in data

                // make table columns linked 
                var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList");
                if (typeof columnLinkPrefixList != "undefined") {
                    for (var columnName in columnLinkPrefixList) {
                        table = view.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
                    }          
                }       
                
                modelParams[presenter.widgetName] = modelParams.page;
                delete modelParams.page;    // remove page parameter
                delete modelParams.per_page;    // remove page parameter
                
                if (presenter.pageCount > 1) {                   
                    // make table header as linked for sorting
                    table = presenter.addServerSortingLinks(table, presenter.widgetName, modelParams);  
                    
                    // print table
                    presenter.printTable(csvButtonLink, table);
                    
                    // print bottom page navigation
                    delete modelParams[presenter.widgetName];  // remove old page number
                    var queryString = "?" + analytics.util.constructUrlParams(modelParams);
                    view.printBottomPageNavigator(presenter.pageCount, presenter.currentPageNumber, queryString, presenter.widgetName);
                    view.loadPageNavigationHandlers("analytics.main.reloadWidgetOnPageNavigation");
                    
                    view.loadTableHandlers(false);  // don't display client side sorting for table with pagination
                } else {
                    // print table
                    presenter.printTable(csvButtonLink, table);

                    // display client sorting
                    var clientSortParams = analytics.configuration.getProperty(presenter.widgetName, "clientSortParams");
                    view.loadTableHandlers(true, clientSortParams);
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
            // print table
            var csvButtonLink = presenter.getLinkForExportToCsvButton();
            presenter.printTable(csvButtonLink, data[0]);
            
            // display client sorting
            var clientSortParams = analytics.configuration.getProperty(presenter.widgetName, "clientSortParams");
            presenter.view.loadTableHandlers(true, clientSortParams);
            
            // finish loading widget
            analytics.views.loader.needLoader = false;
        });

        var modelViewName = analytics.configuration.getProperty(widgetName, "modelViewName");
        model.getAllResults(modelViewName);
    }
};

analytics.presenter.HorizontalTablePresenter.prototype.printTable = function(csvButtonLink, table) { 
    var view = this.view;
    
    var widgetLabel = analytics.configuration.getProperty(this.widgetName, "widgetLabel");
    view.printWidgetHeader(widgetLabel, csvButtonLink);

    view.print("<div class='body'>");
    view.printTable(table, false);
        
    view.print("</div>");
}