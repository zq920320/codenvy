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
var analytics = analytics || {};
analytics.factory = new AnalyticsFactory();

function AnalyticsFactory() {
    var widgetComponents = {};
    
    function getModel() {
        return new AnalyticsModel();
    }

    function getView(widgetName, params) {
        var view = new AnalyticsView();
        
        var widgetElement = jQuery("#" + widgetName);
        view.setWidget(widgetElement);
        
        view.setParams(params);
        
        return view;
    }

    function getPresenter(widgetName) {
        if (typeof widgetComponents[widgetName] == "undefined") {
            widgetComponents[widgetName] = {};
        }

        if (typeof widgetComponents[widgetName].presenter == "undefined") {
            createPresenter(widgetName);
        }

        return widgetComponents[widgetName].presenter;
    }

    function createPresenter(widgetName, params) {
        var model = getModel();
        var view = getView(widgetName, params);
        
        if (typeof widgetComponents[widgetName] == "undefined") {
            widgetComponents[widgetName] = {};
        }

        var presenterType = analytics.configuration.getProperty(widgetName, "presenterType");
        
        var presenter = new analytics.presenter[presenterType]();        
        presenter.setView(view);
        presenter.setModel(model);
        presenter.setWidgetName(widgetName);
        
        widgetComponents[widgetName].presenter = presenter;
    }
    
    /** ****************** API ********** */
    return {
        getPresenter: getPresenter,
        createPresenter: createPresenter,
    }
}
