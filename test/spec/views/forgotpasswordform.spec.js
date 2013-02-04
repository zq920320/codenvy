define(["jquery", "views/forgotpasswordform", "text!templates/forgotpasswordform.html"],

	function($,ForgotPasswordForm,formTemplate){

		describe("Views : ForgotPasswordForm", function(){

			describe("module", function(){
				it("can be imported", function(){
					expect(ForgotPasswordForm).to.be.ok;
				});

				it("exports get", function(){
					expect(ForgotPasswordForm).to.respondTo("get");
				});

				it("exports ForgotPasswordForm", function(){
					expect(ForgotPasswordForm).to.respondTo("ForgotPasswordForm");
				});
			});

			describe("get", function(){

				it("requires a form dom", function(){

					var fn = function(){
						ForgotPasswordForm.get();
					};

					expect(fn).to.throw("Need a form");

				});

				it("returns an instance of ForgotPasswordForm", function(){
					expect(ForgotPasswordForm.get(jQuery("<form>"))).to.be.instanceOf(
						ForgotPasswordForm.ForgotPasswordForm
					);
				});

			});

			describe("ForgotPasswordForm", function(){
				function buildForm(){
					return jQuery(formTemplate);
				}
			});

		});

	}
);
