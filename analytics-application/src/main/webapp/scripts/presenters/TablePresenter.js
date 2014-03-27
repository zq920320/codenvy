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

analytics.presenter.TablePresenter = function TablePresenter() {};

analytics.presenter.TablePresenter.prototype = new Presenter();

analytics.presenter.TablePresenter.prototype.DEFAULT_ONE_PAGE_ROWS_COUNT = 10;

analytics.presenter.TablePresenter.prototype.load = function() { 
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    var widgetName = presenter.widgetName;
    
    var modelViewName = analytics.configuration.getProperty(widgetName, "modelViewName");
    var viewParams = view.getParams();
    
    var onePageRowsCount = analytics.configuration.getProperty(widgetName, "onePageRowsCount", presenter.DEFAULT_ONE_PAGE_ROWS_COUNT);
    
    var modelParams = presenter.getModelParams(viewParams);
    
    // process sorting
    if (analytics.configuration.getProperty(widgetName, "isSortable", false)) {   // default value is "false"
        modelParams.sort = analytics.configuration.getProperty(widgetName, "defaultSortParams");
    }
    
    var isPaginable = analytics.configuration.getProperty(widgetName, "isPaginable", false);   // default value is "false"

    //process pagination
    if (isPaginable) {
        delete modelParams[widgetName];
        
        var currentPageNumber = viewParams[widgetName];  // search on table page number in parameter "{modelViewName}={page_number}"            
        if (typeof currentPageNumber == "undefined") {
            currentPageNumber = 1;
        } else {
            currentPageNumber = new Number(currentPageNumber);
        }
        modelParams.page = currentPageNumber;
        modelParams.per_page = onePageRowsCount;
        
        model.setParams(modelParams);
        model.pushDoneFunction(function(data){
            model.popDoneFunction(data);
   
            // default value is "false"
            var doNotDisplayCSVButton = analytics.configuration.getProperty(presenter.widgetName, "doNotDisplayCSVButton", false);
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
            
            presenter.printTable(csvButtonLink, table, true, currentPageNumber, onePageRowsCount, modelParams);
            
            // finish loading widget
            analytics.views.loader.needLoader = false;
        })
        
        model.getAllResults(modelViewName);

    } else {
        model.setParams(modelParams);
        
        model.pushDoneFunction(function(data) {
            var csvButtonLink = presenter.getLinkForExportToCsvButton();
            presenter.printTable(csvButtonLink, data[0], false);
            
            // finish loading widget
            analytics.views.loader.needLoader = false;
        });
        
        model.getAllResults(modelViewName);
    }
};

analytics.presenter.TablePresenter.prototype.printTable = function(csvButtonLink, table, isPaginable, currentPageNumber, onePageRowsCount, modelParams) {
    var presenter = this; 
    var view = presenter.view;
    
    var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
    view.printWidgetHeader(widgetLabel, csvButtonLink);

    view.print("<div class='body'>");
    view.printTable(table, false);
    
    // display pagination
    if (isPaginable) {
        delete modelParams.page;
        presenter.printTableNavigation(
            currentPageNumber,
            onePageRowsCount,
            modelParams
        );

        view.loadPageNavigationHandlers("analytics.main.reloadWidgetOnPageNavigation");
    } else {
        var columnSortingParameters = analytics.configuration.getProperty(presenter.widgetName, "columnSortingParameters");
        view.loadTableHandlers(true, columnSortingParameters);
    }
    
    view.print("</div>");
}

analytics.presenter.TablePresenter.prototype.printTableNavigation = function(currentPageNumber, onePageRowsCount, modelParams) { 
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    var widgetName = presenter.widgetName;
    var modelMetricName = analytics.configuration.getProperty(widgetName, "modelMetricName");
    
    model.setParams(modelParams);
    model.pushDoneFunction(function(data) {
        var pageCount = Math.ceil(data / onePageRowsCount);
        if (pageCount > 1) {
            var queryString = "?" + analytics.util.constructUrlParams(modelParams);

            view.printBottomPageNavigator(pageCount, currentPageNumber, queryString, presenter.widgetName);
            
            view.loadTableHandlers(false);  // don't display client side sorting for table with pagination
        } else {
            var columnSortingParameters = analytics.configuration.getProperty(presenter.widgetName, "columnSortingParameters");
            view.loadTableHandlers(true, columnSortingParameters);
        }
    });
    
    model.getMetricValue(modelMetricName);
}