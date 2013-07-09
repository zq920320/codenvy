define(["jquery", "backbone"],
    function($, Backbone){
        var GC_banner = Backbone.View.extend({
         initialize: function(){

                var divs = $("div.cta-banner").get().sort(function(){ 
                    return Math.round(Math.random())-0.5;
                }).slice(0,1);
                $(divs).appendTo(divs[0].parentNode).show();

                var divsm = $("div.cta-banner-m").get().sort(function(){ 
                    return Math.round(Math.random())-0.5;
                }).slice(0,1);
                $(divsm).appendTo(divsm[0].parentNode).show();
            }
        });
        return GC_banner;
    }
);