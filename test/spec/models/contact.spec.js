define(["jquery", "models/contact", "models/originextractor"], function($,Contact,OriginExtractor){

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

            var ajaxStub = null, getFromLocationStub = null;

            afterEach(function(){
                if(ajaxStub){
                    ajaxStub.restore();
                    ajaxStub = null;
                }

                if(getFromLocationStub){
                    getFromLocationStub.restore();
                    getFromLocationStub = null;
                }
            });

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

            it("invokes OriginExtractor.getFromLocation", function(done){
                getFromLocationStub = sinon.stub(OriginExtractor,"getFromLocation",function(){
                    done();
                });

                ajaxStub = sinon.stub($,"ajax",function(options){

                });

                var sender = "bob@gmail.com", message = "Message";
                Contact.sendMessage(sender,message);
            });

            it("passes what OriginExtractor.getFromLocation returns along to UserVoice", function(done){

                var sender = "bob@gmail.com", message = "Message", origin = "this is origin";

                getFromLocationStub = sinon.stub(OriginExtractor,"getFromLocation",function(){
                    return origin;
                });

                ajaxStub = sinon.stub($,"ajax",function(options){
                    expect(options.data.ticket.referrer).to.equal(origin);
                    done();
                });

                Contact.sendMessage(sender,message);
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
