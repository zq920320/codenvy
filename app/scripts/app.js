define(["jquery","modernizr","skrollr", "text!templates/movie.html"], function($,Modernizr,skrollr,movieTemplate){


	function initializeSkrollr(){
		var s = skrollr.init({
            beforerender: function(data) {
                //console.log('beforerender');
            },
            render: function() {
                //console.log('render');
            },
            constants: {
                rubicon: 200,
                build : 2000,
                theend : 5000
            },
            easing: {
                WTF: Math.random,
                inverted: function(p) {
                    return 1-p;
                }
            }
        });
	}

	function initializeScrollMeter(){
		window.onscroll = function() {
            function getScrollTop(){
                return window.pageYOffset || document.scrollTop || document.body.scrollTop || 0;
            }
            $(".scroll-meter").html(getScrollTop());
        }
	}


	return {
		run : function(){
			$(document).ready(function(){
				if(Modernizr.mq('only screen and (min-width: 947px)')){
					$(".wrapper").addClass("extended");

					$("body").append(movieTemplate);
					initializeSkrollr();
					initializeScrollMeter();

				}
			});
		}
	};

});
