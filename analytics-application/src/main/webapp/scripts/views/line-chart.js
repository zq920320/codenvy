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

analytics.views = analytics.views || {};
analytics.views.lineChart = new LineChart();

function LineChart() {
    /**
	 * Management of C3.js line chart
	 * @see http://c3js.org/gettingstarted.html
     */
    function display(columnsJson, columnLabelsJson, containerId) {
        var columns = JSON.parse(columnsJson);
        var normalizedColumns = analytics.util.normalizeNumericValues(columns);
        
        var columnLabels = JSON.parse(columnLabelsJson);
        
        var chart = c3.generate({
            bindto: '#' + containerId,
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
    }

	
    /** ****************** API ********** */
    return {
        display: display,
    }
}