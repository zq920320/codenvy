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

    function doneFunction(data) {
        for (var i = doneFunctionStack.length - 1; i >= 0; i--) {
            doneFunctionStack[i](data);
        }
    }

    function failFunction(status, textStatus, errorThrown) {
        for (var i = failFunctionStack.length - 1; i >= 0; i--) {
            failFunctionStack[i](status, textStatus, errorThrown);
        }
    }

    function getMetricValue(modelMetricName, isAsync) {
        if (typeof isAsync == "undefined") {
            isAsync = true;
        }
        var url = '/analytics/api/view/metric/' + modelMetricName;

        var callback = function (data) {
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

    function getSummarizedMetricValue(modelMetricName, isAsync) {
        return doGetSpecificMetricValue(modelMetricName, "summary", isAsync);
    }

    function getExpandedMetricValue(modelMetricName, isAsync) {
        return doGetSpecificMetricValue(modelMetricName, "expand", isAsync);
    }

    /**
     * Returns a metric value depending on specific operation: expand, summary.
     */
    function doGetSpecificMetricValue(modelMetricName, specificOperations, isAsync) {
        if (typeof isAsync == "undefined") {
            isAsync = true;
        }
        var url = '/analytics/api/view/metric/' + modelMetricName + "/" + specificOperations;

        var callback = function (data) {
            data = convertJsonToTables(data);
            doneFunction(data);
        };

        var request = get(url, "json", callback, isAsync);

        if (!isAsync) {
            data = jQuery.parseJSON(request.responseText);
            data = parseInt(data.value);
            return data;
        }
    }

    function getModelViewData(modelViewName, isAsync) {
        if (typeof isAsync == "undefined") {
            isAsync = true;
        }
        var url = "/analytics/api/view/" + modelViewName;

        var callback = function (data) {
            data = convertJsonToTables(data);

            doneFunction(data);
        }

        var request = get(url, "json", callback, isAsync);

        if (!isAsync) {
            data = jQuery.parseJSON(request.responseText);

            data = convertJsonToTables(data);
            return data;
        }
    }

    function getExpandableMetricList(modelViewName, isAsync) {
        if (typeof isAsync == "undefined") {
            isAsync = true;
        }
        var url = "/analytics/api/view/" + modelViewName + "/expandable-metric-list";

        var callback = function (data) {
            doneFunction(data);
        }

        var request = get(url, "json", callback, isAsync);

        if (!isAsync) {
            data = jQuery.parseJSON(request.responseText);
            return data;
        }
    }

    function getWsNameById(wsId, isAsync) {
        return getEntityNameById("ws", wsId, isAsync);
    }

    function getUserNameById(userId, isAsync) {
        return getEntityNameById("user", userId, isAsync);
    }

    /* for user's id it returns its aliases and for workspace's id it returns its name */
    function getEntityNameById(entity, id, isAsync) {
        if (!id || id.length == 0) {
            var data = {};
            data[entity.toUpperCase()] = id;

            return data;
        }

        if (typeof isAsync == "undefined") {
            isAsync = false;
        }
        var url = "/analytics/api/view/" + entity + "name/" + id;

        var callback = function (data) {
            doneFunction(data);
        }

        var request = get(url, "json", callback, isAsync);

        if (!isAsync) {
            var data = jQuery.parseJSON(request.responseText);
            return data;
        }
    }

    function get(url, responseType, doneCollback, isAsync) {
        var url = url || "";
        var responseType = responseType || "json";
        var doneCollback = doneCollback || function () {
        };

        if (!jQuery.isEmptyObject(params)) {
            url = url + "?" + analytics.util.constructUrlParams(params);
        }

        if (isAsync) {
            currentAjaxRequest = $.ajax({
                url: url,
                dataType: responseType,
                async: isAsync
            })
                .done(function (data) {
                    doneCollback(data);
                })
                .fail(function (data, textStatus, errorThrown) {
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

        for (var t in data) {
            var rows = [];
            var columns = [];

            for (var r in data[t]) {
                if (r == 0) {
                    // create header row
                    for (var c in data[t][r]) {
                        columns.push(data[t][r][c]);
                    }
                } else {
                    var row = [];
                    for (var c in data[t][r]) {
                        row.push(data[t][r][c]);
                    }
                    rows.push(row);
                }
            }

            result[t] = {rows: rows, columns: columns};
        }

        return result;
    };


    function getLinkToExportToCsv(modelName) {
        var url = "/analytics/api/view/" + modelName + ".csv";

        if (!jQuery.isEmptyObject(params)) {
            url = url + "?" + analytics.util.constructUrlParams(params);
        }

        return url;
    }

    function setParams(newParams) {
        params = newParams;
    }

    function getParams() {
        return params;
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

    function pushFailFunction(newFailFunction) {
        failFunctionStack.push(newFailFunction);
    }

    function clearFailFunction() {
        failFunctionStack = new Array();
    }

    // add handler of pressing "Esc" button
    $(document).keydown(function (event) {
        var escKeyCode = 27;
        if (event.which == escKeyCode) {
            if (currentAjaxRequest != null) {
                currentAjaxRequest.abort();
            }
        }
    });

    /** ****************** API ********** */
    return {
        getModelViewData: getModelViewData,
        getMetricValue: getMetricValue,
        getSummarizedMetricValue: getSummarizedMetricValue,
        getExpandedMetricValue: getExpandedMetricValue,
        getExpandableMetricList: getExpandableMetricList,
        getWsNameById: getWsNameById,
        getUserNameById: getUserNameById,
        setParams: setParams,
        getParams: getParams,

        pushDoneFunction: pushDoneFunction,
        popDoneFunction: popDoneFunction,
        clearDoneFunction: clearDoneFunction,
        doneFunction: doneFunction,

        pushFailFunction: pushFailFunction,
        clearFailFunction: clearFailFunction,

        getLinkToExportToCsv: getLinkToExportToCsv,
    }
}