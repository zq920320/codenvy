define(["jquery"], function($){

	return {
		run : function(){
			$(document).ready(function(){
                $(".signup-form > input, a").on("focus", function(){
                    $(".signup-form > input[type=submit]").show();
                    $(".signup-form > .domain-name").removeClass("hidden-text-box");
                    $(".aternative-login").addClass("collapsed");
                });

                $(".signup-form > input, a").on("blur", function(){
                    $(".signup-form > input[type=submit]").hide();
                    $(".signup-form > .domain-name").addClass("hidden-text-box");
                    $(".aternative-login").removeClass("collapsed");
                });
			});
		}
	};

});
