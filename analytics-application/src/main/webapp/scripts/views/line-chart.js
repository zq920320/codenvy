/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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

analytics.views = analytics.views || {};
analytics.views.lineChart = new AnalyticsLineChart();

function AnalyticsLineChart() {
    
    var charts = [];
    var isChartDisplayed = false;
    
    /**
	 * Management of C3.js line chart
	 * @see http://c3js.org/gettingstarted.html
     */
    function display(columns, columnLabels, chartId) {
        var normalizedColumns = analytics.util.normalizeTableNumbers(columns);
        
        var chart = c3.generate({
            bindto: '#' + chartId,
            data: {
              columns: normalizedColumns
            },
            
            legend: {
                position: 'right'
            },
            
            axis: {
                x: {
                    type: 'categorized', // this needed to load string values for x-axis
                    categories: columnLabels
                }
            }
        });
        
        // workaround to fix bug with displaying chart by c3js plugin
        setTimeout(function() {
            chart.hide();
            chart.show();
        }, 0);
    }

    function displayAll() {
        if (isChartDisplayed == true) {
            return;
        }
        
        for (var chartIndex in charts) {
            var chart = charts[chartIndex];
            display(chart.columns,
                    chart.columnLabels,
                    chart.chartId);
        }
        
        isChartDisplayed = true;
    }
    
    /** Save data to display chart later */
    function push(chart) {
        charts.push(chart);
    }
    
    function init() {
        charts = [];
        isChartDisplayed = false;
    }
    
    
    /** ****************** API ********** */
    return {
        push: push,
        displayAll: displayAll,
        init: init,
    }
}
