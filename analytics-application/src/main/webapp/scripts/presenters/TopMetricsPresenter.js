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
    
    // define modelName
    var viewParams = view.getParams();

    var uiToDatabaseMap = {}

    uiToDatabaseMap.metricPrefix = {
        "TOP FACTORY SESSIONS" : "top_factory_sessions",
        "TOP FACTORIES" : "top_factories",
        "TOP REFERRERS" : "top_referrers"
    };
    var DEFAULT_METRIC_VALUE = "TOP FACTORY SESSIONS";
    var metric = viewParams["metric"];
    if (metric == null) {
        metric = DEFAULT_METRIC_VALUE;
    } else {
        metric = metric.toUpperCase();
    }

    uiToDatabaseMap.timeunitSuffix = {
        "1 DAY" : "1day",
        "7 DAYS" : "7day",
        "30 DAYS" : "30day",
        "60 DAYS" : "60day",
        "90 DAYS" : "90day",
        "1 YEAR" : "365day",
        "LIFETIME" : "lifetime"
    };
    var DEFAULT_TIME_UNIT_VALUE = "1 DAY";
    var timeGroup = viewParams["timeGroup"];
    if (timeGroup == null) {
        timeGroup = DEFAULT_TIME_UNIT_VALUE;
    } else {
        timeGroup = timeGroup.toUpperCase();
    }

    var databaseTableMetricPrefix = uiToDatabaseMap.metricPrefix[metric]
            || uiToDatabaseMap.metricPrefix[DEFAULT_METRIC_VALUE];
    
    var databaseTableTimeunitSuffix = uiToDatabaseMap.timeunitSuffix[timeGroup]
            || uiToDatabaseMap.timeunitSuffix[DEFAULT_TIME_UNIT_VALUE];
    
    var modelName = databaseTableMetricPrefix + "_by_" + databaseTableTimeunitSuffix;

    model.pushDoneFunction(function(data) {
        for (var table in data) {
            view.printTable(data[table], false);
        }

        view.loadTableHandlers(false);
    });

    model.getAllResults(modelName);
};