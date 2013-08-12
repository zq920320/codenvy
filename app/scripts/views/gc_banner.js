/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2013] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
 
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