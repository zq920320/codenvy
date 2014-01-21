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

    var viewParams = view.getParams();

    viewParams = analytics.configuration.setupDefaultViewParams(presenter.widgetName, viewParams);
    
    var uiToDatabaseMap = {}
    uiToDatabaseMap.metricPrefix = {
        "TOP FACTORY SESSIONS" : "top_factory_sessions",
        "TOP FACTORIES" : "top_factories",
        "TOP REFERRERS" : "top_referrers",
        "TOP USERS" : "top_users",
        "TOP DOMAINS" : "top_domains",
        "TOP COMPANIES" : "top_companies",
    };
    uiToDatabaseMap.timeunitSuffix = {
        "1 DAY" : "1day",
        "7 DAYS" : "7day",
        "30 DAYS" : "30day",
        "60 DAYS" : "60day",
        "90 DAYS" : "90day",
        "1 YEAR" : "365day",
        "LIFETIME" : "lifetime"
    };

    var databaseTableMetricPrefix = uiToDatabaseMap.metricPrefix[viewParams.metric.toUpperCase()];
    
    var databaseTableTimeunitSuffix = uiToDatabaseMap.timeunitSuffix[viewParams.timeGroup.toUpperCase()];
    
    var modelViewName = databaseTableMetricPrefix + "_by_" + databaseTableTimeunitSuffix;

    model.pushDoneFunction(function(data) {
        for (var table in data) {
            view.printTable(data[table], false);
        }

        view.loadTableHandlers();
    });

    model.getAllResults(modelViewName);
};