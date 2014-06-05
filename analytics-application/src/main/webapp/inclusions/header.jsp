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

<script type="text/javascript" src="/analytics/third-party/jquery-1.7.2.min.js"></script>
<script type="text/javascript" src="/analytics/bootstrap/js/bootstrap.js"></script>

<link rel="stylesheet" href="/analytics/third-party/jquery-ui-1.8.20/themes/base/minified/jquery-ui.min.css">
<script type="text/javascript"
        src="/analytics/third-party/jquery-ui-1.8.20/ui/minified/jquery-ui.min.js"></script>

<!-- DataTable plugin -->
<script type="text/javascript" src="/analytics/third-party/jquery.dataTables-1.9.4.min.js"></script>
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
    
    /** 
     * Setup DataTables to sort cells with numbers with comma separators like "1,200,100", numbers enclosed in HTML, and persentages.
     * @see http://stackoverflow.com/questions/20336091/sort-numbers-with-the-format-with-datatables-js
     * http://datatables.net/plug-ins/sorting#numbers_html 
    
	* Patterns to detect and extruct numbers:
	/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/.test("<a href='test' class='link'>63</a>")    -> true
	"<a href='test' class='link'>63</a>".match(/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/)   -> 63
	
	/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/.test("<a href='test' class='link'>63.01</a>")  -> true
	"<a href='test' class='link'>63.01</a>".match(/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/)   -> 63.01
	
	/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/.test("<a href='test' class='link'>6,3</a>")  -> true
	"<a href='test' class='link'>6,3</a>".match(/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/)   -> 6,3
	
	/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/.test("<a href='test' class='link'>63%</a>")  -> true
	"<a href='test' class='link'>63%</a>".match(/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/)   -> 63
	
	/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/.test("<a href='test' class='link'>test01</a>")  -> false
	"<a href='test' class='link'>test01</a>".match(/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/)   -> null
	
	/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/.test("<a href='test' class='link'>00:06</a>")  -> false
	"<a href='test' class='link'>00:06</a>".match(/^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/)   -> null
	
	/^(([0-9,.+\-]+)%?)$/.test("63")      -> true
	"63".match(/^(([0-9,.+\-]+)%?)$/)     -> 63
	
	/^(([0-9,.+\-]+)%?)$/.test("63.01")      -> true
	"63.01".match(/^(([0-9,.+\-]+)%?)$/)     -> 63.01
	
	/^(([0-9,.+\-]+)%?)$/.test("6,3")      -> true
	"6,3".match(/^(([0-9,.+\-]+)%?)$/)     -> 6.3
	
	/^(([0-9,.+\-]+)%?)$/.test("63%")      -> true
	"63%".match(/^(([0-9,.+\-]+)%?)$/)     -> 63
	
	/^(([0-9,.+\-]+)%?)$/.test("63")      -> true
	"63".match(/^(([0-9,.+\-]+)%?)$/)     -> 63
	
	/^(([0-9,.+\-]+)%?)$/.test("test01")      -> false
	"test01".match(/^(([0-9,.+\-]+)%?)$/)     -> null
	
	/^(([0-9,.+\-]+)%?)$/.test("06:03")      -> false
	"06:03".match(/^(([0-9,.+\-]+)%?)$/)     -> null
	
    */    
    jQuery(function() {
        // check on numbers with ","  and "%"
        var numberPattern = /^(([0-9,.+\-]+)%?)$/;
        
        // check on numbers with ","  and "%" enclosed in the html tags
        var numberWithinHtmlPattern = /^<[^>]*>(([0-9,.+\-]+)%?)<\/[^>]*>$/;
       
        // check on time number with ":" delimeters in format "NN{2,}:NN{2}:NN{2}";
        var timePattern = /^[0-9]{2,}:[0-9]{2}:[0-9]{2}$/;

        // check on time in the html tags
        var timeWithinHtmlPattern = /^<[^>]*>([0-9]{2,}:[0-9]{2}:[0-9]{2})<\/[^>]*>$/;
        
        
        /** Detect number.
           @see original method of DataTable library v.1.9.4 here: 
            http://datatables.net/download/build/jquery.dataTables.js : $.extend( DataTable.ext.aTypes, ...        
        */
        jQuery.fn.dataTableExt.aTypes.unshift(function(sData) {            
            // check on valid number
            if (numberPattern.test(sData)) {
                return "numeric";   // return pre-defined data type of numbers

            // check on valid number enclosed in HTML tags
            } else if (numberWithinHtmlPattern.test(sData)) {
                return "numeric";   // return pre-defined data type of numbers
            
            // check on valid time
            } else if (timePattern.test(sData)) {
                return "numeric";   // return pre-defined data type of numbers

            // check on time enclosed in HTML tags
            } else if (timeWithinHtmlPattern.test(sData)) {
                return "numeric";   // return pre-defined data type of numbers
            }
            
            return null;
        });        
        
        /** Redefine method of DataTable library to extract and clear the number.
            @see original method "numeric-pre" of DataTable library v.1.9.4 here: 
             http://datatables.net/download/build/jquery.dataTables.js
        */
        jQuery.fn.dataTableExt.oSort['numeric-pre'] = function(a) {            
            var x = String(a);
            var matches = a.match(numberPattern);
            if (matches != null && matches.length > 0) {
                x = matches[matches.length - 1];
                
            } else {
                matches = a.match(numberWithinHtmlPattern);
                if (matches != null && matches.length > 0) {
                    x = matches[matches.length - 1];
                    
                } else {
                    matches = a.match(timeWithinHtmlPattern);
                    if (matches != null && matches.length > 0) {
                        x = matches[matches.length - 1];
                    }
                }
            }
            
            x = x.replace(",", "");          // remove "," in numbers like "2,123"

            x = x.replace(/:/g,"")      // remove ":"
                 .replace(/^0+/g,"");   // remove starting 0s in time string like "000343:45:56"
            
            return (x=="-" || x==="") ? 0 : x*1;   // original code 
        };
    });
