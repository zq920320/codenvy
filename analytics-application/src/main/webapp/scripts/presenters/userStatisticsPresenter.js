if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenters = analytics.presenters || {};
analytics.presenters.userStatistics = new Presenter();

analytics.presenters.userStatistics.ONE_PAGE_ROWS_COUNT = 1;
analytics.presenters.userStatistics.USERS_OVERVIEW_PAGE_LINK = "user-view.jsp";

analytics.presenters.userStatistics.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    var modelParams = view.getParams();
    
    // table names in order of receiving from server
    var databaseToUIMap = {
       databaseViews: ["user_data", "user-sessions", "user_workspace_data", "user_activity"],
//       tablePageCountResources: [undefined, "product_usage_sessions", "users_workspaces", "users_activity"],
       tablePageCountResources: [undefined, "product_usage_sessions", undefined, "users_activity"],       
       uiTables: ["User Statistics", "Sessions", "Workspaces", "User Logs"],
       sort: [undefined, "-date", undefined, "-date"]
    };
    
    // fix date range value format: fix "yyyy-mm-dd" on "yyyymmdd"
    if (typeof modelParams["from_date"] != "undefined") {
       modelParams["from_date"] = modelParams["from_date"].replace(/-/g, "");
    }
    if (typeof modelParams["to_date"] != "undefined") {
       modelParams["to_date"] = modelParams["to_date"].replace(/-/g, "");
    }
    
    modelParams.per_page = presenter.ONE_PAGE_ROWS_COUNT;
    
    view.print("<div class='view'>");
    view.print("<div class='tables'>");
    
    for (var i = 0; i < databaseToUIMap.databaseViews.length; i++) {
        view.print("<div class='item'>");
        view.print("<div class='header'>" + databaseToUIMap.uiTables[i] + "</div>");
        view.print("<div class='body'>");
    
        // process sorting
        if (typeof databaseToUIMap.sort[i] != "undefined") {
            modelParams.sort = databaseToUIMap.sort[i];
        }
       
        if (typeof databaseToUIMap.tablePageCountResources[i] != "undefined") {
            //process pagination
            var currentPageNumber = modelParams[databaseToUIMap.databaseViews[i]];  // search on table page number in parameter "{ui_table_name}={page_number}"          if (typeof currentPageNumber == "undefined") {
            if (typeof currentPageNumber == "undefined") {
                currentPageNumber = 1;
            } else {
                currentPageNumber = new Number(currentPageNumber);
                delete modelParams[databaseToUIMap.databaseViews[i]];
            }
            modelParams.page = currentPageNumber;
            model.setParams(modelParams);
            
            data = model.getAllResults(databaseToUIMap.databaseViews[i], false);
            
            view.printTable(data[0], false);

            delete modelParams.page;
            presenter.printTableNavigation(
                   databaseToUIMap.tablePageCountResources[i],
                   currentPageNumber, modelParams,
                   databaseToUIMap.databaseViews[i]
            );
        } else {
            model.setParams(modelParams);
            data = model.getAllResults(databaseToUIMap.databaseViews[i], false);
            view.printTable(data[0], false);
        }
       
        view.print("</div>");
        view.print("</div>");
        view.print("<br />");
    }
    
    view.loadTableHandlers(false);
    view.loadPageNavigationHandlers("analytics.main.reloadWidgetOnPageNavigation");
    
    view.print("</div>");
    view.print("</div>");
    
    model.doneFunction(data);
};

analytics.presenters.userStatistics.printTableNavigation = function(tablePageCountResource, currentPageNumber, modelParams, tableName) {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;    
    
    model.setParams(modelParams);    
    var data = model.getMetricValue(tablePageCountResource, false);
    var pageCount = data / this.ONE_PAGE_ROWS_COUNT;
    if (pageCount > 1) {
        var queryString = this.USERS_OVERVIEW_PAGE_LINK + "?" + analytics.util.constructUrlParams(modelParams);

        view.printBottomPageNavigator(pageCount, currentPageNumber, queryString, tableName);
    }
}
