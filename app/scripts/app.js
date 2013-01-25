define(["jquery"], function($){

	return {
		run : function(){
			$(document).ready(function(){
                function adjustFormDisplay(){
                    if($(".signup-form > input").toArray().indexOf(document.activeElement) !== -1){
                        $(".signup-form > .signup-button").removeClass("hidden-button");
                        $(".signup-form > .domain-name").removeClass("hidden-text-box");
                        $(".aternative-login").addClass("collapsed");
                    } else {
                        $(".signup-form > .signup-button").addClass("hidden-button");
                        $(".signup-form > .domain-name").addClass("hidden-text-box");
                        $(".aternative-login").removeClass("collapsed");
                    }
                }

                $(".signup-form > input").on("focus", function(){
                    adjustFormDisplay();
                });

                $(".signup-form > input").on("blur", function(){
                    adjustFormDisplay();
                });
			});
		}
	};

});
