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
jQuery.fn.doesExist = function () {
    return jQuery(this).length > 0;
};

if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.main = new Main();

function Main() {
    jQuery(document).ready(function () {
        setupButtons();
        loadAllWidgets();
    });

    var setupButtons = function () {
        // Time selectors group
        $("#timely-dd button.command-btn").click(function () {
            $("#timely-dd button").removeClass('btn-primary');
            $(this).addClass('btn-primary');
            reloadWidgets($("#timely-dd").attr("targetWidgets"));
        });

        // "Filter by" group
        $("#filter-by input").keypress(function(event) {  // on "Enter" key pressed
            if ( event.which == 13 ) {
                event.preventDefault();
                reloadWidgets($("#filter-by").attr("targetWidgets"));
            }
        });
        $("#filter-by button.command-btn").click(function () {
            reloadWidgets($("#filter-by").attr("targetWidgets"));
        });
        $("#filter-by .clear-btn").click(function () {    // clearing
            clearFilter();
        });

        // Metric selectors group
        $("#metric button.command-btn").click(function () {
            $("#metric button").removeClass('btn-primary');
            $(this).addClass('btn-primary');

            reloadWidgets($("#metric").attr("targetWidgets"));
        });

        // Ide version selectors group
        $("#ide-version a.command-btn").click(function () {
            $("#ide-version a").removeClass('btn-primary');
            $(this).addClass('btn-primary');

            reloadWidgets($("#ide-version").attr("targetWidgets"));
        });

        // Data universe selectors group
        $("#data-universe a.command-btn").click(function () {
            $("#data-universe a").removeClass('btn-primary');
            $(this).addClass('btn-primary');

            reloadWidgets($("#data-universe").attr("targetWidgets"));
        });
        
        // UI preferences selectors group
        $("#ui-preferences button.command-btn").click(function () {
            $("#ui-preferences button").removeClass('btn-primary');
            $(this).addClass('btn-primary');

            var uiPreferences = $("#ui-preferences .btn-primary");
            if (typeof uiPreferences.attr("value") != "undefined") {
                analytics.util.updateGlobalParamInStorage("ui_preferences", uiPreferences.attr("value"));
                analytics.view.updateUI();
            }

        });
        
        // Show session events selector
        $("#show-session-events").click(function (event) {
            reloadWidgets($("#show-session-events").attr("targetWidgets"));
        });
        
        // Show factories selectors group
        $("#show-factories button.command-btn").click(function () {
            $("#show-factories button").removeClass('btn-primary');
            $(this).addClass('btn-primary');
            reloadWidgets($("#show-factories").attr("targetWidgets"));
        });
    };

    /**
     * Get params from buttons state
     */
    function getParamsFromButtons() {
        var params = {};

        // process filter
        params = getFilterParams($("#filter-by input"));
        
        // process time selector
        var selectedTimeButton = $("#timely-dd button.btn-primary");
        if (selectedTimeButton.doesExist()) {
            params.time_unit = selectedTimeButton.val();
        }

        // process metric selector
        var selectedMetricButton = $("#metric button.btn-primary");
        if (selectedMetricButton.doesExist()) {
            params.metric = selectedMetricButton.val();
        }

        // process ide version selector
        var ideVersionButton = $("#ide-version a.btn-primary");
        if (ideVersionButton.doesExist()) {
            if (typeof ideVersionButton.attr("value") != "undefined") {
                params.ide = ideVersionButton.attr("value");
            }
            // params["ide"] = null if ideVersionButton.attr("value") is undefined
            analytics.util.updateGlobalParamInStorage("ide", params["ide"]);
        }
        
        // process data universe selector
        var dataUniverseButton = $("#data-universe a.btn-primary");
        if (dataUniverseButton.doesExist()) {
            if (typeof dataUniverseButton.attr("value") != "undefined") {
                params.data_universe = dataUniverseButton.attr("value");
            }
            // params["data_universe"] = null if dataUniverseButton.attr("value") is undefined
            analytics.util.updateGlobalParamInStorage("data_universe", params["data_universe"]);
        }

        // process show session events selector
        var showSessionEventsCheckbox = $("#show-session-events");
        if (showSessionEventsCheckbox.doesExist()
            && !showSessionEventsCheckbox.prop("checked")) {
            params.event = showSessionEventsCheckbox.attr("inverseValue");
        }

        // process show-factories selector
        var selectedShowFactoriesButton = $("#show-factories button.btn-primary");
        if (selectedShowFactoriesButton.doesExist() && selectedShowFactoriesButton.val() != "") {
            params.encoded_factory = selectedShowFactoriesButton.val();
        }

        return params;
    };

    /**
     * Reload div on clicking on page navigation links at the bottom of the tables
     */
    function reloadWidgetByUrl(url, widgetName) {
        var urlParams = analytics.util.extractUrlParams(url);

        loadWidget(widgetName, urlParams, function() {
            jQuery(document).scrollTop( jQuery("#" + widgetName).offset().top );
        });
    }

    /**
     * Reload widgets
     * @param widgetNames: String with widget names divided by ","
     * @param namesOfParamsToRemove: set of parameter names from filter in which the clear button had been clicked
     */
    function reloadWidgets(widgetNames, namesOfParamsToRemove) {
        var namesOfParamsToRemove = namesOfParamsToRemove || {};

        if (typeof widgetNames != "undefined") {
            var widgetNames = widgetNames.split(',');

            var urlParams = analytics.util.extractUrlParams(window.location.href);
            var buttonParams = getParamsFromButtons();

            // remove unregistered url params
            var urlParams = analytics.util.getSubset(urlParams, analytics.configuration.getCrossPageParamsList());

            // union url params with button params and choose button params values above url params values
            var params = analytics.util.unionWithRewrite(urlParams, buttonParams);

            // remove conflicting params
            params = analytics.util.removeParams(params, namesOfParamsToRemove);

            // set 'page' parameter = 1 to receive correct result from server after performing filter from view filter (not view url)
            if (typeof params["page"] != "undefined") {
                params["page"] = 1;
            }

            if (widgetNames == "_all") {
                loadAllWidgets(params);
            } else {
                for (var i = 0; i < widgetNames.length; i++) {
                    loadWidget(widgetNames[i], params);
                }
            }
        }
    }

    function loadAllWidgets(params) {
        // load all widgets at first time of loading the page
        if (typeof params == "undefined") {
            params = analytics.util.extractUrlParams(window.location.href);
            analytics.util.updateGlobalParamsWithValuesFromStorage(params);
            updateFilterState(params);
        }

        var widgetNames = analytics.configuration.getWidgetNames();
        for (var i = 0; i < widgetNames.length; i++) {
            var widgetName = widgetNames[i];
            if (jQuery("#" + widgetName).doesExist()) {
                loadWidget(widgetName, params);
            }
        }
    }

    function loadWidget(widgetName, params, callback) {
        var params = params || {};
        var callback = callback || function () {};

        var view = analytics.factory.getView(widgetName);
        view.setParams(params);

        // display loader after the 2 seconds of timeout
        analytics.views.loader.needLoader = true;
        var timeoutInMillisec = 2000;
        setTimeout(function () {
            if (analytics.views.loader.needLoader) {
                analytics.views.loader.show();
            }
        }, timeoutInMillisec);

        var model = analytics.factory.getModel(widgetName);
        model.clearDoneFunction();
        model.pushDoneFunction(function () {
            if (!analytics.views.loader.needLoader) {  // verify if creating of widget is finished entirely
                view.show();
                analytics.views.loader.hide();
                callback();

                analytics.view.updateUI();
            }
        });

        model.clearFailFunction();
        model.pushFailFunction(function (status, textStatus, errorThrown) {
            analytics.views.loader.needLoader = false;
            analytics.views.loader.hide();

            if (textStatus == "abort") {
                view.showAbortMessage();
            } else if (textStatus == "error" && status == 0){
                view.showInterruptMessage();
            } else {
                view.showErrorMessage(status, textStatus, errorThrown);
            }
        });

        var presenter = analytics.factory.getPresenter(widgetName, view, model);

        view.clear();
        presenter.load();
    }


    function updateFilterState(params) {
        var params = params || {};

        // update time selection buttons
        var timeUnitButtons = jQuery("#timely-dd button");
        if (timeUnitButtons.doesExist()) {
            setPrimaryButtonOnValue(timeUnitButtons, params["time_unit"]);
        }

        // update filter-by group
        var filterInputs = $("#filter-by input");
        setFilterInputValues(filterInputs, params);

        // update metric selection buttons
        var metricButtons = jQuery("#metric button");
        if (metricButtons.doesExist()) {
            setPrimaryButtonOnValue(metricButtons, params["metric"]);
        }

        // update ide version selection buttons
        var ideVersionButtons = jQuery("#ide-version a");
        if (ideVersionButtons.doesExist()) {
            setPrimaryButtonOnValue(ideVersionButtons, params["ide"]);
        }

        // update data universe selection buttons
        var dataUniverseButtons = jQuery("#data-universe a");
        if (dataUniverseButtons.doesExist()) {
            setPrimaryButtonOnValue(dataUniverseButtons, params["data_universe"]);
        }
        
        // update ui preferences selection buttons
        var uiPreferencesButtons = jQuery("#ui-preferences button");
        if (uiPreferencesButtons.doesExist()) {
            setPrimaryButtonOnValue(uiPreferencesButtons, params["ui_preferences"]);
        }
        
        // update show session events selector
        var showSessionEventsCheckbox = jQuery("#show-session-events");
        if (showSessionEventsCheckbox.doesExist()) {
            if (typeof params["event"] != "undefined") {
                if (params["event"] == showSessionEventsCheckbox.attr("inverseValue")) {
                    showSessionEventsCheckbox.prop("checked", false);
                } else {
                    showSessionEventsCheckbox.prop("checked", true);
                }
            }
        }

        // update show encoded selection buttons
        var showFactoriesButtons = jQuery("#show-factories button");
        if (showFactoriesButtons.doesExist()) {
            setPrimaryButtonOnValue(showFactoriesButtons, params["encoded_factory"]);
        }
    }

    function setPrimaryButtonOnValue(buttonGroup, parameter) {
        buttonGroup.removeClass('btn-primary');
        if (typeof parameter != "undefined") {
            // set as primary the button with value = parameter
            for (var i = 0; i < buttonGroup.length; i++) {
                var button = jQuery(buttonGroup[i]);
                if (button.attr("value") == parameter) {
                    button.addClass("btn-primary");
                    break;
                }
            }
        } else {
            // restore default primary button
            for (var i = 0; i < buttonGroup.length; i++) {
                var button = jQuery(buttonGroup[i]);
                if (typeof button.attr("default") != "undefined") {
                    button.addClass("btn-primary");
                    break;
                }
            }
        }
    }

    /** Filter functions **/
    
    function getFilterParamNames(filterInputs) {
        var paramNames = [];
        for (var i = 0; i < filterInputs.length; i++) {
            if (typeof filterInputs[i] != "undefined"
                && typeof filterInputs[i].name != "undefined"
                && filterInputs[i].name != "") {
                paramNames.push(filterInputs[i].name);
            }
        }

        return paramNames;
    }

    function getFilterParams(filterInputs) {
        var params = {};
        for (var i = 0; i < filterInputs.length; i++) {
            var inputName = filterInputs[i].name;
            var inputValue = filterInputs[i].value;
            params[inputName] = inputValue;
        }

        return params;
    }
 
    function setFilterInputValues(filterInputs, params) {
        var isParamExists = false;
        for (var i = 0; i < filterInputs.length; i++) {
            var filterInput = filterInputs[i];
            filterInput.value = ""; 
            
            if (typeof params[filterInput.name] != "undefined") {
                var paramValue = params[filterInput.name];
                
                // translate date format from "yyyymmdd" to "yyyy-mm-dd"
                if (analytics.configuration.isDateParam(filterInput.name)) {
                    paramValue = analytics.util.decodeDate(paramValue);
                }
                
                filterInput.value = paramValue;
                isParamExists = true;
            }
        }
        
        // display filter panel as opened if there is at least one non-empty filter parameter
        analytics.views.accordion.display("filter-by", isParamExists);
    }
    
    function clearFilter() {
        var filterInputs = $("#filter-by input");
        for (var i = 0; i < filterInputs.length; i++) {
            filterInputs[i].value = "";
        }
        
        reloadWidgets($("#filter-by").attr("targetWidgets"));
        
        // close filter panel after the clearing
        analytics.views.accordion.close("filter-by");
    }
    
    /** ****************** library API ********** */
    return {
        reloadWidgetByUrl: reloadWidgetByUrl
    }
}