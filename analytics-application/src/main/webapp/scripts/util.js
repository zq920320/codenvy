var analytics = analytics || {};
analytics.util = new Util();

function Util() {
		
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

	
    /** ****************** library API ********** */
    return {
    	constructUrlParams: constructUrlParams,
    	extractUrlParams: extractUrlParams,
    }

}