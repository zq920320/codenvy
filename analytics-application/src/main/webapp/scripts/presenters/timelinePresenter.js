if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenters = analytics.presenters || {};
analytics.presenters.timeline = new Presenter();

analytics.presenters.timeline.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    var DEFAULT_TIME_UNIT_VALUE = "day";
	
	var viewParams = view.getParams();
	
	var timeGroup = viewParams["timeGroup"];
	var user = viewParams["Email"];
	var domain = viewParams["Domain"];
	var company = viewParams["Company"];
	
	if (timeGroup == null) {
	   timeGroup = DEFAULT_TIME_UNIT_VALUE;
	}
	
	var modelParams = {"time_unit": timeGroup.toUpperCase()};
	
	// filter-by parameters
	if (user != null) {
	    modelParams["user"] = user;

	} else if (domain != null) {
	    modelParams["domain"] = domain;

	} else if (company != null) {
	    modelParams["user_company"] = company;
	}

    model.setParams(modelParams);
        
    model.pushDoneFunction(function(data) {
        for (var table in data) {
            view.printTable(data[table], true);
        }
        
        view.loadTableHandlers(true);           
    });

	model.getAllResults("timeline");
};