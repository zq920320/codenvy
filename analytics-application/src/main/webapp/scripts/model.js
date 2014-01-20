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

analytics.model = new Model();

function Model() {
    var currentAjaxRequest = null;
    
    var params = {};
    
    var doneFunctionStack = new Array();
    
    var failFunctionStack = new Array();
    
    function doneFunction(data){
        for (var i = doneFunctionStack.length - 1; i >= 0; i--) {
            doneFunctionStack[i](data);
        }
    }
    
    function failFunction(status, textStatus, errorThrown){
        for (var i = failFunctionStack.length - 1; i >= 0; i--) {
            failFunctionStack[i](status, textStatus, errorThrown);
        } 
    }
    
	function getMetricValue(modelName, isAsync) {
        if (typeof isAsync == "undefined") {
            isAsync = true;
        }
	    var url = '/api/analytics/metric/' + modelName;

	    var callback = function(data) {
            data = parseInt(data.value);   
            
            doneFunction(data);	        
	    };
	    
        var request = get(url, "json", callback, isAsync);
        
        if (!isAsync) {
            data = jQuery.parseJSON(request.responseText);
            
            data = parseInt(data.value);
            return data;
        }
	}
	
	function getAllResults(modelName, isAsync) {
        if (typeof isAsync == "undefined") {
            isAsync = true;
        }
		var url = "/api/view/get/" + modelName;

		var callback = function(data) {
            data = convertJsonToTables(data);
            
            doneFunction(data);
		}

		var request = get(url, "json", callback, isAsync);
		
		if (!isAsync) {
		    data = jQuery.parseJSON(request.responseText);
		    
    		data = convertJsonToTables(data);		
    		return data;
		}
	};
	   
    function get(url, responseType, doneCollback, isAsync) {
        var url = url || "";
        var responseType = responseType || "json";
        var doneCollback = doneCollback || function(){};
        
        if (!jQuery.isEmptyObject(params)) {
            url = url + "?" + analytics.util.constructUrlParams(params);
        }
        
        if (isAsync) {
            currentAjaxRequest = $.ajax({
               url: url,
               dataType: responseType,
               async: isAsync
            })
            .done(function(data) {
                doneCollback(data);
            })
            .fail(function(data, textStatus, errorThrown) {
                failFunction(data.status, textStatus, errorThrown);
            });
        } else {
            var response = currentAjaxRequest = $.ajax({
                url: url,
                dataType: responseType,
                async: isAsync
            });
            
            return response;
        }
    }   
	
	function convertJsonToTables(data) {
	    var result = {};
	
	    // get sorted tables identifications
	    var tableIds = [];
	    for (var id in data) {
	        tableIds.push(id);
	    }
	    tableIds.sort();
	
	    for (var t = 0; t < tableIds.length; t++) {
	        var rows = [];
	        var columns = [];
	
	        // get sorted rows' identifications
	        var rowIds = [];
	        for (var id in data[tableIds[t]]) {
	            rowIds.push(id);
	        }
	        rowIds.sort();
	
	        // get sorted columns' identifications
	        var colIds = [];
	        for (var id in data[tableIds[t]]['r00']) {
	            colIds.push(id);
	        }
	        colIds.sort();
	
	        for (var r = 0; r < rowIds.length; r++) {
	            if (r == 0) {
	                for (var c = 0; c < colIds.length; c++) {
	                    columns.push(data[tableIds[t]][rowIds[r]][colIds[c]]);
	                }
	            } else {
	                var row = [];
	                for (var c = 0; c < colIds.length; c++) {
	                    row.push(data[tableIds[t]][rowIds[r]][colIds[c]]);
	                }
	                rows.push(row);
	            }
	        }
	
	        result[t] = {rows: rows, columns: columns};
	    }
	
	    return result;
	};


	function setParams(newParams) {
        params = newParams;
    }

    function pushDoneFunction(newDoneFunction) {
        doneFunctionStack.push(newDoneFunction);
    }

    function popDoneFunction() {
        doneFunctionStack.pop();
    }
    
    function clearDoneFunction() {
        doneFunctionStack = new Array();
    }

    function pushFailFunction (newFailFunction) {
        failFunctionStack.push(newFailFunction);
    }

    function clearFailFunction() {
        failFunctionStack = new Array();
    }
    
    // add handler of pressing "Esc" button
    $(document).keydown(function(event) {
       var escKeyCode = 27;
       if (event.which == escKeyCode) {
          if (currentAjaxRequest != null) {
             currentAjaxRequest.abort();
          }
       }
    });
    
    /** ****************** API ********** */
    return {
        getAllResults: getAllResults,
        getMetricValue: getMetricValue,
        setParams: setParams,
        
        pushDoneFunction: pushDoneFunction,
        popDoneFunction: popDoneFunction,
        clearDoneFunction: clearDoneFunction,
        doneFunction: doneFunction,
        
        pushFailFunction: pushFailFunction,
        clearFailFunction: clearFailFunction

    }
}