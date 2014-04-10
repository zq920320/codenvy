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

analytics.presenter.ReportPresenter = function ReportPresenter() {};

analytics.presenter.ReportPresenter.prototype = new Presenter();

analytics.presenter.ReportPresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    // get list of expandable metrics of report
    model.pushDoneFunction(function(data) {
        var viewParams = view.getParams();
        var modelParams = presenter.getModelParams(viewParams);
        model.setParams(modelParams);

        var mapExpandableMetricToLabel = data;
        
        // get report data
        model.popDoneFunction();
        model.pushDoneFunction(function(data) {
            var doNotDisplayCSVButton = analytics.configuration.getProperty(presenter.widgetName, "doNotDisplayCSVButton", false);  // default value is "false" 
            var csvButtonLink = (doNotDisplayCSVButton) 
                                ? undefined
                                : presenter.getLinkForExportToCsvButton();     
            var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
            view.printWidgetHeader(widgetLabel, csvButtonLink);
    
            view.print("<div class='body'>");
                
            for (var i in data) {
                var table = data[i];
                
                // add links to drill down page
                table = presenter.linkMetricValueWithDrillDownPage(table, mapExpandableMetricToLabel, modelParams);            
                
                view.printTable(table, true);
            }
            
            var clientSortParams = analytics.configuration.getProperty(presenter.widgetName, "clientSortParams");
            view.loadTableHandlers(true, clientSortParams);   
    
            view.print("</div>");
            
            // finish loading widget
            analytics.views.loader.needLoader = false;
        });
    
        var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
        
    	model.getModelViewData(modelViewName);
    });

    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getExpandableMetricList(modelViewName);
};

analytics.presenter.ReportPresenter.prototype.linkMetricValueWithDrillDownPage = function(table, mapExpandableMetricToLabel, modelParams) { 
    for (var rowNumber = 0; rowNumber < table.rows.length; rowNumber++) {
        var metricLabel = table.rows[rowNumber][0];
        
        // check if there is row in mapExpandableMetricToLabel
        var metricName = analytics.util.getKeyByValue(mapExpandableMetricToLabel, metricLabel);
        if (metricName != null) {            
            for (var columnNumber = 1; columnNumber < table.rows[rowNumber].length; columnNumber++) {
                var columnValue = table.rows[rowNumber][columnNumber];
                
                // don't display link to empty drill down page
                if (! this.isEmptyValue(columnValue)) {
                    var timeInterval = columnNumber - 1;
                    var drillDownPageLink = this.getDrillDownPageLink(timeInterval, metricName, columnValue, modelParams);                
                    
                    table.rows[rowNumber][columnNumber] = "<a href='" + drillDownPageLink + "'>" + columnValue + "</a>";
                }
            }
        }        
    }
    
    return table;
}

analytics.presenter.ReportPresenter.prototype.getDrillDownPageLink = function(timeInterval, metricName, metricValue, modelParams) {
    var drillDownPageLink = this.DRILL_DOWN_PAGE_ADDRESS + "?"+ analytics.util.constructUrlParams(modelParams);
    drillDownPageLink += "&" + this.TIME_INTERVAL_PARAMETER + "=" + timeInterval;
    drillDownPageLink += "&" + this.METRIC_ORIGINAL_VALUE_VIEW_PARAMETER + "=" + metricValue;
    drillDownPageLink += "&" + this.METRIC_ORIGINAL_NAME_VIEW_PARAMETER + "=" + metricName;
    
    return drillDownPageLink;
}

/** 
 * @returns true if value = "0".
 * */
analytics.presenter.ReportPresenter.prototype.isEmptyValue = function(value) {
    return value == "0"  // 0 numeric value
           || value == "00:00:00";   // 0 time value
}