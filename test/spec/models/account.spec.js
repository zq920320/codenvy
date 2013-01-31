define(["models/account"], function(Account){

	describe("Models : Account", function(){

		describe("module", function(){

			it("can be imported", function(){
				expect(Account).to.be.ok;
			});

			it("exports signUp method", function(){
				expect(Account).to.respondTo("signUp");
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

		});

	});

});