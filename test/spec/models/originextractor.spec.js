define(["underscore","config","models/originextractor"],function(_,Config,OriginExtractor){

    describe('Model : Origin extractor', function () {

        describe("module", function(){
            it("can be imported", function(){
                expect(OriginExtractor).to.be.ok;
            });

            it("exports getFromQueryString method", function(){
                expect(OriginExtractor).to.respondTo("getFromQueryString");
            });

            it("exports getFromLocation method", function(){
                expect(OriginExtractor).to.respondTo("getFromLocation");
            });
        });

        describe("getFromLocation", function(){

            var getFromQueryStringStub = null;

            afterEach(function(){
                if(getFromQueryStringStub){
                    getFromQueryStringStub.restore();
                    getFromQueryStringStub = null;
                }
            });

            it("calls OriginExtractor.getFromQueryString with window.location.search parameter", function(done){
                getFromQueryStringStub = sinon.stub(OriginExtractor,
                    "getFromQueryString", function(queryString){
                        expect(queryString).to.eql(window.location.search);
                        done();
                    });

                OriginExtractor.getFromLocation();
            });

        });

        describe("getFromQueryString", function(){

            it("returns nothing if nothing is passed in",function(){
                expect(OriginExtractor.getFromQueryString()).to.not.be.ok;
            });

            it("returns origin tracker value if it's in the query string", function(){

                var queryStrings = [
                    "?o=pricing-history",
                    "?another+parameter=foo&o=pricing-history",
                    "?o=pricing-history&another+parameter=foo",
                ], parameterValue = "pricing-history";

                _.each(queryStrings, function(q){
                    expect(OriginExtractor.getFromQueryString(q)).to.equal(parameterValue);
                });

            });

            it("returns nothing if tracker value is not in the query string", function(){

                var queryStrings = [
                    "?oops=pricing-history",
                    "?another+parameter=foo&o1=pricing-history",
                    "?os=pricing-history&another+parameter=foo",
                    "?", ""
                ];

                _.each(queryStrings, function(q){
                    expect(OriginExtractor.getFromQueryString(q)).to.not.be.ok;
                });

            });

        });

    });

});
