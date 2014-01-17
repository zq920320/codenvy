if (typeof analytics === "undefined") {
    analytics = {};
}

function Presenter() {}

Presenter.prototype.view = null;
Presenter.prototype.model = null;
Presenter.prototype.load = null;

Presenter.prototype.setView = function(newView) {
    this.view = newView;
};

Presenter.prototype.setModel = function(newModel) {
    this.model = newModel;
};

Presenter.prototype.databaseToUIMap = null;

/**
 * Return encoded parameter name according to mapping rules defined in databaseToUIMap, for example: "First Name" => "user_first_name"
 */
Presenter.prototype.mapTableFromUIToDatabase = function(uiTableColumnName) {
    var uiTableColumns = this.databaseToUIMap.uiTableColumns;
    for (var i = 0; i < uiTableColumns.length; i++) {
        if (uiTableColumnName == uiTableColumns[i]) {
            return this.databaseToUIMap.databaseTableColumns[i];
        }
    }

    return null;
};

/**
 * Return decoded parameter name according to mapping rules defined in databaseToUIMap, for example: "user_first_name" => "First Name"
 */
Presenter.prototype.mapTableFromDatabaseToUI = function(databaseTableColumnName) {
    var databaseTableColumns = this.databaseToUIMap.databaseTableColumns;
    for (var i = 0; i < databaseTableColumns.length; i++) {
        if (databaseTableColumnName == databaseTableColumns[i]) {
            return this.databaseToUIMap.uiTableColumns[i];
        }
    }

    return null;
};

/**
 * Return query parameters object for rest-service according to mapping jag request query to rest-service request query rules defined in databaseToUIMap
 */
Presenter.prototype.mapQueryParametersFromUIToDatabase = function(viewParams) {
    var restServiceRequestQueryParameters = {};
    var jagRequestParameters = this.databaseToUIMap.jagQueryParameters;
    for (var i = 0; i < jagRequestParameters.length; i++) {
        var jagRequestParameterValue = viewParams[jagRequestParameters[i]];
        if (jagRequestParameterValue != null) {
            restServiceRequestQueryParameters[this.databaseToUIMap.databaseTableColumns[i]] = jagRequestParameterValue;
        }
    }

    return restServiceRequestQueryParameters;
};

/**
 * Return query parameters object for rest-service according to mapping jag request query to rest-service request query rules defined in databaseToUIMap
 */
Presenter.prototype.mapQueryParametersFromDatabaseToUI = function(params) {
    var params = params || {};

    var jagRequestParameters = this.databaseToUIMap.jagQueryParameters;
    for (var i = 0; i < jagRequestParameters.length; i++) {
        var databaseParameterName = this.databaseToUIMap.databaseTableColumns[i];
        var paramValue = params[databaseParameterName];
        if (typeof paramValue != "undefined") {
            delete params[databaseParameterName];
            params[jagRequestParameters[i]] = paramValue;
        }
    }

    return params;
};