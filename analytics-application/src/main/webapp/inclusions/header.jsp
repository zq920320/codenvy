<%-- 
 CODENVY CONFIDENTIAL
 ________________

 [2012] - [2014] Codenvy, S.A.
 All Rights Reserved.
 NOTICE: All information contained herein is, and remains
 the property of Codenvy S.A. and its suppliers,
 if any. The intellectual and technical concepts contained
 herein are proprietary to Codenvy S.A.
 and its suppliers and may be covered by U.S. and Foreign Patents,
 patents in process, and are protected by trade secret or copyright law.
 Dissemination of this information or reproduction of this material
 is strictly forbidden unless prior written permission is obtained
 from Codenvy S.A.. 
--%>

<link rel="stylesheet" type="text/css" href="/analytics/bootstrap/css/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/analytics/bootstrap/css/bootstrap-responsive.min.css"/>
<link rel="stylesheet" type="text/css" href="/analytics/css/styles.css"/>
<link rel="stylesheet" type="text/css" href="/analytics/css/single-column.css"/>
<link rel="stylesheet" type="text/css" href="/analytics/css/view.css"/>
<link rel="stylesheet" type="text/css" href="/analytics/css/database-table.css" />

<style type="text/css">
    body {
        padding-top: 60px;
    }

    .sidebar-nav {
        padding: 9px 0;
    }
</style>

<script type="text/javascript" src="/analytics/scripts/third-party/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="/analytics/bootstrap/js/bootstrap.js"></script>

<link rel="stylesheet" href="/analytics/scripts/third-party/jquery-ui-1.8.20/themes/base/minified/jquery-ui.min.css">
<script type="text/javascript"
        src="/analytics/scripts/third-party/jquery-ui-1.8.20/ui/minified/jquery-ui.min.js"></script>

<!-- DataTable plugin -->
<script type="text/javascript" src="/analytics/scripts/third-party/jquery.dataTables-1.9.4.min.js"></script>
<script type="text/javascript" src="dataTables.numericCommaSort.js"></script>
<script type="text/javascript" src="dataTables.numericCommaTypeDetect.js"></script>
<script>
	// setup default settings
	jQuery.extend(true, jQuery.fn.dataTable.defaults, {
	       "bPaginate": false,
	       "bLengthChange": false,
	       "bFilter": false,
	       "bSort": true,
	       "bInfo": false,
	       "bAutoWidth": false,
	       "oLanguage": {
	           "sZeroRecords": "",  // don't display message about empty table in DateTable 
	           "sEmptyTable": "",   // don't display message about empty table in DateTable
//	           "sEmptyTable": "<div class='system'>empty table</div>"
	       },
	});

    // setup own classes for table headers
    jQuery(function() {
		jQuery.extend(jQuery.fn.dataTableExt.oStdClasses, {
		    "sSortAsc": "ascending",
		    "sSortDesc": "descending",
		    "sSortable": "unsorted",
		});
    });
    
    // setup DataTables to sort cells with numeric with comma separators like "1,200,100"
    // see explanation here http://stackoverflow.com/questions/20336091/sort-numbers-with-the-format-with-datatables-js
    jQuery(function() {
        jQuery.fn.dataTableExt.oSort['numeric-comma-asc'] = function(a, b) {
            //remove the (,) from the string
            var x = (a == "-") ? 0 : a.replace(/,/g, "");
            var y = (b == "-") ? 0 : b.replace(/,/g, "")
            x = parseFloat(x);
            y = parseFloat(y);
            return ((x < y) ? -1 : ((x > y) ? 1 : 0));
        };

        jQuery.fn.dataTableExt.oSort['numeric-comma-desc'] = function(a, b) {
            var x = (a == "-") ? 0 : a.replace(/,/g, "");
            var y = (b == "-") ? 0 : b.replace(/,/g, "")
            x = parseFloat(x);
            y = parseFloat(y);
            return ((x < y) ? 1 : ((x > y) ? -1 : 0));
        };

        //numeric comma autodetect
        jQuery.fn.dataTableExt.aTypes.unshift(function(sData) {
            //include the dot in the sValidChars string (don't place it in the last position)
            var sValidChars = "0123456789-.,";
            var Char;
            var bDecimal = false;

            /* Check the numeric part */
            for (i = 0; i < sData.length; i++) {
                Char = sData.charAt(i);
                if (sValidChars.indexOf(Char) == -1) {
                    return null;
                }

                /* Only allowed one decimal place... */
                if (Char == ",") {
                    if (bDecimal) {
                        return null;
                    }
                    bDecimal = true;
                }
            }

            return 'numeric-comma';
        });
    });
</script>
<style type="text/css">
	/* don't display message about empty table in DateTable  */
	.dataTables_empty {
	    display: none;
	}
</style>