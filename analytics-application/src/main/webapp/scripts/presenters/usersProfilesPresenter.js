if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenters = analytics.presenters || {};
analytics.presenters.usersProfiles = new Presenter();

analytics.presenters.usersProfiles.CURRENT_PAGE_QUERY_PARAMETER = "page";
analytics.presenters.usersProfiles.ONE_PAGE_ROWS_COUNT = 20;
analytics.presenters.usersProfiles.USER_ID_LINK_PREFIX = "user-view.jsp?user";
analytics.presenters.usersProfiles.USERS_OVERVIEW_PAGE_LINK = "users-profiles.jsp";

analytics.presenters.usersProfiles.SORTING_PARAMETER = "sort";
analytics.presenters.usersProfiles.ASCENDING_ORDER_PREFIX = "+";
analytics.presenters.usersProfiles.DESCENDING_ORDER_PREFIX = "-";
analytics.presenters.usersProfiles.DEFAULT_ORDER_PREFIX = analytics.presenters.usersProfiles.ASCENDING_ORDER_PREFIX;

analytics.presenters.usersProfiles.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    var viewParams = view.getParams();
    
    /** see server-side mapping at the analytics-core/src/main/resources/views.xml in view element with name "users-profiles" */
    presenter.databaseToUIMap = {
        databaseTableColumns: ["_id",   "user_first_name", "user_last_name", "user_company",   "user_job"],
        uiTableColumns:       ["Email", "First Name",      "Last Name",      "Company",        "Job"],
        jagQueryParameters:   ["Email", "First Name",      "Last Name",    "Company"]
    }
    
    // process filter-by parameters
    var modelParams = presenter.mapQueryParametersFromUIToDatabase(viewParams);
    
    // get page count
    model.setParams(modelParams);
    
    model.pushDoneFunction(function(data) {
        var table;
        var pageCount = Math.ceil(data / presenter.ONE_PAGE_ROWS_COUNT) ;
    
        // process pagination
        var currentPageNumber = viewParams[presenter.CURRENT_PAGE_QUERY_PARAMETER];
        if (currentPageNumber == null) {
           currentPageNumber = 1;
        } else {
           currentPageNumber = new Number(currentPageNumber);
        }
        
        modelParams.per_page = presenter.ONE_PAGE_ROWS_COUNT;
        modelParams.page = currentPageNumber;
        
        //process sorting
        var sortingParameterValue = viewParams["sort"];
        if (sortingParameterValue != null) {
            modelParams["sort"] = sortingParameterValue;
        }

        model.popDoneFunction();
        model.pushDoneFunction(function(data) {
            var table;
            for (var i in data) {
                table = data[i];
            }
            
            // make user id in first column as linked 
            for (var i = 0; i < table.rows.length; i++) {
               var userId = table.rows[i][0];
               var href = presenter.USER_ID_LINK_PREFIX + "=" + userId;
               table.rows[i][0] = "<a href='" + href + "'>" + userId + "</a>";
            }
            
            // make table header as linked for sorting
            for (var i = 0; i < table.columns.length; i++) {
               var columnName = table.columns[i];
               var sortingColumnParameter = presenter.mapTableFromUIToDatabase(columnName);
            
               var isAscending = presenter.isSortingOrderAscending(sortingColumnParameter, sortingParameterValue);
            
               if (isAscending == null) {
                  var headerClassOption = "'" + sortingParameterValue + "'";
                  var newSortingParameterValue = presenter.DEFAULT_ORDER_PREFIX + sortingColumnParameter;
            
               } else if (isAscending) {
                  var headerClassOption = "class='ascending'";
                  var newSortingParameterValue = presenter.DESCENDING_ORDER_PREFIX + sortingColumnParameter;  // for example "-user_email"
            
               } else {
                  var headerClassOption = "class='descending'";
                  var newSortingParameterValue = undefined;
               }
            
               modelParams.sort = newSortingParameterValue;
            
               modelParams = presenter.mapQueryParametersFromDatabaseToUI(modelParams);
            
               var headerHref = presenter.USERS_OVERVIEW_PAGE_LINK + "?" + analytics.util.constructUrlParams(modelParams);
               table.columns[i] = "<a href='" + headerHref + "' " + headerClassOption + ">" + columnName + "</a>";
            }
            
            // print table
            view.printTable(table, false);
            view.loadTableHandlers(false);
            
            // print bottom page navigation
            if (pageCount > 1) {
               // remove page parameter
               delete modelParams["page"];
               
               // restore initial sort parameter value from URL
               if (sortingParameterValue != null) {
                   modelParams["sort"] = sortingParameterValue;
               } else {
                   delete modelParams["sort"];
               }
            
               var queryString = presenter.USERS_OVERVIEW_PAGE_LINK + "?" + analytics.util.constructUrlParams(modelParams);
            
               view.printBottomPageNavigator(pageCount, currentPageNumber, queryString, presenter.CURRENT_PAGE_QUERY_PARAMETER);
               view.loadPageNavigationHandlers("analytics.main.reloadWidgetOnPageNavigation");
            }          
        });
        
        model.setParams(modelParams);
        model.getAllResults("users-profiles");        
    });        
    
    model.getMetricValue("users_profiles");
}
    
/**
 * Return:
 * true, if sortingColumn = sortingParameterValue and sortingParameterValuePrefix = "+", for example: return true if sortingColumn="user_email" and sortingParameterValue = "+user_email"
 * false, if sortingColumn = sortingParameterValue and sortingParameterValuePrefix = "-", for example: return true if sortingColumn="user_email" and sortingParameterValue = "-user_email"
 * null, if sortingParameterValue = null, of sortingColumn != sortingParameterValue
 *
 */
analytics.presenters.usersProfiles.isSortingOrderAscending = function(sortingColumn, sortingParameterValue) {
   if (sortingParameterValue == null) {
      return null;
   }

   if (sortingParameterValue.substring(1) == sortingColumn) {
      var sortingOrder = sortingParameterValue.charAt(0);
      if (sortingOrder == this.ASCENDING_ORDER_PREFIX) {
         return true;
      } else if (sortingOrder == this.DESCENDING_ORDER_PREFIX){
         return false;
      }
   }

   return null;
}