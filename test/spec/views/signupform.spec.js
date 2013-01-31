define(["jquery","views/signupform"],function($,SignUpForm){

	describe("Views : SignUpForm",function(){

		describe("module", function(){

			it("can be imported", function(){
				expect(SignUpForm).to.be.ok;
			});

			it("exports get method", function(){
				expect(SignUpForm).to.respondTo('get');
			});

		});

		describe("get", function(){

			it("requires a form", function(){

				var fn = function(){
					SignUpForm.get();
				};

				expect(fn).to.throw("Need a form");

			});

			it("returns a SignUpForm view with el set to form", function(){
				var form = jQuery("<form>"), v = SignUpForm.get(form);
				expect(v).to.be.ok;
				expect(v.el).to.equal(form[0]);
			});

		});

	});

});