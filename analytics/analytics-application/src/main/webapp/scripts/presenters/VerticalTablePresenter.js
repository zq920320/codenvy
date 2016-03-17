/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
    
    var modelParams = presenter.getModelParams(view.getParams());
    model.setParams(modelParams);
    
    presenter.displayEmptyWidget("Overview");
    
    model.pushDoneFunction(function(data) {                            
        var table = data[0];  // there is only one table in data

        // replace empty column on url parameter
        var urlParameterColumn = analytics.configuration.getProperty(presenter.widgetName, "urlParameterColumn");
        if (typeof urlParameterColumn != "undefined") {
            table = presenter.replaceEmptyColumnOnUrlParameter(table, urlParameterColumn, modelParams);
        }
        
        // add links to drill down page
        table = presenter.linkTableValuesWithDrillDownPage(presenter.widgetName, table, modelParams);
        
        // make table columns linked 
        var columnLinkPrefixList = analytics.configuration.getProperty(presenter.widgetName, "columnLinkPrefixList", {});
        for (var columnName in columnLinkPrefixList) {
            table = presenter.makeTableColumnLinked(table, columnName, columnLinkPrefixList[columnName]);    
        }
        
        view.print("<div class='body'>");
        view.print("    <div class='item'>");
        
        view.printTableVerticalRow(table);
        
        view.print("    </div>");
        view.print("</div>");
        
        view.loadTableHandlers(false);  // don't display sorting
        
        // finish loading widget
        presenter.needLoader = false;
    });
        
    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getModelViewData(modelViewName);
};

/**
 * Replace empty value of column with name 'urlParameterColumn' in first row of table on value
 * of parameter with name 'urlParameterColumn' from modelParams.
 * 
 * If there is empty table, insert new row with value of column with name 'urlParameterColumn' = value of parameter 
 * with name 'urlParameterColumn' from modelParams.
 */
analytics.presenter.VerticalTablePresenter.prototype.replaceEmptyColumnOnUrlParameter = function(table, urlParameterColumn, modelParams) {
    var urlParameterName = urlParameterColumn.toLowerCase();
    var urlParameterColumnIndex = analytics.util.getArrayValueIndex(table.columns, urlParameterColumn);
    
    if (typeof modelParams[urlParameterName] != "undefined"
        && urlParameterColumnIndex != null) {
        
        if (table.rows.length == 0) {
            var row = new Array(table.columns.length);
            row[urlParameterColumnIndex] = modelParams[urlParameterName];
            table.rows.push(row);
            
        } else if (table.rows[0][urlParameterColumnIndex] == "") {
            table.rows[0][urlParameterColumnIndex] = modelParams[urlParameterName];
        }
    }
    
    return table;
}