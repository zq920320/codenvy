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

analytics.presenter.TopMetricsPresenter = function TopMetricsPresenter() {};

analytics.presenter.TopMetricsPresenter.prototype = new Presenter();

analytics.presenter.TopMetricsPresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;

    var modelParams = presenter.getModelParams(view.getParams())

    presenter.modelViewName = presenter.getModelViewName(modelParams);

    // remove unnecessary params 
    delete modelParams.metric;
    delete modelParams.time_unit;
    
    model.setParams(modelParams);
    
    model.pushDoneFunction(function(data) {
        var csvButtonLink = presenter.getLinkForExportToCsvButton(presenter.modelViewName);
        var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
        view.printWidgetHeader(widgetLabel, csvButtonLink);            
        
        view.print("<div class='body'>");
        
        for (var table in data) {
            view.printTable(data[table], false);
        }

        view.loadTableHandlers();

        view.print("</div>");
        
        // finish loading widget
        analytics.views.loader.needLoader = false;
    });

    model.getAllResults(presenter.modelViewName);
};

analytics.presenter.TopMetricsPresenter.prototype.getModelViewName = function(modelParams) {
    var databaseTableMetricPrefix = modelParams.metric.toLowerCase();
    var databaseTableTimeunitSuffix = modelParams.time_unit.toLowerCase();
    return databaseTableMetricPrefix + "_by_" + databaseTableTimeunitSuffix;
}