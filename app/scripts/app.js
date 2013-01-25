define(["jquery"], function($){

	return {
		run : function(){
			$(document).ready(function(){
                function adjustFormDisplay(){
                    if($(".field > input").toArray().indexOf(document.activeElement) !== -1){
                        $(".field > .signup-button").removeClass("hidden-button");
                        $(".field > .domain-name").removeClass("hidden-text-box");
                        $(".aternative-login").addClass("collapsed");
                    } else {
                        $(".field > .signup-button").addClass("hidden-button");
                        $(".field > .domain-name").addClass("hidden-text-box");
                        $(".aternative-login").removeClass("collapsed");
                    }
                }

                $(".field > input").on("focus", function(){
                    adjustFormDisplay();
                });

                $(".field > input").on("blur", function(){
                    adjustFormDisplay();
                });
			});
		}
	};

});
