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

analytics.presenter.SummaryTablePresenter = function SummaryTablePresenter() {};

analytics.presenter.SummaryTablePresenter.prototype = new Presenter();

analytics.presenter.SummaryTablePresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    // default label is "Summary"
    var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel", "Summary");
    var modelParams = presenter.getModelParams(view.getParams());
    model.setParams(modelParams);
    
    model.pushDoneFunction(function(table) {
        if (typeof modelParams[presenter.EXPANDED_METRIC_NAME_PARAMETER] == "undefined") {  // don't expand summary metric secondary
            // add links to drill down page
            table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams);
        }
        
        view.printWidgetHeader(widgetLabel);

        var tabelId = presenter.widgetName + "_table";
        
        view.print("<div class='body'>");
        view.printTable(table, false, tabelId);
        view.print("</div>");
        
        view.loadTableHandlers(false, {}, tabelId);
        
        // finish loading widget
        analytics.views.loader.needLoader = false;
    });

    var modelMetricName = analytics.configuration.getProperty(presenter.widgetName, "modelMetricName");
    model.getSummarizedMetricValue(modelMetricName);
};
