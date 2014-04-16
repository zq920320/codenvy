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

function Presenter() {}

Presenter.prototype.view = null;
Presenter.prototype.model = null;
Presenter.prototype.load = null;
Presenter.prototype.widgetName = null;

/** Sorting parameters */
Presenter.prototype.SORTING_PARAMETER = "sort";
Presenter.prototype.ASCENDING_ORDER_PREFIX = "+";
Presenter.prototype.DESCENDING_ORDER_PREFIX = "-";
Presenter.prototype.DEFAULT_ORDER_PREFIX = Presenter.prototype.ASCENDING_ORDER_PREFIX;

/** Pagination parameters */
Presenter.prototype.CURRENT_PAGE_QUERY_PARAMETER = "page";
Presenter.prototype.DEFAULT_ONE_PAGE_ROWS_COUNT = 20;

/** Drill Down page parameters */
Presenter.prototype.METRIC_ORIGINAL_VALUE_VIEW_PARAMETER = "expanded_metric_value";
Presenter.prototype.METRIC_ORIGINAL_NAME_VIEW_PARAMETER = "expanded_metric_name";
Presenter.prototype.TIME_INTERVAL_PARAMETER = "time_interval";

Presenter.prototype.setView = function(newView) {
    this.view = newView;
};

Presenter.prototype.setModel = function(newModel) {
    this.model = newModel;
};

Presenter.prototype.setWidgetName = function(newWidgetName) {
    this.widgetName = newWidgetName;
};

/**
 * Return modelParams based on params from view which are registered in analytics.configuration object and updated with default values.
 * Exclude model parameters with value = ""
 */
Presenter.prototype.getModelParams = function(viewParams) {
    var modelParams = {};

    var viewParamNames = Object.keys(viewParams);
    for (var i in viewParamNames) {
        var viewParamName = viewParamNames[i]
        if (analytics.configuration.isParamRegistered(viewParamName)) {
            var paramValue = viewParams[viewParamName];
            
            // translate date range value format: fix "yyyy-mm-dd" on "yyyymmdd"
            if (analytics.configuration.isDateParam(viewParamName)) {
                paramValue = analytics.util.encodeDate(paramValue);
            }
            
            modelParams[viewParamName] = paramValue;
        }
    }

    analytics.configuration.setupDefaultModelParams(this.widgetName, modelParams);
    
    analytics.configuration.removeForbiddenModelParams(this.widgetName, modelParams);
    
    // remove modelParams with value = ""
    var modelParamNames = Object.keys(modelParams);
    for (var i in modelParamNames) {
        var modelParamName = modelParamNames[i];
        if (modelParams[modelParamName] === "") {
            delete modelParams[modelParamName];
        }
    }
    
    return modelParams;
}

/**
 * Return link to get view data in CSV format
 */
Presenter.prototype.getLinkForExportToCsvButton = function(modelViewName) {    
    var lastModelParams = analytics.util.clone(this.model.getParams());
    
    // get all pages of view
    delete lastModelParams["per_page"];
    delete lastModelParams["page"];
    
    this.model.setParams(lastModelParams);
    
    var modelViewName = modelViewName || analytics.configuration.getProperty(this.widgetName, "modelViewName");
    
    return this.model.getLinkToExportToCsv(modelViewName);
}

/**
 * Make table header as linked for sorting.
 */
Presenter.prototype.addServerSortingLinks = function(table, widgetName, modelParams, doNotMap) {
    if (typeof doNotMap == "undefined") {
        doNotMap = false;
    }
        
    var mapColumnToServerSortParam = analytics.configuration.getProperty(widgetName, "mapColumnToServerSortParam", undefined);
    if (typeof mapColumnToServerSortParam == "undefined" && !doNotMap) {
        return table;
    }
    
    // don't display sorting command if there is no rows or if there is only one row in the table
    if (table.rows.length < 1) {
        return table;
    }
    
    var modelParams = analytics.util.clone(modelParams);
    
    if (typeof modelParams.sort == "undefined" && !doNotMap) { 
        if (typeof  analytics.configuration.getProperty(widgetName, "defaultServerSortParams") != "undefined") {
            modelParams.sort = analytics.configuration.getProperty(widgetName, "defaultServerSortParams");
        }
    }
    
    var sortingParameterValue = modelParams.sort || null;
             
    for (var i = 0; i < table.columns.length; i++) {
        var columnName = table.columns[i];
        
        if (doNotMap) {
            var sortParamColumnName = columnName;            
        } else {
            var sortParamColumnName = mapColumnToServerSortParam[columnName];
        }
        
        if (typeof sortParamColumnName == "undefined") {
            continue;
        }
       
        var isAscending = this.isSortingOrderAscending(sortParamColumnName, sortingParameterValue);
       
        if (isAscending == null) {
           var headerClassOption = "class='unsorted'";
           var newSortingParameterValue = this.DEFAULT_ORDER_PREFIX + sortParamColumnName;
          
        } else if (isAscending) {
           var headerClassOption = "class='ascending'";
           var newSortingParameterValue = this.DESCENDING_ORDER_PREFIX + sortParamColumnName;  // for example "-user_email"
    
        } else {
           var headerClassOption = "class='descending'";
           var newSortingParameterValue = this.ASCENDING_ORDER_PREFIX + sortParamColumnName;  // for example "+user_email"
        }
    
        if (typeof newSortingParameterValue == "undefined") {
            delete modelParams.sort;
        } else {
            modelParams.sort = newSortingParameterValue;
        }
    
        var headerHref = analytics.util.getCurrentPageName() + "?" + analytics.util.constructUrlParams(modelParams);
        var onClickHandler = "analytics.main.reloadWidgetByUrl(\"" + headerHref + "\",\"" + widgetName + "\"); return false;";
        
        table.columns[i] = "<a href='" + headerHref + "' " + headerClassOption + " onclick='" + onClickHandler + "'>" + columnName + "</a>";
    }
    
    return table;
}

/**
 * Return:
 * true, if sortingColumn = sortingParameterValue and sortingParameterValuePrefix = "+", for example: return true if sortingColumn="user_email" and sortingParameterValue = "+user_email"
 * false, if sortingColumn = sortingParameterValue and sortingParameterValuePrefix = "-", for example: return true if sortingColumn="user_email" and sortingParameterValue = "-user_email"
 * null, if sortingParameterValue = null, of sortingColumn != sortingParameterValue
 *
 */
Presenter.prototype.isSortingOrderAscending = function(sortingColumn, sortingParameterValue) {   
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
