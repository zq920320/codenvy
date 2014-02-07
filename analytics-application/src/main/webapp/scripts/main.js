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
jQuery.fn.doesExist = function(){
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
            
    var setupButtons = function() {        
        // Time selectors group
        $("#timely-dd button.command-btn").click(function () {
            $("#timely-dd button").removeClass('btn-primary');
            $(this).addClass('btn-primary');
            reloadWidgets($("#timely-dd").attr("targetWidgets"));
        });
        
        // "Filter by" group
        $("#filter-by button.command-btn").click(function() {
           $("#filter-by button").removeClass('btn-primary');
           if ($("#filter-by input[name='keyword']").val() != "") {  // select button only if there is some text in keyword input
              $(this).addClass('btn-primary');
              reloadWidgets($("#filter-by").attr("targetWidgets"));
           }
        });
        $("#filter-by button.clear-btn").click(function() {    // clearing
            $("#filter-by button").removeClass('btn-primary');
            $("#filter-by input[name='keyword']").val("");
            
            reloadWidgets($("#filter-by").attr("targetWidgets"));            
        });
        
        // "Date range" group
        $("#date-range button.command-btn").click(function() {
           $("#date-range button").removeClass('btn-primary');
           if ($("#date-range input[name='from_date']").val() != ""
                 || $("#date-range input[name='to_date']").val() != "") {  // select button only if there is date in one of date range input
              $(this).addClass('btn-primary');
              reloadWidgets($("#date-range").attr("targetWidgets"));
           }
        });
        $("#date-range button.clear-btn").click(function() {    // clearing
            $("#date-range button").removeClass('btn-primary');
            $("#date-range input[name='from_date']").val("");
            $("#date-range input[name='to_date']").val("");
            
            reloadWidgets($("#date-range").attr("targetWidgets"));          
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
    };
    
    /**
     * Get params from buttons state
     */
    function getParamsFromButtons() {   
        var params = {};
        
        // process time selector
        var selectedTimeButton = $("#timely-dd button.btn-primary");
        if (selectedTimeButton.doesExist()) {
           params.time_unit = selectedTimeButton.val();       
        }
        
        // process filter
        var filterInput = $("#filter-by input[name='keyword']");
        var selectedFilterButton = $("#filter-by button.btn-primary")
        if (selectedFilterButton.doesExist() 
              && filterInput.val().length > 0) {
           params[selectedFilterButton.val()] = filterInput.val(); 
        }
    
        // process date-range
        var fromDateInput = $("#date-range input[name='from_date']");
        if (fromDateInput.doesExist() 
              && fromDateInput.val() != "") {
           params["from_date"] = fromDateInput.val();       
        }
        var toDateInput = $("#date-range input[name='to_date']");
        if (toDateInput.doesExist() 
              && toDateInput.val() != "") {
           params["to_date"] = toDateInput.val();       
        }
    
        // process userid url query parameter
        var urlParams = analytics.util.extractUrlParams(window.location.href);
        if (urlParams != null 
              && typeof urlParams["user"] != "undefined") {
           params["user"] = urlParams["user"];       
        }
        
        // process metric selector
        var selectedMetricButton = $("#metric button.btn-primary");
        if (selectedMetricButton.doesExist()) {
           params.metric = selectedMetricButton.val();       
        }    

        // process ide version selector
        var ideVersionButton = $("#ide-version a.btn-primary");
        if (ideVersionButton.doesExist()) {            
           params.ide = ideVersionButton.attr("value"); 
           updateGlobalParamInStorage("ide", params.ide);
        }
        
        return params;
    };
    
    /**
     * Reload div on clicking on page navigation links at the bottom of the tables
     * @param pageNavigationLinkElement dom-element
     * @param widgetName id of div to reload
     */
    function reloadWidgetOnPageNavigation(pageNavigationLinkElement, widgetName) {
        var jQueryPageLinkElement = jQuery(pageNavigationLinkElement);
        var href = jQueryPageLinkElement.attr("href");
        
        var urlParams = analytics.util.extractUrlParams(href);
        
        reloadWidget(widgetName, urlParams);
    }

    /**
     * Reload widgets
     * @param widgetNames: String with widget names divided by ","
     */
    function reloadWidgets(widgetNames) { 
        if (typeof widgetNames != "undefined") {
           var widgetName = widgetNames.split(',');
           
           if (widgetName == "_all") {
               loadAllWidgets(getParamsFromButtons());
               
           } else {
               for (var i in widgetName) {
                   reloadWidget(widgetName[i]);
               }
           }
        }
    }
    
    function reloadWidget(widgetName, params) {
        var params = params || getParamsFromButtons();
        
        loadWidget(widgetName, params, function(data) {
          // rewrite page location to make it possible to navigate new url through the browser's history
          if (analytics.configuration.getProperty(widgetName, "isNeedToSaveInHistory")) {
             var pageUrl = window.location.href;
             if (pageUrl.indexOf('?')) {
                var absUrl = pageUrl .split('?');
                pageUrl = absUrl[0];
             }
             
             var newPageUrl = pageUrl;
             var urlParams = analytics.util.constructUrlParams(params);
             if (urlParams != null) {
                newPageUrl += "?" + urlParams;
             }
          
             window.history.pushState({"widgetName": widgetName}, document.title, newPageUrl);
          }
       });
       
    }
    
    function loadAllWidgets(params) {
       if (typeof params == "undefined") {
           params = analytics.util.extractUrlParams(window.location.href);
           updateGlobalParamsWithValuesFromStorage(params);
           updateCommandButtonsState(params);
       }
       
       var widgetNames = analytics.configuration.getWidgetNames();
       for(var i in widgetNames) {
           var widgetName = widgetNames[i];
           if (jQuery("#" + widgetName).doesExist()) {
               if (analytics.configuration.getProperty(widgetName, "isNeedToSaveInHistory")) {       
                   // update div when navigating in history
                   var everPushedSomething = false;
                   var initialUrl = window.location.href;
                   window.addEventListener('popstate', function(event) {
                      if (! everPushedSomething 
                             && window.location.href == initialUrl) {
                         everPushedSomething = true;
                         return;
                      }

                      if (event.state != null
                              && typeof event.state.widgetName != "undefined") {
                          // update parameter buttons selection
                          var params = analytics.util.extractUrlParams(window.location.href);
                          updateCommandButtonsState(params);

                          loadWidget(event.state.widgetName, params);
                      }
                   });
               }
               
               loadWidget(widgetName, params);
           }
       }
    }
    
    function loadWidget(widgetName, params, callback) {
        var params = params || {};
        var callback = callback || function(){};
        
        var view = analytics.factory.getView(widgetName);
        view.setParams(params);

        // display loader after the 2 seconds of timeout
        var needLoader = true;
        var timeoutInMillisec = 2000;          
        setTimeout(function() {
           if (needLoader) {
               analytics.views.loader.show();               
           }
        }, timeoutInMillisec);
        
        var model = analytics.factory.getModel(widgetName);
        model.clearDoneFunction();
        model.pushDoneFunction(function() {
            needLoader = false;
            view.show();
            analytics.views.loader.hide();
            callback();            
        })      
        
        model.clearFailFunction();
        model.pushFailFunction(function(status, textStatus, errorThrown) {
            if (textStatus == "abort") {
                needLoader = false;
                analytics.views.loader.hide();
                
                view.showAbortMessage();
            } else {
                needLoader = false;
                analytics.views.loader.hide();
                
                view.showErrorMessage(status, textStatus, errorThrown);
            }
        });
        
        var presenter = analytics.factory.getPresenter(widgetName, view, model);        

        view.clear();        
        presenter.load();
    }
    
    
    function updateCommandButtonsState(params) {
       var params = params || {};
       
       // update time selection buttons
       var timeUnitButtons = jQuery("#timely-dd button"); 
       if (timeUnitButtons.doesExist()) {
          setPrimaryButtonOnValue(timeUnitButtons, params["time_unit"]);
       }
       
       // update filter-by group
       var filterButtons = jQuery("#filter-by button");
       var filterInput = $("#filter-by input[name='keyword']");
       if (filterButtons.doesExist() && filterInput.doesExist()) {
          jQuery("#filter-by button").removeClass('btn-primary');
          filterInput.val("");
          
          // find out "filter by" param like "Email: test@test.com" which is linked with button with text "Email"
          for (var i = 0; i < filterButtons.length; i++) {
             var button = jQuery(filterButtons[i]);
             var filterParamValue = params[button.attr("value")];
             if (typeof filterParamValue != "undefined") {
                button.addClass('btn-primary');
                filterInput.val(filterParamValue);   // set keyword input = value from param
                break;
             }
          }
       }
       
       // update date-range group
       var isDateParameterPresenceInQuery = false;
       {  var input = $("#date-range input[name='from_date']");
          var queryParam = params["from_date"];
          if (input.doesExist()) {
             if (typeof queryParam != "undefined") {
                input.val(queryParam);   
                isDateParameterPresenceInQuery = true;
             } else {
                input.val("");
             }
          }
       }
       {  var input = $("#date-range input[name='to_date']");
          var queryParam = params["to_date"];
          if (input.doesExist()) {
             if (typeof queryParam != "undefined") {
                input.val(queryParam);   
                isDateParameterPresenceInQuery = true;
             } else {
                input.val("");
             }
          }
       }
       jQuery("#date-range button").removeClass('btn-primary');
       var dateRangeButton = jQuery("#date-range button:contains('Filter')");   
       if (dateRangeButton.doesExist() && isDateParameterPresenceInQuery) {
          jQuery("#date-range button").removeClass('btn-primary');
          dateRangeButton.addClass('btn-primary');
       }
       
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
    }

    /**
     * Update undefined global params with values from HTML5 Web Storage
     */
    function updateGlobalParamsWithValuesFromStorage(params) {
        if (!analytics.util.isBrowserSupportWebStorage()) {
            return;
        }
        
        var globalParamList = analytics.configuration.getGlobalParamList();
        for (var i in globalParamList) {
            var globalParamName = globalParamList[i];
            var storedParam = localStorage.getItem(globalParamName);  // get param value from HTML5 Web Storage
            if (typeof params[globalParamName] == "undefined" 
                    && storedParam != null) {
                params[globalParamName] = storedParam;
            }
        }
    }
    
    /**
     * Save global param value in the HTML5 Web Storage
     */
    function updateGlobalParamInStorage(parameter, value) {
        if (!analytics.util.isBrowserSupportWebStorage()) {
            return;
        }
        
        if (analytics.configuration.isParamGlobal(parameter)) {
            if (typeof value != "undefined") {
                localStorage.setItem(parameter, value);    // save param value in the HTML5 Web Storage                
            } else {
                localStorage.removeItem(parameter);    // remove param from HTML5 Web Storage
            }
        }
    }
    
    function setPrimaryButtonOnValue(buttonGroup, parameter) {
        buttonGroup.removeClass('btn-primary');
        if (typeof parameter != "undefined") {
            // set as primary the button with value = parameter 
            for (var i = 0; i < buttonGroup.size(); i++) {
               var button = jQuery(buttonGroup[i]);
               if (button.attr("value") == parameter) {
                  button.addClass("btn-primary");
                  break;
               }
            }
        } else {
           // restore default primary button
           for (var i = 0; i < buttonGroup.size(); i++) {
               var button = jQuery(buttonGroup[i]);
               if (typeof button.attr("default") != "undefined") {
                  button.addClass("btn-primary");
                  break;
               }
           }
        }
     }
    
    /** ****************** library API ********** */
    return {
        reloadWidgetOnPageNavigation: reloadWidgetOnPageNavigation
    }
}