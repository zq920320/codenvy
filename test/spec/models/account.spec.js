define(["models/account","models/tenant","underscore"], function(Account,Tenant,_){

    describe("Models : Account", function(){

        describe("module", function(){

            it("can be imported", function(){
                expect(Account).to.be.ok;
            });

            it("exports AccountError", function(){
                expect(Account).to.respondTo("AccountError");
            });

            it("exports login method", function(){
                expect(Account).to.respondTo("login");
            });

            it("exports createTenant method", function(){
                expect(Account).to.respondTo("createTenant");
            });

            it("exports recoverPassword method", function(){
                expect(Account).to.respondTo("recoverPassword");
            });

            it("exports isValidDomain method", function(){
                expect(Account).to.respondTo("isValidDomain");
            });

            it("exports setupPassword method", function(){
                expect(Account).to.respondTo("setupPassword");
            });

            it("exports confirmSetupPassword method", function(){
                expect(Account).to.respondTo("confirmSetupPassword");
            });

            it("exports getTenants method", function(){
                expect(Account).to.respondTo("getTenants");
            });

            it("exports waitForTenant method", function(){
                expect(Account).to.respondTo("waitForTenant");
            });

        });

        describe("isValidDomain", function(){

            it("does not allow empty string/null/undefined", function(){
                expect(Account.isValidDomain()).to.be.false;
                expect(Account.isValidDomain("")).to.be.false;
                expect(Account.isValidDomain(null)).to.be.false;
            });

            it("only allows names that end with .codenvy.com", function(){
                expect(Account.isValidDomain("bob.codenvy.com")).to.be.true;
                expect(Account.isValidDomain("bob.google.com")).to.be.false;
            });

            it("only allows names that contain lower-case letters, digits, and dash ('-')", function(){
                var invalidChars = "~`!@#$%^&*()_=+;:\\|/?.,><";

                _.each(invalidChars,function(c){
                    expect(Account.isValidDomain("bob" + c + ".codenvy.com")).to.be.false;
                });

                expect(Account.isValidDomain("BOB.codenvy.com")).to.be.false;
                expect(Account.isValidDomain("bob1.codenvy.com")).to.be.true;
                expect(Account.isValidDomain("bob123.codenvy.com")).to.be.true;
                expect(Account.isValidDomain("bob-124.codenvy.com")).to.be.true;
            });

            it("only allows names that do not start with a digit", function(){
                expect(Account.isValidDomain("1bob.codenvy.com")).to.be.false;
            });

            it("only allows names that do not start or end with a dash", function(){
                expect(Account.isValidDomain("-bob.codenvy.com")).to.be.false;
                expect(Account.isValidDomain("-bob-.codenvy.com")).to.be.false;
                expect(Account.isValidDomain("bob-.codenvy.com")).to.be.false;
            });

            it("only allows names up to 20 symbols long", function(){

                function makeAName(length){
                    var str = "";
                    while(length > 0){
                        str = str + "a";
                        length--;
                    }
                    return str;
                }

                expect(Account.isValidDomain(makeAName(4) + ".codenvy.com")).to.be.true;
                expect(Account.isValidDomain(makeAName(19) + ".codenvy.com")).to.be.true;
                expect(Account.isValidDomain(makeAName(20) + ".codenvy.com")).to.be.true;
                expect(Account.isValidDomain(makeAName(21) + ".codenvy.com")).to.be.false;
                expect(Account.isValidDomain(makeAName(50) + ".codenvy.com")).to.be.false;
            });

            it("only allows names that are at least 1 character long", function(){
                expect(Account.isValidDomain(".codenvy.com")).to.be.false;
                expect(Account.isValidDomain("a.codenvy.com")).to.be.true;
            });

        });

        describe("getTenants", function(){

            afterEach(function(){
                if(typeof Tenant.getTenants.restore !== 'undefined'){
                    Tenant.getTenants.restore();
                }
            });

            it("calls Tenant.getTenants", function(done){

                sinon.stub(Tenant,"getTenants", function(){
                    done();
                    return [];
                });

                Account.getTenants(function(){});
            });

            it("calls success callback if Tenant.getTenants resolves", function(done){

                var ts = ["bob.codenvy.com","mark.codenvy.com"],
                    success = function(tenants){
                        expect(tenants).to.eql(ts);
                        done();
                    }

                sinon.stub(Tenant,"getTenants",function(){
                    var dfd = $.Deferred();
                    dfd.resolve(ts);
                    return dfd.promise();
                });

                Account.getTenants(success);

            });

            it("calls error callback if Tenant.getTenants fails", function(done){

                var msg = "Cannot get tenants",
                    error = function(errors){
                        expect(errors.length).to.equal(1);
                        expect(errors[0].getErrorDescription()).to.equal(msg);
                        done();
                    };

                sinon.stub(Tenant,"getTenants",function(){
                    var dfd = $.Deferred();
                    dfd.reject(msg);
                    return dfd.promise();
                });

                Account.getTenants(null,error);

            });

        });


    });

});
