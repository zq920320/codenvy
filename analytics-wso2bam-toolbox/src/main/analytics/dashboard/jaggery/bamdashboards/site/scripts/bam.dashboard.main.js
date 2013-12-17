jQuery.fn.doesExist = function(){
   return jQuery(this).length > 0;
};

var currentAjaxRequest = null;

$(function () {
//    $("#server-dd").change(function () {
//        var selectedServer = $("#server-dd option:selected").text();
//        $("#service-dd").find('option').remove();
//        $("#operation-dd").find('option').remove();
//        if (selectedServer == '') {
//            triggerCollect();
//        }
//        else {
//            populateServicesCombo(selectedServer);
//        }
//    });
//    $("#service-dd").change(function () {
//        var selectedServer = $("#server-dd option:selected").text();
//        var selectedService = $("#service-dd option:selected").text();
//        $("#operation-dd").find('option').remove();
//        if (selectedService == '') {
//            triggerCollect();
//        }
//        else {
//            populateOperationsCombo(selectedServer, selectedService);
//        }
//    });
//    $("#operation-dd").change(function () {
//        triggerCollect();
//    });
    $("#clearSelectionBtn").click(function () {
//        $("#server-dd option:first-child").attr("selected", "selected");
//        $("#service-dd").find('option').remove();
//        $("#operation-dd").find('option').remove();
//        triggerCollect();
//        $("#service-dd").find('option').remove();
//        $("#operation-dd").find('option').remove();
        
        $("#filter-by button").removeClass('btn-primary');
        $("#filter-by input[name='keyword']").val("");
        
        var targetDiv = $("#filter-by").attr("target");
        if (typeof targetDiv != "undefined") {
           triggerCollect(targetDiv);
        }
    });
    
    // Time selectors group
    $("#timely-dd button").click(function () {
        $("#timely-dd button").removeClass('btn-primary');
        $(this).addClass('btn-primary');
        
        var targetDiv = $("#timely-dd").attr("target");
        if (typeof targetDiv != "undefined") {
           triggerCollect(targetDiv);
        }
    });
    
    // "Filter by" group
    $("#filter-by button").click(function() {
       $("#filter-by button").removeClass('btn-primary');
       if ($("#filter-by input[name='keyword']").val() != "") {  // select button only if there is some text in keyword input
          $(this).addClass('btn-primary');
       }
       
       var targetDiv = $("#filter-by").attr("target");
       if (typeof targetDiv != "undefined") {
          triggerCollect(targetDiv);
       }
    });
});

function triggerCollect(targetDiv) {   
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

    reloadDiv(params, targetDiv);
};

function reloadDiv(params, targetDiv) {
   var div = jQuery("#" + targetDiv);
   
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
      window.history.pushState({html: div.html(), params: params}, document.title, newPageUrl);
   });
   
}


function reloadThroughAjax(containerToReload, url, callback) {
   containerToReload.empty();  // clear container
   
   displayLoader(); // display loader
   
   var ajaxRequest = $.ajax({
      url: url
      
    }).fail(function(data, textStatus) {
       hideLoader();
       containerToReload.html("Error of processing request: " + textStatus);
       
    }).done(function(data) {
      hideLoader(); // display loader
      
      containerToReload.html(data);  // update div with response
 
      callback();
   });
   
   return ajaxRequest;
}


function loadDashboardWidget(gadgetUrl, widgetId) { 
   if (typeof widgetId == "undefined") {
      return;
   }

   var params = extractUrlParams(window.location.href);
   
   params = getParametersWithPresetDefaults(params);
   
   if (params != null && Object.keys(params).length > 0) {
      var absUrlArray = window.location.href.split('?');
      var urlParamsString = absUrlArray[1];

      var div = jQuery("#" + widgetId);
      var callback = function() {};
      if (typeof urlParamsString != "undefined") {
         gadgetUrl += "?" + urlParamsString;
      } else {
         callback = function() {
            // rewrite page location to make it possible to navigate new url through the browser's history
            window.history.pushState({"html": div.html(), params: params}, document.title, window.location.href);
         };
      }
      
      currentAjaxRequest = reloadThroughAjax(div, gadgetUrl, callback);
   }
      
   updateCommandButtonsState(params);
   
   // update div when navigating in history
   window.addEventListener('popstate', function(event) {
      if (event.state != null && typeof event.state.params != "undefined" && Object.keys(event.state.params).length > 0) {
         // update parameter buttons selsction
         var params = event.state.params;
         params = getParametersWithPresetDefaults(params);
         updateCommandButtonsState(params);
      }
      
      if (event.state != null && typeof event.state.html != "undefined") {
         jQuery("#" + widgetId).html(event.state.html);
      }
   });
}

