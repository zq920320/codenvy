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

analytics.presenter.ReportPresenter = function ReportPresenter() {
};

analytics.presenter.ReportPresenter.prototype = new Presenter();

analytics.presenter.ReportPresenter.prototype.load = function () {
    var presenter = this;
    var view = presenter.view;
    var model = presenter.model;

    // get list of expandable metrics of report
    model.pushDoneFunction(function (data) {
        var viewParams = view.getParams();
        var modelParams = presenter.getModelParams(viewParams);
        model.setParams(modelParams);

        var expandableMetricPerSection = data;

        // get report data
        model.popDoneFunction();
        model.pushDoneFunction(function (data) {
            var doNotDisplayCSVButton = analytics.configuration.getProperty(presenter.widgetName, "doNotDisplayCSVButton", false);  // default value is "false" 
            var csvButtonLink = (doNotDisplayCSVButton)
                ? undefined
                : presenter.getLinkForExportToCsvButton();
            var widgetLabel = analytics.configuration.getProperty(presenter.widgetName, "widgetLabel");
            view.printWidgetHeader(widgetLabel, csvButtonLink);

            view.print("<div class='body'>");
            
            var displayLineChart = analytics.configuration.getProperty(presenter.widgetName, "displayLineChart", false);
            
            for (var i in data) {
                var table = analytics.util.clone(data[i], true, []);
                
                // add links to drill down page
                table = presenter.linkTableValuesWithDrillDownPage(table, i, expandableMetricPerSection, modelParams);            
                
                if (displayLineChart) {
                    view.printTableAndChart(table, data[i]);
                } else {
                    view.printTable(table, true);
                }
            }

            var clientSortParams = analytics.configuration.getProperty(presenter.widgetName, "clientSortParams");
            view.loadTableHandlers(true, clientSortParams);

            view.print("</div>");

            // finish loading widget
            analytics.views.loader.needLoader = false;
        });

        var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");

        model.getModelViewData(modelViewName);
    });

    var modelViewName = analytics.configuration.getProperty(presenter.widgetName, "modelViewName");
    model.getExpandableMetricList(modelViewName);
};

/**
 * expandableMetricPerSection format:
 * [
 * {"1": "total_factories",   // first section (first row with "0" key is title as usual in reports, and so is absent)
 *  "2": "created_factories",
 *  ...},
 *
 * {},                        // second section (first row with "0" key is title as usual in reports, and so is absent)
 *
 * {"2": "active_workspaces", // third section (first row with "0" key is title as usual in reports, and so is absent)
 *  "5": "active_users",
 *  ...},
 *
 *  ...
 *  ]
 */
analytics.presenter.ReportPresenter.prototype.linkTableValuesWithDrillDownPage = function (table, tableNumber, expandableMetricPerSection, modelParams) {
    // setup top date of expanded value due to date of generation of report
    modelParams["to_date"] = modelParams["to_date"] || analytics.configuration.getServerProperty("reportGenerationDate");

    for (var rowNumber = 0; rowNumber < table.rows.length; rowNumber++) {
        // check if there is expandable metric in row
        var metricName = expandableMetricPerSection[tableNumber][rowNumber + 1];  // taking into account absent title row
        if (typeof metricName != "undefined") {
            for (var columnNumber = 1; columnNumber < table.rows[rowNumber].length; columnNumber++) {
                var columnValue = table.rows[rowNumber][columnNumber];

                // don't display link to empty drill down page
                if (!this.isEmptyValue(columnValue)) {
                    var timeInterval = columnNumber - 1;
                    var drillDownPageLink = this.getDrillDownPageLink(metricName, modelParams, timeInterval);

                    table.rows[rowNumber][columnNumber] = "<a href='" + drillDownPageLink + "'>" + columnValue + "</a>";
                }
            }
        }
    }

    return table;
}