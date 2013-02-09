define(["views/ideloader","models/account"],
    function(IDELoader,Account){

        describe("Views : IDE Loader", function(){

            describe("module", function(){

                it("can be imported", function(){
                    expect(IDELoader).to.be.ok;
                });

                it("exports get", function(){
                    expect(IDELoader).to.respondTo("get");
                });

                it("exports IDELoader", function(){
                    expect(IDELoader).to.respondTo("IDELoader");
                });

            });

            describe("IDELoader", function(){

                afterEach(function(){
                    if(typeof Account.waitForTenant.restore !== 'undefined'){
                        Account.waitForTenant.restore();
                    }
                });

                it("calls Account.waitForTenant", function(done){
                    sinon.stub(Account,"waitForTenant",function(){
                        done();
                    });

                    new IDELoader.IDELoader();
                });

                it("triggers ready event when Account.waitForTenant succeeds", function(done){

                    var successData = { url : "redirect-here" };

                    sinon.stub(Account,"waitForTenant",function(success,error){
                        setTimeout(function(){
                            success(successData);    
                        },500);
                    });

                    new IDELoader.IDELoader().on("ready",function(d){
                        expect(d).to.eql(successData);
                        done();
                    });

                });

                it("triggers error event when Account.waitForTenant fails", function(done){

                    var errorMessage = "Something went wrong";

                    sinon.stub(Account,"waitForTenant",function(success,error){
                        setTimeout(function(){
                            error([
                                new Account.AccountError(null,errorMessage)
                            ]);
                        },500);
                    }); 

                    new IDELoader.IDELoader().on("error", function(e){
                        expect(e).to.equal(errorMessage);
                        done();
                    });

                });

            });

        });

    }
);