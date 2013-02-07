define(["jquery","views/signinform", "models/account", "text!templates/signinform.html"],

    function($,SignInForm,Account,formTemplate){

    	describe("Views : SignInForm", function(){

    		describe("module", function(){

    			it("can be imported", function(){
    				expect(SignInForm).to.be.ok;
    			});

    			it("exports get", function(){
    				expect(SignInForm).to.respondTo("get");
    			});

    			it("exports SignInForm", function(){
    				expect(SignInForm).to.respondTo("SignInForm");
    			});
    		});

    		describe("get", function(){

    			it("requires a dom element", function(){
    				var fn = function(){
    					SignInForm.get();
    				};

    				expect(fn).to.throw("Need a form");
    			});

    			it("returns an instance of SignInForm", function(){
    				expect(SignInForm.get(jQuery("<div>"))).to.be.instanceOf(
    					SignInForm.SignInForm
    				);
    			});

    		});

    		describe("SignInForm", function(){

                function buildForm(){
                    $("body").append(jQuery(formTemplate));
                    return $(".login-form");
                }

                afterEach(function(){

                    $(".login-form").remove();

                    if(typeof Account.login.restore !== 'undefined'){
                        Account.login.restore();
                    }
                });

                it("triggers invalid event when email is not provided", function(done){

                    var form = SignInForm.get(buildForm());

                    form.on("invalid",function(field,description){
                        expect(field).to.equal("email");
                        expect(description).to.equal(form.settings.noEmailErrorMessage);
                        done();
                    });

                    $(form.el).valid();

                });

                it("triggers invalid event when email is not valid", function(done){

                    var form = SignInForm.get(buildForm());

                    form.on("invalid", function(field,description){
                        expect(field).to.equal("email");
                        expect(description).to.equal(form.settings.invalidEmailErrorMessage);
                        done();
                    });

                    $(form.el).find("input[name='email']").val("this is not a valid email");

                    $(form.el).valid();
                });

                // it("triggers invalid event when domain is not provided", function(done){

                //     var form = SignInForm.get(buildForm());

                //     form.on("invalid", function(field,description){
                //         expect(field).to.equal("domain");
                //         expect(description).to.equal(form.settings.noDomainErrorMessage);
                //         done();
                //     });

                //     $(form.el).find("input[name='email']").val("bob@gmail.com");

                //     $(form.el).valid();

                // });

                // it("triggers invalid event when domain is not valid", function(done){

                //     var form = SignInForm.get(buildForm());

                //     form.on("invalid", function(field,description){
                //         expect(field).to.equal("domain");
                //         expect(description).to.equal(form.settings.invalidDomainNameErrorMessage);
                //         done();
                //     });

                //     $(form.el).find("input[name='email']").val("bob@gmail.com");
                //     $(form.el).find("input[name='domain']").val("12--thisisnotavaliddomain");

                //     $(form.el).valid();
                // });

                it("triggers invalid event when password is not provided", function(done){
                    var form = SignInForm.get(buildForm());

                    form.on("invalid", function(field,description){
                        expect(field).to.equal("password");
                        expect(description).to.equal(form.settings.noPasswordErrorMessage);
                        done();
                    });

                    $(form.el).find("input[name='email']").val("bob@gmail.com");
                    $(form.el).find("input[name='domain']").val("bob");

                    $(form.el).valid();
                });

    			it("calls account login upon successful submit",function(done){

                    var v = SignInForm.get(buildForm()),
                        e = "bob@gmail.com", p = "password";

                    sinon.stub(Account,"login", function(email,password,success,error){

                        expect(email).to.equal(e);
                        expect(password).to.equal(p);

                        done();
                    });

                    $(v.el).find("input[name='email']").val(e);
                    $(v.el).find("input[name='password']").val(p);

                    $(v.el).submit();
                });
    		});

    	});

    }
);
