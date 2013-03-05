define(["jquery","underscore","views/selectdomain",
    "models/account","models/tenant","text!templates/select-domain.html"],

    function($,_,SelectDomain,Account,Tenant,selectDomainTemplate){

        describe("Views : SelectDomain", function(){

            describe("module", function(){

                it("can be imported", function(){
                    expect(SelectDomain).to.be.ok;
                });

                it("exports get", function(){
                    expect(SelectDomain).to.respondTo("get");
                });

                it("exports DomainSelector", function(){
                    expect(SelectDomain).to.respondTo("DomainSelector");
                });

            });

            describe("Domain Selector", function(){

                afterEach(function(){
                    if(typeof Account.getTenants.restore !== 'undefined'){
                        Account.getTenants.restore();
                    }
                });

                it("requires an element to bind to", function(){
                    var fn = function(){
                        new SelectDomain.DomainSelector();
                    };
                    expect(fn).to.throw("Need an element");
                });

                it("calls Account.getTenants", function(done){
                    sinon.stub(Account,"getTenants",function(success,error){
                        done();
                    });

                    new SelectDomain.DomainSelector({ el : jQuery("<div>")});
                });

                it("renders tenants as a list of links", function(done){

                    var tenants = ["bob","mark","philip"],
                        selectorEl = jQuery(selectDomainTemplate),
                        urls = _.map(tenants, function(t){
                            return window.location.protocol +
                                "//" + t + "." + window.location.host;
                        });

                    sinon.stub(Account,"getTenants",function(success,error){
                        success(_.map(tenants, function(t){
                            return new Tenant.Tenant({ name : t });
                        }));
                    });

                    var sd = new SelectDomain.DomainSelector({ el : selectorEl});

                    setTimeout(function(){
                        expect($(sd.el).find(".domain-list").children().length).to.equal(3);
                        _.each($(sd.el).find("ul.domain-list > li > a"), function(link){
                            expect(urls.indexOf($(link).attr("href"))).to.not.equal(-1);
                            expect(tenants.indexOf($(link).html())).to.not.equal(-1);
                        });
                        done();
                    },500);

                });

                it("removes loading class once tenants are loaded", function(done){

                    var selectorEl = jQuery(selectDomainTemplate);

                    sinon.stub(Account,"getTenants",function(success,error){
                        success([]);
                    });

                    var sd = new SelectDomain.DomainSelector({ el : selectorEl});

                    setTimeout(function(){
                        expect($(sd.el).hasClass("loading")).to.be.false;
                        done();
                    },500);

                });

                it("shows error message if Account.getTenants fails", function(done){
                    var selectorEl = jQuery(selectDomainTemplate),
                        errorMessage = "Something went wrong";

                    sinon.stub(Account,"getTenants",function(success,error){
                        error([
                            new Account.AccountError(null,errorMessage)
                        ]);
                    });

                    var sd = new SelectDomain.DomainSelector({ el : selectorEl});

                    setTimeout(function(){
                        expect($(sd.el).find(".error").hasClass("hidden")).to.be.false;
                        expect($(sd.el).find(".error").html()).to.equal(errorMessage);
                        done();
                    },500);
                });

            });

        });
    }
);