</script>
<style type="text/css">
	/* don't display message about empty table in DateTable  */
	.dataTables_empty {
	    display: none;
	}
</style>

<style type="text/css">
	/* redefine style of Accordion Widget of jQuery UI (@see http://api.jqueryui.com/accordion/#method-disable ) */
	.ui-accordion {
	    width: 400px;
	}

	.ui-corner-all, 
	.ui-corner-bottom, 
	.ui-corner-right,
	.ui-corner-left, 
	.ui-corner-top	{
	    border-radius: 0px !important;
	}
		
	.ui-accordion .ui-accordion-header {
        border: 1px solid #ddd !important;
	    background-color: #afafaf;
	    background-image: -ms-linear-gradient(top, #fff, #AFAFAF);
	    background-image: -webkit-gradient(linear, 0 0, 0 100%, from(#fff), to(#AFAFAF));
	    background-image: -webkit-linear-gradient(top, #fff, #AFAFAF);
	    background-image: -o-linear-gradient(top, #fff, #AFAFAF);
	    background-image: linear-gradient(top, #fff, #AFAFAF);
	    background-image: -moz-linear-gradient(top, #fff, #AFAFAF);
	    outline: none;

        color: #363636;
        text-transform: uppercase;
        font-size: 13px;
	}

	.ui-accordion .ui-state-hover {
	    outline: 0;
	    -webkit-box-shadow: inset 0 3px 5px rgba(0,0,0,.125);
	    box-shadow: inset 0 3px 5px rgba(0,0,0,.125);
	}
	
	.ui-accordion .ui-accordion-header .ui-icon {
        left: -17px !important;
        background-image: url('/analytics/third-party/jquery-ui-1.8.20/themes/base/minified/images/ui-icons_454545_256x240.png') !important;
    }
		
	.ui-accordion .ui-accordion-content {
	    padding: 10px !important;
	}
	
	.ui-widget-content {
        border: 1px solid #ddd !important;
    }
</style>

<!-- load C3.js line chat plugin (http://c3js.org/) -->
<link rel="stylesheet" type="text/css" href="/analytics/third-party/c3/c3.css" />
<script type="text/javascript" src="/analytics/third-party/c3/d3.v3.min.js" charset="utf-8"></script>
<script type="text/javascript" src="/analytics/third-party/c3/c3.v0.1.41.min.js"></script>
<style type="text/css">
	.c3 text {
	    font-size: 12px;
	    fill: #888888;
	}
	
	.c3-axis-x path,
	.c3-axis-x line,
	.c3-axis-y path,
	.c3-axis-y line {
	    stroke: #888888;
	}
	
	.c3-axis-x {
	    text-transform: uppercase;
	}
</style>