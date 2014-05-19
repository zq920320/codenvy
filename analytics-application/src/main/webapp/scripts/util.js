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
     * Set operation 'diff': return (map1 - map2)
     */
    function diff(map1, map2) {
        var diffMap = clone(map1);
        var map2Keys = Object.keys(map2);
        for (var i in map2Keys) {
            var map2Key = map2Keys[i];
            if (typeof diffMap[map2Key] != "undefined") {
                delete diffMap[map2Key];
            }
        }
        
        return diffMap;
    }

    /**
     * Set operation 'union': return unionMap = (map1 + map2) where map1 params are re-written with params from map2. 
     */
    function unionWithRewrite(map1, map2) {
        var unionMap = clone(map1);
        var map2Keys = Object.keys(map2);
        for (var i in map2Keys) {
            var map2Key = map2Keys[i];
            unionMap[map2Key] = map2[map2Key];   // re-write map1 param value with param value from map2 
        }
        
        return unionMap;
    }
    
    /**
     * Return subset of map with keys from keyList
     */
    function getSubset(map, keyList) {
        var subsetMap = {};
        for (var i in map) {
            
            for (var j in keyList) {
                if (keyList[j] == i) {
                    subsetMap[i] = map[i];
                    break;
                }
            }
        }
        
        return subsetMap;
    }
    
    
    /**
     * Remove all params with null values from map
     */
    function removeParamsWithNullValues(map) {
        for (var i in map) {
            if (map[i] == null) {
                delete map[i];   // remove null map param
            }
        }
        
        return map;
    }

     /**
     * Remove from map all params with names from array namesOfParamsToRemove 
     */
    function removeParams(map, namesOfParamsToRemove) {
        for (var i in namesOfParamsToRemove) {
            delete map[namesOfParamsToRemove[i]];
        }
        
        return map;
    }
    
    /** Return array without certain elements  
     *  @param elements - list of elements to remove.
     * */
    function removeElementsFromArray(array, elements) {
        for (var i in elements) { 
            var element = elements[i];
            var index = array.indexOf(element);
            if (index > -1) {
                array.splice(index, 1);
            }
        }
        
        return array;
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
    
    /**
     * Logout user from codenvy.
     * @see Codenvy Authentication API here https://wiki.codenvycorp.com/display/PSR/Authentication+API 
     */
    function processUserLogOut() {
        $.ajax({
            url: "/api/auth/logout",
            type: "POST"
        })
        .done(function (data) {
            window.location = "/site/login";
        })
        .fail(function (data, textStatus, errorThrown) {
            window.location = "/site/login";
        });
        
        return false;
    }
    
    /**
     * Return index of column with certain name.
     */
    function getColumnIndexByColumnName(columns, columnName) {
        for (var i in columns) {
            if (columns[i] == columnName) {
                return i;
            }
        }
        
        return null;
    }

    /**
     * @returns index of value in array, or null.
     */
    function getArrayValueIndex(array, value) {
        for (var i in array) {
            if (array[i] == value) {
                return i;
            }
        }
        
        return null;
    }
    
    /**
     * @returns key of map entry with value, or null 
     */
    function getKeyByValue(map, value) {
        var keys = Object.keys(map);
        for (var i in keys) {
            var key = keys[i];
            
            if (map[key] == value) {
                return key;
            }
        }
        
        return null;
    }
    
    /**
     * For example: 
     * 1) return "user-view.jsp" from page with next urls:
     * "http://localhost:9763/analytics/pages/user-view.jsp" or
     * "http://localhost:9763/analytics/pages/user-view.jsp?user=123"
     * 
     * 2) return "" from page with next urls:
     * "http://localhost:9763/analytics/pages/" or
     * "http://localhost:9763/analytics/pages/?user=123" 
     */
    function getCurrentPageName() {
        var currentPageName = "";
        var currentPageHref = location.href;

        if (currentPageHref.indexOf("?") >= 0) {   // check if there is query parameters
            var matches = currentPageHref.match(/\/(\/*.*\/)(.*)[?]+/);
            if (matches.length > 0) {
                currentPageName = matches[matches.length - 1];
            }
            
        } else {
            var matches = currentPageHref.match(/(\/*.*\/)(.*)/);
            if (matches.length > 0) {
                currentPageName = matches[matches.length - 1];
            }
        }
        
        return currentPageName;
    }
    
    /**
     * Translate dateString from "yyyy-mm-dd" to "yyyymmdd".
     */
    function encodeDate(dateString) {
        return dateString.replace(/-/g, "");
    }
    
    /**
     * Translate dateString from "yyyymmdd" to "yyyy-mm-dd".
     */
    function decodeDate(dateString) {
        return dateString.substring(0,4) + "-" + dateString.substring(4,6) + "-" + dateString.substring(6,8); 
    }

    
    /**
     * @returns shorten version of factory url, for example: 
     * initial url: "https://codenvy.com/factory?v=1.0&pname=EspressoDemo&wname=EspressoLogic&action=openproject
     *           &openfile=index.html&vcs=git&idcommit=none&vcsurl=https://github.com/EspressoLogicDemo/Demo_4xslT.git"
     * shorten url: "1.0 | EspressoDemo | EspressoLogic | openproject | index.html | git | none | Demo_4xslT.git"
     */
    function getShortenFactoryUrl(factoryUrl) {
        var shortenFactoryUrl = factoryUrl;
        
        shortenFactoryUrl = shortenFactoryUrl.replace(/^[\w\/.:]*[?]/, "");  // remove prefix "https://codenvy.com/factory?"
        
        // shortening url in parameter "vcsurl": 
        // replace "vcsurl=https://github.com/EspressoLogicDemo/Demo_4xslT.git" on "vcsurl=Demo_4xslT.git"
        var shortenVcsurl = shortenFactoryUrl.match(/vcsurl=[\w\/:.]*\/([\w]*)/);
        if (shortenVcsurl != null) {
            shortenVcsurl = shortenVcsurl[1];
            shortenFactoryUrl = shortenFactoryUrl.replace(/vcsurl=[\w\/:.]*/, "vcsurl=" + shortenVcsurl);
        }
        
        // replace all URL query parameter names on " | "
        shortenFactoryUrl = shortenFactoryUrl.replace(/^[\w]+=/, "");  // replace first parameter name without starting "&"
        shortenFactoryUrl = shortenFactoryUrl.replace(/&[\w]+=/g, " | ");
            
        return shortenFactoryUrl;
    }
    
    
    /**
     * @returns object with first property only, for example:
     * >>> if 
     * object = {
     *      property1: value1,
     *      property2: value2,
     *      ...
     * } 
     * >>> then this method returns 
     * object = {
     *      property1: value1
     * }
     */
    function getObjectWithFirstPopertyOnly(object) {
        if (typeof object != "undefined" 
               && Object.keys(object).length > 0) {
            var firstPropertyName = Object.keys(object)[0];
            var firstPropertyValue = object[firstPropertyName];
            object = {};
            object[firstPropertyName] = firstPropertyValue;
        }
    
        return object;
    }
    
    
    /** ****************** library API ********** */
    return {
        // url manipulations
        constructUrlParams: constructUrlParams,
        extractUrlParams: extractUrlParams,
        getCurrentPageName: getCurrentPageName,
        
        // set operations
        diff: diff,
        unionWithRewrite: unionWithRewrite,
        getSubset: getSubset,
        
        // operations with parameters
        removeParamsWithNullValues: removeParamsWithNullValues,
        removeParams: removeParams,
        removeElementsFromArray: removeElementsFromArray,
        
        // operations with objects
        getObjectWithFirstPopertyOnly: getObjectWithFirstPopertyOnly,
        
        // other operations
        getArrayValueIndex: getArrayValueIndex,
        getKeyByValue: getKeyByValue,
        getColumnIndexByColumnName: getColumnIndexByColumnName,
        
        isBrowserSupportWebStorage: isBrowserSupportWebStorage,
        
        clone: clone,
        
        processUserLogOut: processUserLogOut,
        
        getShortenFactoryUrl: getShortenFactoryUrl,
        
        // date coding
        encodeDate: encodeDate,
        decodeDate: decodeDate,
    }

}