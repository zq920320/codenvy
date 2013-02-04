define(["jquery","views/signupform","models/account","text!templates/signupform.html"],

    function($,SignUpForm,Account,signupFormTemplate){

    	describe("Views : SignUpForm",function(){

    		describe("module", function(){

    			it("can be imported", function(){
    				expect(SignUpForm).to.be.ok;
    			});

    			it("exports get method", function(){
    				expect(SignUpForm).to.respondTo('get');
    			});

    		});

    		describe("get", function(){

    			it("requires a form", function(){

    				var fn = function(){
    					SignUpForm.get();
    				};

    				expect(fn).to.throw("Need a form");

    			});

    			it("returns a SignUpForm view with el set to form", function(){
    				var form = jQuery("<form>"), v = SignUpForm.get(form);
    				expect(v).to.be.ok;
    				expect(v.el).to.equal(form[0]);
    			});

    		});

            describe("SignUpForm", function(){

                afterEach(function(){
                    if(typeof Account.isValidDomain.restore !== 'undefined'){
                        Account.isValidDomain.restore();
                    }

                    if(typeof Account.createTenant.restore !== 'undefined'){
                        Account.createTenant.restore();
                    }
                });

                function buildForm(){
                    return jQuery(signupFormTemplate);
                }

                it("sets validator property", function(){
                    var formDOM = $(jQuery("<form>")),
                        form = SignUpForm.get(formDOM);

                    expect(form.validator).to.be.ok;
                });


                it("raises invalid event if email is not specified", function(done){

                    var form = SignUpForm.get(buildForm());


                    form.on("invalid", function(errorField, errorText){
                        expect(errorField).to.equal("email");
                        expect(errorText).to.equal(form.settings.noEmailErrorMessage);

                        done();
                    });

                    form.__showErrors({email: form.settings.noEmailErrorMessage});
                });

                it("raises invalid event if email is not valid", function(done){
                    var form = SignUpForm.get(buildForm());


                    form.on("invalid", function(errorField, errorText){
                        expect(errorField).to.equal("email");
                        expect(errorText).to.equal(form.settings.invalidEmailErrorMessage);
                        done();
                    });

                    form.__showErrors({email: form.settings.invalidEmailErrorMessage});
                });

                it("raises invalid event if domain is not speicified", function(done){
                    var form = SignUpForm.get(buildForm());


                    form.on("invalid", function(errorField, errorText){
                        expect(errorField).to.equal("domain");
                        expect(errorText).to.equal(form.settings.noDomainErrorMessage);
                        done();
                    });

                    form.__showErrors({domain: form.settings.noDomainErrorMessage});
                });

                it("raises invalid event if domain is not valid", function(done){
                    var form = SignUpForm.get(buildForm());


                    form.on("invalid", function(errorField, errorText){
                        expect(errorField).to.equal("domain");
                        expect(errorText).to.equal(form.settings.invalidDomainNameErrorMessage);
                        done();
                    });

                    form.__showErrors({domain: form.settings.invalidDomainNameErrorMessage});
                });

                it("triggers submitting event before submitting the form", function(done){

                    var e = 'bob@gmail.com', d = 'bob';

                    sinon.stub(Account,"createTenant",function(email,domain,success,error){
                    });

                    var form = SignUpForm.get(buildForm());

                    $(form.el).find("input[name='email']").val(e);
                    $(form.el).find("input[name='domain']").val(d);

                    form.on("submitting", function(){
                        done();
                    });

                    $(form.el).submit();

                });

                it("calls Account.createTenant when the form is valid", function(done){

                    var e = 'bob@gmail.com', d = 'bob';

                    sinon.stub(Account,"createTenant",function(email,domain){
                        expect(email).to.equal(e);
                        expect(domain).to.equal(d);
                        done();
                    });

                    var form = SignUpForm.get(buildForm());

                    $(form.el).find("input[name='email']").val(e);
                    $(form.el).find("input[name='domain']").val(d);

                    $(form.el).submit();
                });

                it("raises invalid event when Account.createTenant returns errors", function(done){
                    var e = 'bob@gmail.com', d = 'bob',
                        efield = "email", edesc = "Already in use";

                    sinon.stub(Account,"createTenant",function(email,domain,success,error){
                        error([
                            new Account.AccountError(efield,edesc)
                        ]);
                    });

                    var form = SignUpForm.get(buildForm());

                    $(form.el).find("input[name='email']").val(e);
                    $(form.el).find("input[name='domain']").val(d);

                    form.on("invalid", function(field,message){
                        expect(field).to.equal(efield);
                        expect(message).to.equal(edesc);
                        done();
                    });

                    $(form.el).submit();
                });

                it("raises success event when Account.createTenant succeeds", function(done){
                    var e = 'bob@gmail.com', d = 'bob',
                        data = { redirectTo : "/" };

                    sinon.stub(Account,"createTenant",function(email,domain,success,error){
                        success(data);
                    });

                    var form = SignUpForm.get(buildForm());

                    $(form.el).find("input[name='email']").val(e);
                    $(form.el).find("input[name='domain']").val(d);

                    form.on("success", function(d){
                        expect(d).to.eql(data);
                        done();
                    });

                    $(form.el).submit();
                });

                // it("validates domain name using Account.isValidDomain", function(done){

                //     sinon.stub(Account,"isValidDomain", function(name){
                //         done();
                //     });

                //     var form = SignUpForm.get(buildForm());
                //     $(form.el).find("input[name='email']").val("bob@gmail.com")
                //     $(form.el).submit();
                // });

            });

    	});

    }
);
