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
                    return jQuery(formTemplate);
                }

                afterEach(function(){
                    if(typeof Account.login.restore !== 'undefined'){
                        Account.login.restore();
                    }
                });

    			it("calls account login upon successful submit",function(done){

                    var v = SignInForm.get(buildForm()),
                        e = "bob@gmail.com", d = "bob", p = "password";

                    sinon.stub(Account,"login", function(email,password,domain,success,error){

                        expect(email).to.equal(e);
                        expect(password).to.equal(p);
                        expect(domain).to.equal(d);

                        done();
                    });

                    $(v.el).find("input[name='email']").val(e);
                    $(v.el).find("input[name='domain']").val(d);
                    $(v.el).find("input[name='password']").val(p);

                    $(v.el).submit();
                });

                it("triggers invalid when Account.login fails", function(done){
                    var v = SignInForm.get(buildForm()),
                        e = "bob@gmail.com", d = "bob", p = "password",
                        eField = "email", eDesc = "Bad email";

                    sinon.stub(Account,"login", function(email,password,domain,success,error){
                        error([
                            new Account.AccountError(eField,eDesc)
                        ]);
                    });

                    v.on("invalid",function(field,message){
                        expect(field).to.equal(eField);
                        expect(message).to.equal(eDesc);
                        done();
                    });

                    $(v.el).find("input[name='email']").val(e);
                    $(v.el).find("input[name='domain']").val(d);
                    $(v.el).find("input[name='password']").val(p);

                    $(v.el).submit();
                });

                // it("triggers success when Account.login succeeds", function(done){

                //     var v = SignInForm.get(buildForm()),
                //         e = "bob@gmail.com", d = "bob", p = "password";

                //     sinon.stub(Account,"login", function(email,password,domain,success,error){
                //         success();
                //     });

                //     v.on("success",function(){
                //         done();
                //     });

                //     $(v.el).find("input[name='email']").val(e);
                //     $(v.el).find("input[name='domain']").val(d);
                //     $(v.el).find("input[name='password']").val(p);

                //     $(v.el).submit();

                // });

                // it("triggers submitting event just before hitting Account.login ", function(done){

                //     var v = SignInForm.get(buildForm()),
                //         e = "bob@gmail.com", d = "bob", p = "password";

                //     sinon.stub(Account,"login", function(email,password,domain,success,error){

                //     });

                //     v.on("submitting",function(){
                //         done();
                //     });

                //     $(v.el).find("input[name='email']").val(e);
                //     $(v.el).find("input[name='domain']").val(d);
                //     $(v.el).find("input[name='password']").val(p);

                //     $(v.el).submit();

                // });

    		});

    	});

    }
);
