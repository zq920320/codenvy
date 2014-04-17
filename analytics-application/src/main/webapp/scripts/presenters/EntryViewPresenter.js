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

analytics.presenter.EntryViewPresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);

    // obtain page count
    if (this.isInDrillDownPageRole(viewParams)) {
        // get page count from special parameter. It could be "Not a Number" (NaN)                       
        var rowCountString = viewParams[presenter.METRIC_ORIGINAL_VALUE_VIEW_PARAMETER];
        rowCountString = rowCountString.replace(/,/g, "");  // remove thousand delimiters in case of numbers like "5,120,954"
        
        var rowCount = new Number(rowCountString);
        if (rowCount.toString() != "NaN") {
            var onePageRowsCount = analytics.configuration.getProperty(presenter.widgetName, "onePageRowsCount", presenter.DEFAULT_ONE_PAGE_ROWS_COUNT);
            var pageCount = Math.ceil(rowCount / onePageRowsCount);
        } else {
            var pageCount = 1; 
        }
        
        presenter.obtainViewData(model, view, presenter, pageCount);

    } else {
        // remove redundant params
        delete modelParams.page;
        delete modelParams.sort;
        delete modelParams.per_page; 
    
        model.setParams(modelParams);
        
        // get page count    
        model.pushDoneFunction(function(data) {
            model.popDoneFunction();
            
            var pageCount = Math.ceil(data / presenter.DEFAULT_ONE_PAGE_ROWS_COUNT) ;
        
            presenter.obtainViewData(model, view, presenter, pageCount);
        });        
        
        var modelMetricName = analytics.configuration.getProperty(presenter.widgetName, "modelMetricName");
        model.getMetricValue(modelMetricName);
    }
}

analytics.presenter.EntryViewPresenter.prototype.obtainViewData = function(model, view, presenter, pageCount) {
    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);
    
    // process pagination
    var currentPageNumber = modelParams.page;
    if (typeof currentPageNumber == "undefined") {
       currentPageNumber = 1;
    } else {
       currentPageNumber = new Number(currentPageNumber);
    }
    
    modelParams.per_page = presenter.DEFAULT_ONE_PAGE_ROWS_COUNT;
    modelParams.page = currentPageNumber;

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
            var viewParams = view.getParams();
            
            // make table header as linked for sorting
            var mapColumnToServerSortParam = analytics.configuration.getProperty(presenter.widgetName, "mapColumnToServerSortParam", undefined);
            // add sorting link only in the header of the first column of drill down page
            if (presenter.isInDrillDownPageRole(viewParams)) {
                mapColumnToServerSortParam = analytics.util.getObjectWithFirstPopertyOnly(mapColumnToServerSortParam);
            }
            table = presenter.addServerSortingLinks(table, presenter.widgetName, viewParams, mapColumnToServerSortParam);                
            
            // print table
            view.printTable(table, false);                 
        
            // print bottom page navigation
            var queryString = analytics.util.getCurrentPageName() + "?" + analytics.util.constructUrlParams(viewParams);
        
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
    model.getModelViewData(modelViewName);
}

analytics.presenter.EntryViewPresenter.prototype.isInDrillDownPageRole = function(viewParams) {
    return typeof viewParams[this.METRIC_ORIGINAL_VALUE_VIEW_PARAMETER] != "undefined";
}