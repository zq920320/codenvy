if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenters = analytics.presenters || {};
analytics.presenters.topMetrics = new Presenter();

analytics.presenters.topMetrics.load = function() {
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