define(["jquery","underscore","json","models/tenant","backbone","text!../../data/tenants.json"],

    function($,_,JSON,Tenant,Backbone,tenantsData){

        describe("Models : Tenant", function(){

            describe("module", function(){
                it("can be imported", function(){
                    expect(Tenant).to.be.ok;
                });

                it("exports getTenants", function(){
                    expect(Tenant).to.respondTo("getTenants");
                });

                it("exports Tenants Collection", function(){
                    expect(Tenant).to.respondTo("Tenants");
                });

                it("exports Tenant Model", function(){
                    expect(Tenant).to.respondTo("Tenant");
                });
            });

            describe("Tenants", function(){

                var fakeTenants = JSON.parse(tenantsData);

                afterEach(function(){
                    if(typeof $.ajax.restore !== 'undefined'){
                        $.ajax.restore();
                    }
                });

                it("returns a list of Tenant models through fetch", function(done){
                    sinon.stub($,"ajax",function(ajaxSettings){
                        ajaxSettings.success(fakeTenants);
                        return fakeTenants;
                    });

                    $.when(new Tenant.Tenants().fetch()).done(function(tenants){
                        expect(tenants.length).to.equal(3);

                        _.each(tenants, function(t){
                            expect(fakeTenants.indexOf(t.get("name"))).to.not.equal(-1);
                        });

                        done();
                    });
                });

                it("fails if ajax fails", function(done){
                    sinon.stub($,"ajax",function(ajaxSettings){
                        var dfd = $.Deferred();
                        dfd.reject();
                        ajaxSettings.error();
                        return dfd.promise();
                    });

                    $.when(new Tenant.Tenants().fetch()).fail(function(){
                        done();
                    });

                });

            });

        });

    }
);
