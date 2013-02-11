define(["jquery", "models/contact"], function($,Contact){

    describe("Models : Contact", function(){

        describe("module", function(){

            it("can be imported", function(){
                expect(Contact).to.be.ok;
            });

            it("exports sendMessage", function(){
                expect(Contact).to.respondTo("sendMessage");
            });

        });

        describe("sendMessage", function(){

            var ajaxStub = null;

            afterEach(function(){
                if(ajaxStub){
                    ajaxStub.restore();
                    ajaxStub = null;
                }
            })

            it("requires sender argument", function(){
                var fn = function(){
                    Contact.sendMessage();
                };

                expect(fn).to.throw("Needs a sender");
            });

            it("requires message argument", function(){
                var fn = function(){
                    Contact.sendMessage("sender");
                };

                expect(fn).to.throw("Needs a message");
            });

            it("requires sender to be a valid email address", function(){
                var fn = function(){
                    Contact.sendMessage("sender","message");
                };

                expect(fn).to.throw("Sender must be a valid email address");
            });


            it("invokes UserVoice ticket service", function(done){
                
                var sender = "bob@gmail.com", message = "Message";

                ajaxStub = sinon.stub($,"ajax",function(options){
                    expect(options.data.email).to.equal(sender);
                    expect(options.data.ticket.message).to.equal(message);
                    done();
                });

                Contact.sendMessage(sender,message);
            });

            it("invokes success callback if UserVoice is happy", function(done){

                ajaxStub = sinon.stub($,"ajax",function(options){
                    options.success();                 
                });

                Contact.sendMessage("bob@gmail.com","Message",function(){ done(); });
            });

            it("invokes error callback if UserVoice is not happy", function(done){

                ajaxStub = sinon.stub($,"ajax",function(options){
                    options.error();                 
                });

                Contact.sendMessage("bob@gmail.com","Message",function(){}, function(){ done(); });
            });

        });

    });

});