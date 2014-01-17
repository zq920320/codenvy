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