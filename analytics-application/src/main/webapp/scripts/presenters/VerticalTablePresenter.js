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

analytics.presenter.VerticalTablePresenter = function VerticalTablePresenter() {};

analytics.presenter.VerticalTablePresenter.prototype = new Presenter();

analytics.presenter.VerticalTablePresenter.prototype.load = function() {
    var presenter = this; 
    var view = presenter.view;
    var model = presenter.model;
    
    // default label is "Overview"
    var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel", "Overview");
    
    model.setParams(presenter.getModelParams(view.getParams()));
    
    model.pushDoneFunction(function(data) {
        var doNotDisplayCSVButton = analytics.configuration.getProperty(presenter.widgetName, "doNotDisplayCSVButton", false);
        var csvButtonLink = (doNotDisplayCSVButton) 
                            ? undefined
                            : presenter.getLinkForExportToCsvButton();  
                            
        var table = data[0];  // there is only one table in data
        
        // make table columns linked 
        var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList");
        if (typeof columnLinkPrefixList != "undefined") {
            for (var columnName in columnLinkPrefixList) {
                table = view.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
            }          
        }        
               
        view.print("<div class='view'>");
        view.print("   <div class='overview'>");

        view.printWidgetHeader(widgetLabel, csvButtonLink);
        
        view.print("       <div class='body'>");
        view.print("           <div class='item'>");
        
        view.printTableVerticalRow(data[0]);
        
        view.print("           </div>");
        view.print("       </div>");
        view.print("    </div>");
        
        view.loadTableHandlers(false);  // don't display sorting
        
        view.print("</div>");
        
        // finish loading widget
        analytics.views.loader.needLoader = false;
    });
        
    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getAllResults(modelViewName);
};
