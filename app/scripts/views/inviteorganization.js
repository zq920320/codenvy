define(["models/account","backbone"], function(Account,Backbone){

    // set {name} of organization from window.location
    
    var InviteOrganization = Backbone.View.extend({
        setOrganization : function(){
            this.el.innerHTML=("<b>" + Account.getParam("organization") + "</b>");
        }
    });

    return {
        get : function(el){
            if(typeof el === 'undefined'){
                throw new Error("Need a element");
            }
            return new InviteOrganization({el:el});
        },

        InviteOrganization : InviteOrganization
    };

});