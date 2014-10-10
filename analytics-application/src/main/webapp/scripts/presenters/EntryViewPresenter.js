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
analytics.presenter.EntryViewPresenter = function EntryViewPresenter() {
};

// define prototype methods and properties
analytics.presenter.EntryViewPresenter.prototype = Presenter.prototype;

analytics.presenter.EntryViewPresenter.prototype.load = function () {
    var presenter = this;
    var view = presenter.view;
    var model = presenter.model;
    
    var viewParams = view.getParams();
    
    var modelParams = presenter.getModelParams(viewParams);
    
    // obtain page count
    // remove redundant params
    delete modelParams.page;
    delete modelParams.sort;
    delete modelParams.per_page;

    model.setParams(modelParams);

    presenter.displayEmptyWidget();
    
    presenter.obtainViewData(model, view, presenter);
}

analytics.presenter.EntryViewPresenter.prototype.obtainViewData = function (model, view, presenter) {
    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);

    // process pagination
    var currentPageNumber = modelParams.page;
    if (typeof currentPageNumber == "undefined") {
        currentPageNumber = 1;
    } else {
        currentPageNumber = new Number(currentPageNumber);
    }

    var onePageRowsCount = presenter.DEFAULT_ONE_PAGE_ROWS_COUNT;
    
    modelParams.per_page = onePageRowsCount;
    modelParams.page = currentPageNumber;

    model.pushDoneFunction(function (data) {
        view.print("<div class='body'>");

        var table = data[0];  // there is only one table in data
        table.original = analytics.util.clone(table, true, []);

        var pageCount = model.recognizePageCount(onePageRowsCount, currentPageNumber, table.rows.length);
        
        // add links to drill down page
        table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams);

        // make table columns linked 
        var columnCombinedLinkConf = analytics.configuration.getProperty(presenter.widgetName, "columnCombinedLinkConfiguration");
        if (typeof columnCombinedLinkConf != "undefined") {
            table = presenter.makeTableColumnCombinedLinked(table, columnCombinedLinkConf);
        }

        var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList");
        if (typeof columnLinkPrefixList != "undefined") {
            for (var columnName in columnLinkPrefixList) {
                table = presenter.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);
            }
        }

        if (pageCount != 1) {
            var viewParams = view.getParams();

            // make table header as linked for sorting
            var mapColumnToServerSortParam = analytics.configuration.getProperty(presenter.widgetName, "mapColumnToServerSortParam", undefined);
            table = presenter.addServerSortingLinks(table, presenter.widgetName, modelParams, mapColumnToServerSortParam);

            // print table
            view.printTable(table, false);

            // print bottom page navigation
            view.printBottomPageNavigator(pageCount, currentPageNumber, modelParams, presenter.CURRENT_PAGE_QUERY_PARAMETER, presenter.widgetName);

            view.loadTableHandlers(false);  // don't display client side sorting for table with pagination
        } else {
            // print table
            view.printTable(table, false);

            var clientSortParams = analytics.configuration.getProperty(presenter.widgetName, "clientSortParams");
            view.loadTableHandlers(true, clientSortParams);  // use client side sorting commands instead of links for server side sorting
        }

        view.print("</div>");

        // finish loading widget
        presenter.needLoader = false;
    });

    model.setParams(modelParams);

    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getModelViewData(modelViewName);
}

analytics.presenter.EntryViewPresenter.prototype.isInDrillDownPageRole = function (viewParams) {
    return typeof viewParams[this.METRIC_ORIGINAL_VALUE_VIEW_PARAMETER] != "undefined";
}
