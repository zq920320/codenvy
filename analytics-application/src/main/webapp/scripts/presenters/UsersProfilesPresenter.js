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

analytics.presenter.UsersProfilesPresenter = function UsersProfilesPresenter() {};

analytics.presenter.UsersProfilesPresenter.prototype = new Presenter();

analytics.presenter.UsersProfilesPresenter.prototype.CURRENT_PAGE_QUERY_PARAMETER = "page";
analytics.presenter.UsersProfilesPresenter.prototype.ONE_PAGE_ROWS_COUNT = 20;
analytics.presenter.UsersProfilesPresenter.prototype.SORTING_PARAMETER = "sort";
analytics.presenter.UsersProfilesPresenter.prototype.ASCENDING_ORDER_PREFIX = "+";
analytics.presenter.UsersProfilesPresenter.prototype.DESCENDING_ORDER_PREFIX = "-";
analytics.presenter.UsersProfilesPresenter.prototype.DEFAULT_ORDER_PREFIX = analytics.presenter.UsersProfilesPresenter.prototype.ASCENDING_ORDER_PREFIX;

analytics.presenter.UsersProfilesPresenter.prototype.USER_ID_LINK_PREFIX = "user-view.jsp?user";
analytics.presenter.UsersProfilesPresenter.prototype.USERS_OVERVIEW_PAGE_LINK = "users-profiles.jsp";

analytics.presenter.UsersProfilesPresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    var viewParams = view.getParams();
    var modelParams = presenter.getModelParams(viewParams);

    // remove redundant params
    delete modelParams.page;
    delete modelParams.sort;
    delete modelParams.per_page;

    var sortingParameterValue = modelParams.sort || null;
    
    // fix "user" param name on "_id"
    if (typeof viewParams["Email"] != "undefined") {
        modelParams["_id"] = viewParams["Email"];
    }

    model.setParams(modelParams);
    
    // get page count    
    model.pushDoneFunction(function(data) {
        var viewParams = view.getParams();
        var modelParams = presenter.getModelParams(viewParams);
        
        var sortingParameterValue = modelParams.sort || null;
                
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
            var table = data[0];  // there is only one table in data
            
            // make user id in first column as linked 
            for (var i = 0; i < table.rows.length; i++) {
               var userId = table.rows[i][0];
               var href = presenter.USER_ID_LINK_PREFIX + "=" + userId;
               table.rows[i][0] = "<a href='" + href + "'>" + userId + "</a>";
            }
            
            
            // make table header as linked for sorting         
            for (var i = 0; i < table.columns.length; i++) {
               var columnName = table.columns[i];
               var sortingColumnParameter = analytics.configuration.getModelParamName(columnName);
               
               var isAscending = presenter.isSortingOrderAscending(sortingColumnParameter, sortingParameterValue);
               
               if (isAscending == null) {
                  var headerClassOption = "";
                  var newSortingParameterValue = presenter.DEFAULT_ORDER_PREFIX + sortingColumnParameter;
                  
               } else if (isAscending) {
                  var headerClassOption = "class='ascending'";
                  var newSortingParameterValue = presenter.DESCENDING_ORDER_PREFIX + sortingColumnParameter;  // for example "-user_email"
            
               } else {
                  var headerClassOption = "class='descending'";
                  var newSortingParameterValue = undefined;
               }
            
               modelParams.sort = newSortingParameterValue;
            
               var headerHref = presenter.USERS_OVERVIEW_PAGE_LINK + "?" + analytics.util.constructUrlParams(modelParams);
               table.columns[i] = "<a href='" + headerHref + "' " + headerClassOption + ">" + columnName + "</a>";
            }
            
            // print table
            view.printTable(table, false);
            view.loadTableHandlers();
            
            // print bottom page navigation
            if (pageCount > 1) {
               // remove page parameter
               delete modelParams.page;
               
               // restore initial sort parameter value from URL
               if (sortingParameterValue != null) {
                   modelParams.sort = sortingParameterValue;
               } else {
                   delete modelParams.sort;
               }
            
               var queryString = presenter.USERS_OVERVIEW_PAGE_LINK + "?" + analytics.util.constructUrlParams(modelParams);
            
               view.printBottomPageNavigator(pageCount, currentPageNumber, queryString, presenter.CURRENT_PAGE_QUERY_PARAMETER);
               view.loadPageNavigationHandlers("analytics.main.reloadWidgetOnPageNavigation");
            }          
        });
        
        model.setParams(modelParams);
        
        var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
        model.getAllResults(modelViewName);        
    });        
    
    var modelMetricName = analytics.configuration.getProperty(presenter.widgetName, "modelMetricName");
    model.getMetricValue(modelMetricName);
}
    
/**
 * Return:
 * true, if sortingColumn = sortingParameterValue and sortingParameterValuePrefix = "+", for example: return true if sortingColumn="user_email" and sortingParameterValue = "+user_email"
 * false, if sortingColumn = sortingParameterValue and sortingParameterValuePrefix = "-", for example: return true if sortingColumn="user_email" and sortingParameterValue = "-user_email"
 * null, if sortingParameterValue = null, of sortingColumn != sortingParameterValue
 *
 */
analytics.presenter.UsersProfilesPresenter.prototype.isSortingOrderAscending = function(sortingColumn, sortingParameterValue) {
   var presenter = this; 
    
   if (sortingParameterValue == null) {
      return null;
   }

   if (sortingParameterValue.substring(1) == sortingColumn) {
      var sortingOrder = sortingParameterValue.charAt(0);
      if (sortingOrder == presenter.ASCENDING_ORDER_PREFIX) {
         return true;
      } else if (sortingOrder == presenter.DESCENDING_ORDER_PREFIX){
         return false;
      }
   }

   return null;
}