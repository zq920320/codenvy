define(["jquery", "views/forgotpasswordform", "models/account", "text!templates/forgotpasswordform.html"],

	function($,ForgotPasswordForm,Account,formTemplate){

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
					$("body").append(jQuery(formTemplate));
					return $(".forgotpassword-form");
				}

				afterEach(function(){
					$(".forgotpassword-form").remove();

					if(typeof Account.recoverPassword.restore !== 'undefined'){
						Account.recoverPassword.restore();
					}
				});

                it("supports showMessage method", function(){

                    expect(ForgotPasswordForm.ForgotPasswordForm)
                        .to.respondTo("showMessage");

                });

                describe("showMessage", function(){
                    function buildForm(){
                        $("body").append(jQuery(formTemplate));
                        return $(".forgotpassword-form");
                    }

                    afterEach(function(){
                        $(".forgotpassword-form").remove();

                        if(typeof Account.recoverPassword.restore !== 'undefined'){
                            Account.recoverPassword.restore();
                        }
                    });

                    it("requires message parameter", function(){

                        var form = ForgotPasswordForm.get(buildForm());

                        var fn = function(){
                            form.showMessage();
                        };

                        expect(fn).to.throw("Need a message");

                    });

                    it("hides input elements on the form and shows a message", function(){
                        var form = ForgotPasswordForm.get(buildForm()),
                            message = "this is the message";

                        form.showMessage(message);

                        expect(
                            $(form.el).find(".data").hasClass("hidden")
                        ).to.be.true;

                        expect(
                            $(form.el).find(".result-message").hasClass("hidden")
                        ).to.be.false;

                        expect(
                            $(form.el).find(".result-message > p").html()
                        ).to.equal(message);

                    });

                });

				it("triggers invalid event if no email is provided", function(done){

					var form = ForgotPasswordForm.get(buildForm());

					form.on("invalid",function(field,description){
						expect(field).to.equal("email");
						expect(description).to.equal(form.settings.noEmailErrorMessage);
						done();
					});

					$(form.el).valid();

				});

				it("triggers invalid event if email is not valid", function(done){

					var form = ForgotPasswordForm.get(buildForm());

					form.on("invalid", function(field, description){
						expect(field).to.equal("email");
						expect(description).to.equal(form.settings.invalidEmailErrorMessage);
						done();
					});

					$(form.el).find("input[name='email']").val("this is not a valid email");
					$(form.el).valid();
				});

				it("triggers submitting event before submitting data", function(done){

					var form = ForgotPasswordForm.get(buildForm());

					form.on("submitting", function(){
						done();
					});

					$(form.el).find("input[name='email']").val("bob@gmail.com");
					$(form.el).submit();

				});

				it("call Account.recoverPassword if email is valid", function(done){

					var form = ForgotPasswordForm.get(buildForm()), e = "bob@gmail.com";

					sinon.stub(Account,"recoverPassword",function(email){
						expect(email).to.equal(e);
						done();
					});

					$(form.el).find("input[name='email']").val(e);
					$(form.el).submit();

				});

				it("triggers success event if Account.recoverPassword succeeds", function(done){

					var form = ForgotPasswordForm.get(buildForm()), e = "bob@gmail.com", d = { data : "u"};

					sinon.stub(Account,"recoverPassword",function(email,success,error){
						success(d);
					});

					form.on("success", function(data){
						expect(data).to.eql(d);
						done();
					});

					$(form.el).find("input[name='email']").val(e);
					$(form.el).submit();

				});

				it("triggers invalid event if Account.recoverPassword fails", function(done){
					var form = ForgotPasswordForm.get(buildForm()), e = "bob@gmail.com",
						eField = "email", eDesc = "bad email";

					sinon.stub(Account,"recoverPassword",function(email,success,error){
						error([
							new Account.AccountError(eField,eDesc)
						]);
					});

					form.on("invalid", function(field,message){
						expect(field).to.equal(eField);
						expect(message).to.equal(eDesc);
						done();
					});

					$(form.el).find("input[name='email']").val(e);
					$(form.el).submit();
				});

			});

		});

	}
);
