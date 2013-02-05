define(["jquery", "views/resetpasswordform", "models/account", "text!templates/resetpasswordform.html"],

    function($,ResetPasswordForm,Account,formTemplate){

        describe('Views : ResetPasswordForm', function () {
            describe("module",function(){

                it("can be imported", function(){
                    expect(ResetPasswordForm).to.be.ok;
                });

                it("exports get", function(){
                    expect(ResetPasswordForm).to.respondTo("get");
                });

                it("exports ResetPasswordForm", function(){
                    expect(ResetPasswordForm).to.respondTo("ResetPasswordForm");
                });

            });

            describe("get", function(){

                var confirmSetupPasswordStub;

                beforeEach(function(){
                    confirmSetupPasswordStub = sinon.stub(
                        Account,"confirmSetupPassword",
                        function(){

                        }
                    );
                });

                afterEach(function(){
                    if(confirmSetupPasswordStub){
                        confirmSetupPasswordStub.restore();
                    }
                });

                it("requires a dom element", function(){
                    var fn = function(){
                        ResetPasswordForm.get();
                    };

                    expect(fn).to.throw("Need a form");
                });

                it("returns an instance of ResetPasswordForm", function(){
                    expect(ResetPasswordForm.get(jQuery("<form>"))).to.be.instanceOf(
                        ResetPasswordForm.ResetPasswordForm
                    );
                });

            });

            describe("ResetPasswordForm", function(){

                var confirmSetupPasswordStub;

                function buildForm(){
                    $("body").append(jQuery(formTemplate));
                    return $(".resetpassword-form");
                }

                beforeEach(function(){
                    confirmSetupPasswordStub = sinon.stub(
                        Account,"confirmSetupPassword",
                        function(){

                        }
                    );
                });

                afterEach(function(){
                    $(".resetpassword-form").remove();

                    if(typeof Account.setupPassword.restore !== 'undefined'){
                        Account.setupPassword.restore();
                    }

                    if(confirmSetupPasswordStub){
                        confirmSetupPasswordStub.restore();
                    }
                });

                it("supports resolveUserEmail method", function(){

                    expect(ResetPasswordForm.ResetPasswordForm)
                        .to.respondTo("resolveUserEmail");

                });

                it("triggers invalid event if password is not provided", function(done){
                    var form = ResetPasswordForm.get(buildForm());

                    form.on("invalid", function(field,message){
                        expect(field).to.equal("password");
                        expect(message).to.equal(form.settings.noPasswordErrorMessage);
                        done();
                    });

                    $(form.el).valid();
                });

                it("triggers invalid event if password2 is not provided", function(done){
                    var form = ResetPasswordForm.get(buildForm());

                    form.on("invalid", function(field,message){
                        expect(field).to.equal("password2");
                        expect(message).to.equal(form.settings.noConfirmPasswordErrorMessage);
                        done();
                    });

                    $(form.el).find("input[name='password']").val("password");

                    $(form.el).valid();
                });

                it("triggers invalid event if two passwords don't match", function(done){

                    var form = ResetPasswordForm.get(buildForm());

                    form.on("invalid", function(field,message){
                        expect(field).to.equal("password2");
                        expect(message).to.equal(form.settings.noConfirmPasswordErrorMessage);
                        done();
                    });

                    $(form.el).find("input[name='password']").val("password");
                    $(form.el).find("input[name='password2']").val("password2");

                    $(form.el).valid();

                });

                it("triggers submitting event before submit", function(done){

                    var form = ResetPasswordForm.get(buildForm()), password = "password";

                    form.on("submitting", function(){
                        done();
                    });

                    $(form.el).find("input[name='password']").val(password);
                    $(form.el).find("input[name='password2']").val(password);

                    $(form.el).submit();

                });

                it("hits Account setupPassword on submit", function(done){

                    var form = ResetPasswordForm.get(buildForm()), p = "password";

                    sinon.stub(Account,"setupPassword",function(password,success,error){
                        expect(password).to.equal(p);
                        done();
                    });

                    $(form.el).find("input[name='password']").val(p);
                    $(form.el).find("input[name='password2']").val(p);

                    $(form.el).submit();

                });

                it("triggers invalid event if Account setupPassword fails", function(done){

                    var form = ResetPasswordForm.get(buildForm()),
                        p = "password", errorMessage = "something went wrong";

                    sinon.stub(Account,"setupPassword",function(password,success,error){
                        error([
                            new Account.AccountError(null,errorMessage)
                        ]);
                    });

                    form.on("invalid", function(field,message){
                        expect(message).to.equal(errorMessage);
                        done();
                    });

                    $(form.el).find("input[name='password']").val(p);
                    $(form.el).find("input[name='password2']").val(p);

                    $(form.el).submit();

                });

                it("triggers success event if Account setupPassword succeeds", function(done){

                    var form = ResetPasswordForm.get(buildForm()),
                        p = "password", d = { message : "Done" };

                    sinon.stub(Account,"setupPassword",function(password,success,error){
                        success(d);
                    });

                    form.on("success", function(data){
                        expect(data).to.eql(d);
                        done();
                    });

                    $(form.el).find("input[name='password']").val(p);
                    $(form.el).find("input[name='password2']").val(p);

                    $(form.el).submit();

                });

                it("hits Account confirmSetupPassword",function(done){

                    //clean the previous stub
                    if(confirmSetupPasswordStub){
                        confirmSetupPasswordStub.restore();
                    }

                    confirmSetupPasswordStub = sinon.stub(Account,"confirmSetupPassword",function(success,error){
                        done();
                    });

                    var form = ResetPasswordForm.get(buildForm());
                    form.resolveUserEmail();
                });

                it("sets email from Account confirmSetupPassword",function(done){

                    var email = "bob@gmail.com";

                    //clean the previous stub
                    if(confirmSetupPasswordStub){
                        confirmSetupPasswordStub.restore();
                    }

                    confirmSetupPasswordStub = sinon.stub(Account,"confirmSetupPassword",function(success,error){
                        success({email:email});
                    });

                    var form = ResetPasswordForm.get(buildForm());
                    form.resolveUserEmail();

                    setTimeout(function(){
                        expect($(form.el).find(".email").html()).to.equal(email);
                        done();
                    },100);

                });

                it("triggers invalid if Account confirmSetupPassword fails", function(done){

                    var errorMessage = "something is not right";

                    //clean the previous stub
                    if(confirmSetupPasswordStub){
                        confirmSetupPasswordStub.restore();
                    }

                    confirmSetupPasswordStub = sinon.stub(Account,"confirmSetupPassword",function(success,error){
                        error([
                            new Account.AccountError(null,errorMessage)
                        ]);
                    });

                    var form = ResetPasswordForm.get(buildForm());

                    form.on("invalid", function(field,message){
                        expect(message).to.equal(errorMessage);
                        done();
                    });

                    form.resolveUserEmail();

                });

            });

        });
    }
);
