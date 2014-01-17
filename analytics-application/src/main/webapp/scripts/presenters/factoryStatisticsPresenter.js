if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenters = analytics.presenters || {};
analytics.presenters.factoryStatistics = new Presenter();

analytics.presenters.factoryStatistics.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    var DEFAULT_TIME_UNIT_VALUE = "day";
	
	var viewParams = view.getParams();
	
	var timeGroup = viewParams["timeGroup"];
	var user = viewParams["Email"];
	var orgId = viewParams["Organization"];
	var affiliateId = viewParams["Affiliate"];
	
	if (timeGroup == null) {
	   timeGroup = DEFAULT_TIME_UNIT_VALUE;
	}
	
	var modelParams = {"time_unit": timeGroup.toUpperCase()};
	
	// filter-by parameters
	if (user != null) {
	    modelParams["user"] = user;
	
	} else if (orgId != null) {
	    modelParams["org_id"] = orgId;
	
	} else if (affiliateId != null) {
	    modelParams["affiliate_id"] = affiliateId;
	}

    model.setParams(modelParams);
        
    model.pushDoneFunction(function(data) {
        for (var table in data) {
            view.printTable(data[table], true);
        }
        
        view.loadTableHandlers(true);           
    });

	model.getAllResults("factory-timeline");
};