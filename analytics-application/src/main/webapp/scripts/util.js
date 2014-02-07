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
var analytics = analytics || {};
analytics.util = new Util();

function Util() {
		
	/**
	 * Construct url parameters String based on parameters from params object
	 * @param params object like {time_unit: Month, Email="test@gmail.com"}
	 * @returns url like "time_unit=Month&Email=test%40gmail.com"
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
	 * @param url like "http://127.0.0.1/timeline.jsp?time_unit=Month&Email=test%40gmail.com
	 * @returns params object like {time_unit: Month, Email: "test@gmail.com"}
	 */
	function extractUrlParams(url) {
	   if (url.indexOf('?') < 0) {
	      return {};
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
	
	/**
	 * Shallow copy object
	 * @see http://api.jquery.com/jQuery.extend/
	 */
	function clone(object) {
	    return jQuery.extend({}, object);
	}
	
	/**
	 * @see http://html5.litten.com/html5-web-storage-using-localstorage-and-sessionstorage-objects/
	 */
	function isBrowserSupportWebStorage() {
	    return typeof(Storage) != "undefined";
	}
	
    /** ****************** library API ********** */
    return {
    	constructUrlParams: constructUrlParams,
    	extractUrlParams: extractUrlParams,
    	clone: clone,
    	isBrowserSupportWebStorage: isBrowserSupportWebStorage,
    }

}