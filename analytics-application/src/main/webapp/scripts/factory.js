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
analytics.factory = new Factory();

function Factory() {   
    var widgetComponents = {};
		
    function getModel(widgetName) {
        if (typeof widgetComponents[widgetName] == "undefined") {
            widgetComponents[widgetName] = {}; 
        }
        
        if (typeof widgetComponents[widgetName].model == "undefined") {
            widgetComponents[widgetName].model = new Model();
        }        
        
        return widgetComponents[widgetName].model;
    }

    function getView(widgetName, params) {
        if (typeof widgetComponents[widgetName] == "undefined") {
            widgetComponents[widgetName] = {};
        }
        
        if (typeof widgetComponents[widgetName].view == "undefined") {
            widgetComponents[widgetName].view = new View();
            var widgetElement = jQuery("#" + widgetName);
            widgetComponents[widgetName].view.setWidget(widgetElement);
        }
        
        widgetComponents[widgetName].view.setParams(params);
        
        return widgetComponents[widgetName].view;
    }

    function getPresenter(widgetName, view, model) {
        if (typeof widgetComponents[widgetName] == "undefined") {
            widgetComponents[widgetName] = {};
        }
        
        if (typeof widgetComponents[widgetName].presenter == "undefined") {
            var presenterType = analytics.configuration.getProperty(widgetName, "presenterType");
            widgetComponents[widgetName].presenter = new analytics.presenter[presenterType]();
        }
        
        widgetComponents[widgetName].presenter.setView(view);
        widgetComponents[widgetName].presenter.setModel(model);
        widgetComponents[widgetName].presenter.setWidgetName(widgetName);
        
        return widgetComponents[widgetName].presenter;
    }
    
    /** ****************** API ********** */
    return {
        getModel : getModel,
        getView: getView,
        getPresenter: getPresenter
    }
}