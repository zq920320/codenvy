jQuery(function () {
	"use strict";
    
    /*global jQuery, $*/  
	jQuery(document).ready(function(){
		
		jQuery("#signUpForm").validate({
			rules: {
				workspace: {
					required: true,
					minlength: 2
				},
				emailid: {
					required: true,
					email: true
				},
			},
			messages: {
				workspace: "Please enter workspace",
				emailid: "Please enter a valid email",
			}
		});
		
		jQuery("#loginForm").validate({
			rules: {
				workspace: {
					required: true,
					minlength: 2
				},
				emailid: {
					required: true,
					email: true
				},
			},
			messages: {
				workspace: "Please enter workspace",
				emailid: "Please enter a valid email",
			}
		});
		
		jQuery("#forgotForm").validate({
			rules: {
				emailid: {
					required: true,
					email: true
				},
			},
			messages: {
				emailid: "Please enter a valid email",
			}
		});
		
	});
}());