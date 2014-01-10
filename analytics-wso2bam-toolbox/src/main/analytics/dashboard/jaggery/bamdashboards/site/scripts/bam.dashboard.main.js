jQuery.fn.doesExist = function(){
   return jQuery(this).length > 0;
};

var currentAjaxRequest = null;

$(function () {
    $("#clearSelectionBtn").click(function () {
        // "Filter by" group
        $("#filter-by button").removeClass('btn-primary');
        $("#filter-by input[name='keyword']").val("");
        
        var targetDivId = $("#filter-by").attr("target");
        if (typeof targetDivId != "undefined") {
           triggerCollect(targetDivId);
        }
        
        // "Date range" group
        $("#date-range button").removeClass('btn-primary');
        $("#date-range input[name='from_date']").val("");
        $("#date-range input[name='to_date']").val("");
        
        var targetDivId = $("#date-range").attr("target");
        if (typeof targetDivId != "undefined") {
           triggerCollect(targetDivId);
        }
    });
    
    // Time selectors group
    $("#timely-dd button").click(function () {
        $("#timely-dd button").removeClass('btn-primary');
        $(this).addClass('btn-primary');
        
        var targetDivId = $("#timely-dd").attr("target");
        if (typeof targetDivId != "undefined") {
           triggerCollect(targetDivId);
        }
    });
    
    // "Filter by" group
    $("#filter-by button").click(function() {
       $("#filter-by button").removeClass('btn-primary');
       if ($("#filter-by input[name='keyword']").val() != "") {  // select button only if there is some text in keyword input
          $(this).addClass('btn-primary');
       }
       
       var targetDivId = $("#filter-by").attr("target");
       if (typeof targetDivId != "undefined") {
          triggerCollect(targetDivId);
       }
    });
    
    // "Date range" group
    $("#date-range button").click(function() {
       $("#date-range button").removeClass('btn-primary');
       if ($("#date-range input[name='from_date']").val() != ""
             || $("#date-range input[name='to_date']").val() != "") {  // select button only if there is date in one of date range input
          $(this).addClass('btn-primary');
       }
       
       var targetDivId = $("#date-range").attr("target");
       if (typeof targetDivId != "undefined") {
          triggerCollect(targetDivId);
       }
    });
    
    // Metric selectors group
    $("#metric button").click(function () {
        $("#metric button").removeClass('btn-primary');
        $(this).addClass('btn-primary');
        
        var targetDivId = $("#metric").attr("target");
        if (typeof targetDivId != "undefined") {
           triggerCollect(targetDivId);
        }
    });
});

