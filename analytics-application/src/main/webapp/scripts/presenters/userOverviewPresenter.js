if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenters = analytics.presenters || {};
analytics.presenters.userOverview = new Presenter();

analytics.presenters.userOverview.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    view.print("<div class='view'>");
    
    var USER_PROFILE_TABLE_NAME = 0;
    
    model.setParams(view.getParams());
    
    model.pushDoneFunction(function(data) {
        view.print("<div class='view'>");
        
        view.print("<div class='overview'>");
        view.print("<div class='header'>User Overview</div>");
        
        view.print("<div class='body'>");
        
        view.print("<div class='item'>");
        view.printTableVerticalRow(data[USER_PROFILE_TABLE_NAME]);
        view.print("</div>");
        
        view.print("</div>");
        view.print("</div>");
        
        view.loadTableHandlers(false);
        
        view.print("</div>");
        view.print("</div>");
    });
        
    model.getAllResults("user_profile");
};
