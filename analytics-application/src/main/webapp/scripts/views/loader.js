if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.views = analytics.views || {};
analytics.views.loader = new Loader();

function Loader() {
    /**
	 * Loader
	 */
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
	   
	   // add handler of pressing "Esc" button
	   jQuery(document).keydown(function(event) {
	      var escKeyCode = 27;
	      if (event.which == escKeyCode) {
	         hide();
	      }
	   });
	}
	
	
	function show() {
	   loader.show();
	}
	
	function hide() {
	   loader.hide();
	}
	
    /** ****************** API ********** */
    return {
        show: show,
        hide: hide
    }
}