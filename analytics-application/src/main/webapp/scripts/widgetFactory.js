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
analytics.widgetFactory = new WidgetFactory();

function WidgetFactory() {
	var widgetProperties = {
        factoryStatistics: {
            presenterType: "factoryStatistics",
            isNeedToSaveInHistory: true
        },
        
        timeline: {
            presenterType: "timeline",
            isNeedToSaveInHistory: true
        },
        
        topMetrics: {
            presenterType: "topMetrics",
            isNeedToSaveInHistory: true
        },
        
        usersProfiles: {
            presenterType: "usersProfiles",
            isNeedToSaveInHistory: true
        },
        
//        userOverview: {
//            presenterType: "userOverview",
//            isNeedToSaveInHistory: false
//        },
        
        userStatistics: {
            presenterType: "userStatistics",
            isNeedToSaveInHistory: true
        }
	}
    
    var widgetComponents = {};
		
    var getModel = function(widgetName) {
        if (typeof widgetComponents[widgetName] == "undefined") {
            widgetComponents[widgetName] = {};
        }
        
        if (typeof widgetComponents[widgetName].model == "undefined") {
            widgetComponents[widgetName].model = new Model();
        }        
        
        return widgetComponents[widgetName].model;
    };

    var getView = function(widgetName, params) {
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
    };

    var getPresenter = function(widgetName, view, model) {
        if (typeof widgetComponents[widgetName] == "undefined") {
            widgetComponents[widgetName] = {};
        }
        
        if (typeof widgetComponents[widgetName].presenter == "undefined") {
            var presenterType = widgetProperties[widgetName].presenterType;
            widgetComponents[widgetName].presenter = analytics.presenters[presenterType];
        }
        
        widgetComponents[widgetName].presenter.setView(view);
        widgetComponents[widgetName].presenter.setModel(model);
        return widgetComponents[widgetName].presenter;
    };
    
    var isNeedToSaveInHistory = function(widgetName) {
        return widgetProperties[widgetName].isNeedToSaveInHistory;
    };
    
    var getWidgetNames = function() {
        var widgetNames = new Array();
        for (var widgetName in widgetProperties) {
            widgetNames[widgetNames.length] = widgetName;
        }
        
        return widgetNames;
    };
    
    /** ****************** API ********** */
    return {
        getModel : getModel,
        getView: getView,
        getPresenter: getPresenter,
        isNeedToSaveInHistory: isNeedToSaveInHistory,
        getWidgetNames: getWidgetNames
    }
}