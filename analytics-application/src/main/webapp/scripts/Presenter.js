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
Presenter.prototype.METRIC_ORIGINAL_NAME_VIEW_PARAMETER = "expanded_metric_name";
Presenter.prototype.TIME_INTERVAL_PARAMETER = "time_interval";

/** Event filtering parameters */
Presenter.prototype.EVENT_PARAMETER_NAME_FIELD = "event_parameter_name";
Presenter.prototype.EVENT_PARAMETER_VALUE_FIELD = "event_parameter_value";
Presenter.prototype.EVENT_PARAMETER_FIELD = "parameters";

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
    
    // construct event parameter
    if (typeof viewParams[this.EVENT_PARAMETER_NAME_FIELD] != "undefined"
        && typeof viewParams[this.EVENT_PARAMETER_VALUE_FIELD] != "undefined") {
        modelParams[this.EVENT_PARAMETER_FIELD] = 
            viewParams[this.EVENT_PARAMETER_NAME_FIELD] + "=" + viewParams[this.EVENT_PARAMETER_VALUE_FIELD];
    }
    
    for (var i in viewParamNames) {
        var viewParamName = viewParamNames[i]
        if (analytics.configuration.isModelParamRegistered(viewParamName)) {
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
Presenter.prototype.addServerSortingLinks = function(table, widgetName, modelParams, mapColumnToServerSortParam, doNotMap) {
    if (typeof doNotMap == "undefined") {
        doNotMap = false;
    }

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
    
        var headerHref = analytics.util.getCurrentPageName();
        if (!jQuery.isEmptyObject(modelParams)) {
            headerHref += "?" + analytics.util.constructUrlParams(modelParams);
        }
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

/** 
 * @returns true if value = "0".
 * */
Presenter.prototype.isEmptyValue = function(value) {
    return value == "0"  // 0 numeric value
           || value == "00:00:00"   // 0 time value
           || value == "0%";   // 0 time value
}

Presenter.prototype.getDrillDownPageLink = function(metricName, modelParams, timeInterval) {
    var drillDownPageAddress = analytics.configuration.getDrillDownPageAddress(metricName);
    var drillDownPageLinkDelimeter = (drillDownPageAddress.indexOf("?") != -1) ? "&" : "?";
    var drillDownPageLink = analytics.configuration.getDrillDownPageAddress(metricName) 
                            + drillDownPageLinkDelimeter + this.METRIC_ORIGINAL_NAME_VIEW_PARAMETER + "=" + metricName;
    
    if (!jQuery.isEmptyObject(modelParams)) {
        var modelParams = analytics.util.clone(modelParams);
        delete modelParams[this.METRIC_ORIGINAL_NAME_VIEW_PARAMETER];  // remove redundant expanded metric name
        drillDownPageLink += "&" + analytics.util.constructUrlParams(modelParams);
    }

    if (typeof timeInterval != "undefined") {
        drillDownPageLink += "&" + this.TIME_INTERVAL_PARAMETER + "=" + timeInterval;
    }
    
    return drillDownPageLink;
}

Presenter.prototype.linkTableValuesWithDrillDownPage = function(widgetName, table, modelParams) {
    var modelParams = analytics.util.clone(modelParams);
    
    delete modelParams.page;    // remove page parameter
    delete modelParams.per_page;    // remove page parameter
    delete modelParams.sort;    // remove sort parameter
    
    var mapColumnToParameter = analytics.configuration.getSubProperty(widgetName, 
                                                                      "columnDrillDownPageLinkConfiguration", 
                                                                      "mapColumnToParameter", 
                                                                      {});
    
    var doNotLinkOnEmptyParameter = analytics.configuration.getSubProperty(widgetName, 
                                                                           "columnDrillDownPageLinkConfiguration", 
                                                                           "doNotLinkOnEmptyParameter", 
                                                                           true);
    
    // calculate source column indexes for combine links
    var sourceColumnIndexes = [];
    for (var sourceColumnName in mapColumnToParameter) {
        var sourceColumnIndex = analytics.util.getColumnIndexByColumnName(table.columns, sourceColumnName);
        sourceColumnIndexes.push(sourceColumnIndex);
    }
       
    for (var columnIndex = 0; columnIndex < table.columns.length; columnIndex++) {
        var columnName = table.columns[columnIndex];            
        
        var expandedMetricName = analytics.configuration.getExpandableMetricName(widgetName, columnName);
        if (typeof expandedMetricName != "undefined") {
            for (var i = 0; i < table.rows.length; i++) {
                var columnValue = table.rows[i][columnIndex];
                
                if (! this.isEmptyValue(columnValue)) {
                    var drillDownPageLink = this.getDrillDownPageLink(expandedMetricName, modelParams);                
                    
                    // calculation combined link like "ws=...&project=..."
                    if (sourceColumnIndexes.length > 0) {
                        drillDownPageLink += "&" + this.getUrlParamsForCombineColumnLink(table.rows[i], 
                                                                                         sourceColumnIndexes, 
                                                                                         mapColumnToParameter, 
                                                                                         doNotLinkOnEmptyParameter);
                    }
                    
                    table.rows[i][columnIndex] = "<a href='" + drillDownPageLink + "'>" + columnValue + "</a>";
                }
            }
        }
    }          
        
    return table;
}

/**
 * Make table cells of column with certain name as linked with link = "columnLinkPrefix + {columnValue}"
 */
Presenter.prototype.makeTableColumnLinked = function(table, columnName, columnLinkPrefix) {   
    var columnIndex = analytics.util.getArrayValueIndex(table.columns, columnName);
    if (columnIndex != null) {
        for (var i = 0; i < table.rows.length; i++) {
            var columnValue = table.rows[i][columnIndex];
            
            if (analytics.configuration.isSystemMessage(columnValue)) {
               table.rows[i][columnIndex] = this.view.getSystemMessageLabel(columnValue);    
            } else {
               var href = columnLinkPrefix + "=" + encodeURIComponent(columnValue);
               
               if (analytics.configuration.isFactoryUrlColumnName(columnName)) {
                   var title = columnValue;   // display initial url in title of link
                   columnValue = analytics.util.getShortenFactoryUrl(columnValue);                       
                   table.rows[i][columnIndex] = "<a href='" + href + "' title='" + title + "'>" + columnValue + "</a>";
               } else {
                   table.rows[i][columnIndex] = "<a href='" + href + "'>" + columnValue + "</a>";
               }
            }
        }
    }

    return table;
}

/**
 * Make table cells of target column as linked with combined link "project-view.jsp?ws=...&project=.."
 * @param columnCombinedLinkConf = {
 *     targetColumn1: {
 *         baseLink: <baseLink>,
 *         mapColumnToParameter: {
 *             columnName1: <parameterName1>,
 *             columnName2: <parameterName2>,
 *             ...
 *         }
 *     },
 *     
 *     targetColumn2: { ... },
 *     
 *     ...
 * }
 */
Presenter.prototype.makeTableColumnCombinedLinked = function(table, columnCombinedLinkConf) {
    var doNotLinkOnEmptyParameter = columnCombinedLinkConf["doNotLinkOnEmptyParameter"];
    if (typeof doNotLinkOnEmptyParameter == "undefined") {
        doNotLinkOnEmptyParameter = true;
    }

    for (var targetColumnName in columnCombinedLinkConf) {
        var targetColumnIndex = analytics.util.getColumnIndexByColumnName(table.columns, targetColumnName);
        
        var baseLink = columnCombinedLinkConf[targetColumnName].baseLink;
        var mapColumnToParameter = columnCombinedLinkConf[targetColumnName].mapColumnToParameter;

        // calculate source column indexes
        var sourceColumnIndexes = [];
        for (var sourceColumnName in mapColumnToParameter) {
            var sourceColumnIndex = analytics.util.getColumnIndexByColumnName(table.columns, sourceColumnName);
            sourceColumnIndexes.push(sourceColumnIndex);
        }
        
        // make cells of target column as linked with combined link
        for (var i = 0; i < table.rows.length; i++) {
            var targetColumnValue = table.rows[i][targetColumnIndex];
            
            if (analytics.configuration.isSystemMessage(targetColumnValue)) {
               table.rows[i][targetColumnIndex] = this.view.getSystemMessageLabel(targetColumnValue);
               
            } else {
               // calculation combined link like "project-view.jsp?ws=...&project=..."
               var urlParams = this.getUrlParamsForCombineColumnLink(table.rows[i], sourceColumnIndexes, mapColumnToParameter, doNotLinkOnEmptyParameter);
               if (urlParams != "") {
                   var href = baseLink + "?" + this.getUrlParamsForCombineColumnLink(table.rows[i], sourceColumnIndexes, mapColumnToParameter, doNotLinkOnEmptyParameter);
                   table.rows[i][targetColumnIndex] = "<a href='" + href + "'>" + targetColumnValue + "</a>";
               }
            }
        }            
    }

    return table;
}

/**
 * @param sourceColumnIndexes = indexes in row of source columns defined in mapColumnToParameter; 
 * there is null value for absent column in row.
 *  
 * @returns query parameters like "ws=<WS_source_column_value>&project=<PROJECT_source_column_value>"
 */
Presenter.prototype.getUrlParamsForCombineColumnLink = function(row, sourceColumnIndexes, mapColumnToParameter, doNotLinkOnEmptyParameter) {
    var params = {};
    var sourceColumnNames = Object.keys(mapColumnToParameter);   

    for (var j = 0; j < sourceColumnIndexes.length; j++) {
        var sourceColumnIndex = sourceColumnIndexes[j];
        if (sourceColumnIndex != null) {   // there is null value for absent source column in row        
            var sourceColumnName = sourceColumnNames[j];

            var parameterName = mapColumnToParameter[sourceColumnName];
            var parameterValue = row[sourceColumnIndex];
            
            if (parameterValue == "") {
                if (doNotLinkOnEmptyParameter) {
                    return "";
                } else {
                    continue;
                }
            }
            
            params[parameterName] = parameterValue;
        }
    }
    
    return analytics.util.constructUrlParams(params) || "";
}
