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
 * Return modelParams based on params from view which are registered in analytics.configuration object and updated with default values
 */
Presenter.prototype.getModelParams = function(viewParams) {
    var modelParams = {};

    for (var i in viewParams) {
        if (analytics.configuration.isParamRegistered(i)) {
            modelParams[i] = viewParams[i];
        }
    }

    analytics.configuration.setupDefaultModelParams(this.widgetName, modelParams);
    
    analytics.configuration.removeForbiddenModelParams(this.widgetName, modelParams);
    
    // fix date range value format: fix "yyyy-mm-dd" on "yyyymmdd"
    if (typeof modelParams["from_date"] != "undefined") {
        modelParams["from_date"] = modelParams["from_date"].replace(/-/g, "");
    }
    if (typeof modelParams["to_date"] != "undefined") {
        modelParams["to_date"] = modelParams["to_date"].replace(/-/g, "");
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