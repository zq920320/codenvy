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

analytics.presenter.TablePresenter = function TablePresenter() {};

analytics.presenter.TablePresenter.prototype = new Presenter();

analytics.presenter.TablePresenter.prototype.ONE_PAGE_ROWS_COUNT = 10;

analytics.presenter.TablePresenter.prototype.load = function() { 
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    var widgetName = presenter.widgetName;
    
    var modelViewName = analytics.configuration.getProperty(widgetName, "modelViewName");
    var viewParams = view.getParams();
        
    // fix date range value format: fix "yyyy-mm-dd" on "yyyymmdd"
    if (typeof viewParams["from_date"] != "undefined") {
        viewParams["from_date"] = viewParams["from_date"].replace(/-/g, "");
    }
    if (typeof viewParams["to_date"] != "undefined") {
        viewParams["to_date"] = viewParams["to_date"].replace(/-/g, "");
    }
    
    var modelParams = analytics.util.clone(viewParams);
    
    // process sorting
    if (typeof analytics.configuration.getProperty(widgetName, "isSortable") != "undefined"
        && analytics.configuration.getProperty(widgetName, "isSortable")) {
        modelParams.sort = analytics.configuration.getProperty(widgetName, "defaultSortParams");
    }

    //process pagination
    if (typeof analytics.configuration.getProperty(widgetName, "isPaginable") != "undefined"
           && analytics.configuration.getProperty(widgetName, "isPaginable")) {
        delete modelParams[widgetName];
        
        var currentPageNumber = viewParams[widgetName];  // search on table page number in parameter "{modelViewName}={page_number}"            
        if (typeof currentPageNumber == "undefined") {
            currentPageNumber = 1;
        } else {
            currentPageNumber = new Number(currentPageNumber);
        }
        modelParams.page = currentPageNumber;
        modelParams.per_page = presenter.ONE_PAGE_ROWS_COUNT;
        
        model.setParams(modelParams);
        model.pushDoneFunction(function(data){
            model.popDoneFunction(data);

            view.printTable(data[0], false);
            
            delete modelParams.page;
            presenter.printTableNavigation(
                currentPageNumber, 
                modelParams
            );
            
            delete modelParams.sort;
            view.loadPageNavigationHandlers("analytics.main.reloadWidgetOnPageNavigation");
        })
        

        
        model.getAllResults(modelViewName);

    } else {
        model.setParams(modelParams);
        
        model.pushDoneFunction(function(data) {
            view.printTable(data[0], false);            
            view.loadTableHandlers(false);
        });
        
        model.getAllResults(modelViewName);
    }
};

analytics.presenter.TablePresenter.prototype.printTableNavigation = function(currentPageNumber, modelParams) { 
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    var widgetName = presenter.widgetName;
    var modelMetricName = analytics.configuration.getProperty(widgetName, "modelMetricName");
    
    model.setParams(modelParams);
    model.pushDoneFunction(function(data) {
        var pageCount = data / presenter.ONE_PAGE_ROWS_COUNT;
        if (pageCount > 1) {
            var queryString = "?" + analytics.util.constructUrlParams(modelParams);

            view.printBottomPageNavigator(pageCount, currentPageNumber, queryString, presenter.widgetName);
        }
        view.loadTableHandlers(false);
    });

    model.getMetricValue(modelMetricName);
}