function triggerCollect(targetDivId) {   
    var params = {};
    
    // process time selector
    var selectedTimeButton = $("#timely-dd button.btn-primary");
    if (selectedTimeButton.doesExist()) {
       params.timeGroup = selectedTimeButton.text();       
    }
    
    // process filter
    var filterInput = $("#filter-by input[name='keyword']");
    var selectedFilterButton = $("#filter-by button.btn-primary")
    if (selectedFilterButton.doesExist() 
          && filterInput.val().length > 0) {
       params[selectedFilterButton.text()] = filterInput.val(); 
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
    var urlParams = extractUrlParams(window.location.href);
    if (urlParams != null 
          && typeof urlParams["user"] != "undefined") {
       params["user"] = urlParams["user"];       
    }
    
    // process metric selector
    var selectedMetricButton = $("#metric button.btn-primary");
    if (selectedMetricButton.doesExist()) {
       params.metric = selectedMetricButton.text();       
    }    
    
    reloadDiv(params, targetDivId, true);
};

/**
 * Reload div on clicking on page navigation links at the bottom of the tables
 * @param pageNavigationLinkElement dom-element
 * @param dashboardWidgetId id of div to reload
 */
function reloadDivOnPageNavigation(pageNavigationLinkElement, dashboardWidgetId) {
	var jQueryPageLinkElement = jQuery(pageNavigationLinkElement);
    var href = jQueryPageLinkElement.attr("href");
    
    var urlParams = extractUrlParams(href);
    
    reloadDiv(urlParams, dashboardWidgetId, true);
}

function reloadDiv(params, targetDivId, isNeedToSaveInHistory) {
   var div = jQuery("#" + targetDivId);
   
   var divUrl = div.attr('src');

   if (divUrl.indexOf('?')) {
      var absUrl = divUrl.split('?');
      divUrl = absUrl[0];
   }
  
   var newDivUrl = divUrl;   
   var urlParams = constructUrlParams(params);
   if (urlParams != null) {
      newDivUrl += "?" + urlParams;
   }

   currentAjaxRequest = reloadThroughAjax(div, newDivUrl, function(data) {
      // rewrite page location to make it possible to navigate new url through the browser's history
	  if (typeof isNeedToSaveInHistory != "undefined" && isNeedToSaveInHistory) {
	     var pageUrl = window.location.href;
	     if (pageUrl.indexOf('?')) {
	        var absUrl = pageUrl .split('?');
	        pageUrl = absUrl[0];
	     }
	 	 
	     var newPageUrl = pageUrl;
	     var urlParams = constructUrlParams(params);
	     if (urlParams != null) {
	        newPageUrl += "?" + urlParams;
         }
      
         window.history.pushState({}, document.title, newPageUrl);
      }
   });
   
}


function reloadThroughAjax(containerToReload, url, callback) {
   var callback = callback || function(){};
	
   containerToReload.empty();  // clear container
   
   var needLoader = true;
   
   // display loader after the 2 seconds of timeout
   var timeoutInMillisec = 2000;
   setTimeout(function() {
      if (needLoader) {
    	  displayLoader(); // display loader
      }
   }, timeoutInMillisec);
   
   var ajaxRequest = $.ajax({
      url: url
      
    }).fail(function(data, textStatus) {
      needLoader = false;
      hideLoader();
       
      containerToReload.html("Error of processing request: " + textStatus);
       
    }).done(function(data) {
      needLoader = false;
      hideLoader(); // hide loader
      
      containerToReload.html(data);  // update div with response
 
      callback();
   });
   
   return ajaxRequest;
}


function loadDashboardWidget(gadgetUrl, widgetId, isNeedToSaveInHistory) { 
   if (typeof widgetId == "undefined") {
      return;
   }

   var div = jQuery("#" + widgetId);
   
   var params = extractUrlParams(window.location.href);

   updateCommandButtonsState(params);
      
   if (params != null && Object.keys(params).length > 0) {
      var absUrlArray = window.location.href.split('?');
      var urlParamsString = absUrlArray[1];

      if (typeof urlParamsString != "undefined") {
         gadgetUrl += "?" + urlParamsString;
      }   
   }
   
   if (typeof isNeedToSaveInHistory != "undefined" && isNeedToSaveInHistory) {       
       // update div when navigating in history
       var everPushedSomething = false;
       var initialUrl = window.location.href;
       window.addEventListener('popstate', function(event) {
     	 if (! everPushedSomething 
     			 && window.location.href == initialUrl) {
     		 everPushedSomething = true;
     		 return;
     	 }
     	 
          // update parameter buttons selection
          var params = extractUrlParams(window.location.href);
          updateCommandButtonsState(params);

          reloadDiv(params, widgetId, false);
       });
   }
   
   currentAjaxRequest = reloadThroughAjax(div, gadgetUrl);
}

function updateCommandButtonsState(params) {
   var params = params || {};
   
   // update time selection buttons
   var timeUnitButtons = jQuery("#timely-dd button"); 
   if (timeUnitButtons.doesExist()) {
	  setPrimaryButton(timeUnitButtons, params["timeGroup"]);
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
         var filterParamValue = params[button.text()];
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
	   setPrimaryButton(metricButtons, params["metric"]);
   }
}


function setPrimaryButton(buttonGroup, parameter) {
   buttonGroup.removeClass('btn-primary');
   if (typeof parameter != "undefined") {
	   // set as primary the button with label = parameter 
	   for (var i = 0; i < buttonGroup.size(); i++) {
		  var button = jQuery(buttonGroup[i]);
		  if (button.text() == parameter) {
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

/**
 * Construct url parameters String based on parameters from params object
 * @param params object like {timeGroup: Month, Email="test@gmail.com"}
 * @returns url like "timeGroup=Month&Email=test%40gmail.com"
 */
function constructUrlParams(params) {
   var params = params || {};
   if (Object.keys(params).length == 0) {
      return null;
   }
   
   var urlParamsString = "";
   for (var paramName in params) {
      if (params.hasOwnProperty(paramName)) {  // filter object in-build properties like "length"
         urlParamsString += "&" + encodeURIComponent(paramName) + "=" + encodeURIComponent(params[paramName]);
      }
   }
   
   urlParamsString = urlParamsString.substring(1);  // remove first "&"
   
   return urlParamsString;
}

/**
 * Extract url parameters from url
 * @param url like "http://127.0.0.1/timeline.jsp?timeGroup=Month&Email=test%40gmail.com
 * @returns params object like {timeGroup: Month, Email: "test@gmail.com"}
 */
function extractUrlParams(url) {
   if (url.indexOf('?') < 0) {
      return null;
   }
   
   var absUrl = url.split('?');
   var urlParamsArray = absUrl[1].split("&");
   var params = {};
   
   for (var i = 0; i < urlParamsArray.length; i++) {
      var paramArray = urlParamsArray[i].split("=");
      var paramName = decodeURIComponent(paramArray[0]);
      var paramValue = null;
      
      if (paramArray.length > 1) {
         paramValue = decodeURIComponent(paramArray[1]);
      }
      
      params[paramName] = paramValue;
   }
   
   return params;
}


function populateCombo(id, data) {

}

$(document).ready(function () {
    //If no user action, reload page to prevent session timeout.
    var wintimeout;

    function setWinTimeout() {
        wintimeout = window.setTimeout("location.reload(true)", 1740000); //setting timeout for 29 minutes. Actual timeout is 30 minutes.
    }

    $('body').click(function () {
        window.clearTimeout(wintimeout);
        setWinTimeout();
    });
    setWinTimeout();
});

/**
 * Loader
 */
var needLoader = false;
var loader = jQuery("#loader");
if (! loader.doesExist()) {
   jQuery("body").append(
      '<div id="loader">'
      + '<div class="loader-container"></div>'
      + '<table class="full-window-container">'
      + '   <tr>'
      + '     <td align="center">'
      + '        <div id="loader-img">'
      + '           <img src="images/loader.gif" />'
      + '         </div>'
      + '     </td>'
      + '   </tr>'
      + '</table>'
      + '</div>');
   
   loader = jQuery("#loader");
   
   // add handler of pressing "Esc" button
   $(document).keydown(function(event) {
      var escKeyCode = 27;
      if (event.which == escKeyCode) {
         hideLoader();
         if (currentAjaxRequest != null) {
            currentAjaxRequest.abort();
         }
      }
   });
}


function displayLoader() {
   loader.show();
}

function hideLoader() {
   loader.hide();
}