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
// create object instance
analytics.presenter = analytics.presenter || {};
analytics.presenter.EntryViewPresenter = function EntryViewPresenter() {};

// define prototype methods and properties
analytics.presenter.EntryViewPresenter.prototype = Presenter.prototype;

analytics.presenter.EntryViewPresenter.prototype.CURRENT_PAGE_QUERY_PARAMETER = "page";
analytics.presenter.EntryViewPresenter.prototype.ONE_PAGE_ROWS_COUNT = 20;

analytics.presenter.EntryViewPresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);

    // remove redundant params
    delete modelParams.page;
    delete modelParams.sort;
    delete modelParams.per_page; 

    model.setParams(modelParams);
    
    // get page count    
    model.pushDoneFunction(function(data) {
        var viewParams = view.getParams();
        var modelParams = presenter.getModelParams(viewParams);
        
        var pageCount = Math.ceil(data / presenter.ONE_PAGE_ROWS_COUNT) ;
    
        // process pagination
        var currentPageNumber = modelParams.page;
        if (typeof currentPageNumber == "undefined") {
           currentPageNumber = 1;
        } else {
           currentPageNumber = new Number(currentPageNumber);
        }
        
        modelParams.per_page = presenter.ONE_PAGE_ROWS_COUNT;
        modelParams.page = currentPageNumber;

        model.popDoneFunction();
        model.pushDoneFunction(function(data) {
            var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
            view.printWidgetHeader(widgetLabel);            
            
            view.print("<div class='body'>");
            
            var table = data[0];  // there is only one table in data
            
            // make table columns linked 
            var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList");
            if (typeof columnLinkPrefixList != "undefined") {
                for (var columnName in columnLinkPrefixList) {
                    table = view.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
                }                
            }
            
            if (pageCount > 1) {
                // make table header as linked for sorting
                table = presenter.addServerSortingLinks(table, presenter.widgetName, modelParams);                
                
                // print table
                view.printTable(table, false);                 
            
                // print bottom page navigation
                delete modelParams.page;    // remove page parameter

                var queryString = analytics.util.getCurrentPageName() + "?" + analytics.util.constructUrlParams(modelParams);
            
                view.printBottomPageNavigator(pageCount, currentPageNumber, queryString, presenter.CURRENT_PAGE_QUERY_PARAMETER, presenter.widgetName);
               
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
        
        var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
        model.getAllResults(modelViewName);        
    });        
    
    var modelMetricName = analytics.configuration.getProperty(presenter.widgetName, "modelMetricName");
    model.getMetricValue(modelMetricName);
}