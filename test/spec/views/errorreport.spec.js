define(["jquery", "views/errorreport"],function($,ErrorReport){

    describe("Views : ErrorReport", function(){

        describe("module", function(){
            it("can be imported", function(){
                expect(ErrorReport).to.be.ok;
            });

            it("exports get", function(){
                expect(ErrorReport).to.respondTo("get");
            });

            it("exports ErrorReport", function(){
                expect(ErrorReport).to.respondTo("ErrorReport");
            });
        });

        describe("get", function(){
            it("requires a DOM element", function(){
                var fn = function(){
                    ErrorReport.get();
                };

                expect(fn).to.throw("Need a DOM element");
            });

            it("returns an instance of ErrorReport with el set to the DOM piece", function(){

                var d = jQuery("<div>"), v = ErrorReport.get(d);

                expect(v).to.be.instanceOf(ErrorReport.ErrorReport);
                expect(v.el).to.equal(d[0]);
            });
        });

        describe("ErrorReport", function(){

            it("supports show method", function(){
                expect(ErrorReport.ErrorReport).to.respondTo("show");
            });

            it("supports hide method", function(){
                expect(ErrorReport.ErrorReport).to.respondTo("hide");
            });

            describe("show", function(){
                it("requires a message to show", function(){
                    var v = ErrorReport.get(jQuery("<div>")),
                        fn = function(){ v.show(); };
                    expect(fn).to.throw("Need a message");
                });

                it("sets message as content of the el", function(){
                    var v = ErrorReport.get(jQuery("<div>")), err = "Oh no!";
                    v.show(err);
                    expect($(v.el).html()).to.equal(err);
                });

                it("adds expanded class to the el", function(){
                    var v = ErrorReport.get(jQuery("<div>")), err = "Oh no!";
                    v.show(err);
                    expect($(v.el).hasClass("expanded")).to.be.true;
                });
            });

            describe("hide", function(){

                it("clears the content of el", function(){
                    var v = ErrorReport.get(jQuery("<div>")), err = "Oh no!";
                    v.show(err);
                    v.hide();

                    expect($(v.el).html()).to.not.be.ok;
                });

                it("removes expanded class from the el", function(){
                    var v = ErrorReport.get(jQuery("<div>")), err = "Oh no!";
                    v.show(err);
                    v.hide();

                    expect($(v.el).hasClass("expanded")).to.be.false;
                });
            });

        });

    });

});
