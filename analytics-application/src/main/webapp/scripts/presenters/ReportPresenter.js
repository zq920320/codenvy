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
	
    model.setParams(presenter.getModelParams(view.getParams()));
        
    model.pushDoneFunction(function(data) {
        var csvButtonLink = presenter.getLinkForExportToCsvButton();
        view.printCsvButton(csvButtonLink);
        
        for (var table in data) {
            view.printTable(data[table], true);
        }
        
        view.loadTableHandlers();   
        
        // finish loading widget
        analytics.views.loader.needLoader = false;
    });

    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    
	model.getAllResults(modelViewName);
};