function updateCommandButtonsState(params) {
   var params = params || {};
   
   // update time selection buttons
   var timeUnitButtons = jQuery("#timely-dd button"); 
   if (timeUnitButtons.doesExist()) {
      timeUnitButtons.removeClass('btn-primary');
      jQuery("#timely-dd button:contains('" + params["timeGroup"] + "')").addClass('btn-primary');
   }
   
   // update filter-by buttons
   var filterButtons = jQuery("#filter-by button");
   var filterInput = $("#filter-by input[name='keyword']");
   if (filterButtons.doesExist() && filterInput.doesExist()) {
      jQuery("#filter-by button").removeClass('btn-primary');   // 
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
}

function getParametersWithPresetDefaults(params) {
   var params = params || {};
   var DEFAULT_TIME_UNIT_VALUE = "Day";
   
   if (typeof params["timeGroup"] == "undefined") {
      params["timeGroup"] = DEFAULT_TIME_UNIT_VALUE;
   }
   
   return params;
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
    $.ajax({
        url: 'populate_combos_ajaxprocessor.jag',
        dataType: 'json',
        success: function (result) {

            var options = "<option value='__default__'></option>";
            for (var i = 0; i < result.length; i++) {
                var data = result[i];
                for (var key in data) {
                    options = options + "<option>" + data[key] + "</option>"
                }
            }
            $("#server-dd").find('option').remove();
            $("#server-dd").append(options);
        }

    });

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
function populateServicesCombo(server) {
    $.ajax({
        url: 'populate_combos_ajaxprocessor.jag?server=' + server + '',
        dataType: 'json',
        success: function (result) {

            var options = "<option value='__default__'></option>";
            for (var i = 0; i < result.length; i++) {
                var data = result[i];
                for (var key in data) {
                    options = options + "<option>" + data[key] + "</option>"
                }
            }

            $("#service-dd").append(options);
            triggerCollect();//$("#service-dd").ufd({log:true,addEmphasis: true});
        }


    });

};
function populateOperationsCombo(server, service) {

    $.ajax({
        url: 'populate_combos_ajaxprocessor.jag?server=' + server + '&service=' + service + '',
        dataType: 'json',
        success: function (result) {

            var options = "<option value='__default__'></option>";
            for (var i = 0; i < result.length; i++) {
                var data = result[i];
                for (var key in data) {
                    if (data[key] !== null) {
                        options = options + "<option>" + data[key] + "</option>";
                    }
                }
            }

            $("#operation-dd").append(options);
            triggerCollect();    //$("#operation-dd").ufd({log:true,addEmphasis: true});
        }
    });
};


function reloadIFrame(param) {
   $("iframe").each(function () {
       var currentUrl = $(this).attr('src');
       if (currentUrl.indexOf('?')) {
           var absUrl = currentUrl.split('?');
           currentUrl = absUrl[0];
       }
       var newUrl = currentUrl + "?timeGroup=" + param.timeGroup;
       $(this).attr('src', newUrl);
   });
};


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
   
   // add hendler of pressing "Esc" button
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
   needLoader = true;
     
   // display loader after the 2 seconds of timeout
   var timeoutInMillisec = 2000;
   setTimeout(function() {
      if (needLoader) {
         loader.show();
      }
   }, timeoutInMillisec);
}

function hideLoader() {
   needLoader = false;
   loader.hide();
}