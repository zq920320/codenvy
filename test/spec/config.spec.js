define(["config"],function(Config){

    describe("Application Configuration", function(){

        describe("module", function(){
            it("can be imported", function(){
                expect(Config).to.be.ok;
            });
        });

    });

});
