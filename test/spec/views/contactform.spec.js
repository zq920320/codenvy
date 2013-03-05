define(["jquery","views/contactform","models/Contact","text!templates/contactform.html"],

    function($,ContactForm,Contact,formTemplate){

        describe("View : ContactForm", function(){

            describe("module", function(){

                it("can be imported", function(){
                    expect(ContactForm).to.be.ok;
                });

                it("exports get", function(){
                    expect(ContactForm).to.respondTo("get");
                });

                it("exports ContactForm", function(){
                    expect(ContactForm).to.respondTo("ContactForm");
                });

            });

            describe("gets", function(){
                it("requires a dom element", function(){
                    var fn = function(){
                        ContactForm.get();
                    };

                    expect(fn).to.throw("Need an element");
                });

                it("returns an instance of ContactForm", function(){
                    expect(ContactForm.get(jQuery("<form>")))
                        .to.be.instanceOf(ContactForm.ContactForm);
                });
            });

            describe("ContactForm", function(){

                var formElement = null, sendMessageStub = null;

                function buildForm(){
                    formElement = jQuery(formTemplate);
                    $("body").append(formElement);
                    return formElement;
                }

                afterEach(function(){
                    if(sendMessageStub){
                        sendMessageStub.restore();
                        sendMessageStub = null;
                    }

                    if(formElement){
                        formElement.remove();
                        formElement = null;
                    }
                });

                it("raises an invalid event if email is not provided", function(done){
                    var form = ContactForm.get(buildForm());

                    form.on("invalid",function(field,message){
                        expect(field).to.equal("email");
                        expect(message).to.equal(form.settings.invalidEmailErrorMessage);
                        done();
                    });

                    $(form.el).valid();
                });

                it("raises an invalid event if email is not valid", function(done){
                    var form = ContactForm.get(buildForm());

                    form.on("invalid",function(field,message){
                        expect(field).to.equal("email");
                        expect(message).to.equal(form.settings.invalidEmailErrorMessage);
                        done();
                    });

                    $(form.el).find("input[name='email']").val("this is not a valid email");

                    $(form.el).valid();
                });

                it("raises an invalid event if message is not provided", function(done){
                    var form = ContactForm.get(buildForm());

                    form.on("invalid",function(field,message){
                        expect(field).to.equal("message");
                        expect(message).to.equal(form.settings.invalidMessageErrorMessage);
                        done();
                    });

                    $(form.el).find("input[name='email']").val("bob@gmail.com");

                    $(form.el).valid();
                });

                it("triggers submitting event before submission", function(done){
                    var sender = "bob@gmail.com", message = "hello", form = ContactForm.get(buildForm());

                    sendMessageStub = sinon.stub(Contact,"sendMessage",function(s,m){
                    });

                    form.on("submitting", function(){
                        done();
                    });

                    $(form.el).find("input[name='email']").val(sender);
                    $(form.el).find("textarea[name='message']").val(message);

                    $(form.el).submit();
                });

                it("calls Contact.sendMessage on form submission", function(done){

                    var sender = "bob@gmail.com", message = "hello", form = ContactForm.get(buildForm());

                    sendMessageStub = sinon.stub(Contact,"sendMessage",function(s,m){
                        expect(s).to.equal(sender);
                        expect(m).to.equal(message);
                        done();
                    });

                    $(form.el).find("input[name='email']").val(sender);
                    $(form.el).find("textarea[name='message']").val(message);

                    $(form.el).submit();
                });

                it("triggers invalid event if Contact.sendMessage fails", function(done){

                    var sender = "bob@gmail.com", message = "hello", form = ContactForm.get(buildForm());

                    sendMessageStub = sinon.stub(Contact,"sendMessage",function(s,m,success,error){
                        error();
                    });

                    form.on("invalid",function(field,message){
                        expect(message).to.equal(form.settings.failedToSendErrorMessage);
                        done();
                    });

                    $(form.el).find("input[name='email']").val(sender);
                    $(form.el).find("textarea[name='message']").val(message);

                    $(form.el).submit();
                });

                it("shows success-message panel if Contact.sendMessage succeeds", function(done){

                    var sender = "bob@gmail.com", message = "hello", form = ContactForm.get(buildForm());

                    sendMessageStub = sinon.stub(Contact,"sendMessage",function(s,m,success,error){
                        success();
                    });

                    $(form.el).find("input[name='email']").val(sender);
                    $(form.el).find("textarea[name='message']").val(message);

                    $(form.el).submit();

                    setTimeout(function(){
                        expect($(form.el).find(".success-message").hasClass("hidden")).to.be.false;
                        done();
                    },500);
                });
            });

        });

    }
);
