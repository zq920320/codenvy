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
 
define(["jquery","underscore", "backbone", "models/account","views/accountformbase","validation"],

    function($, _, Backbone, Account){
        var animationFrequency = 500;
        var CreateWS = Backbone.View.extend({
            
            initialize : function(){
                setInterval(_.bind(this.onFrameUpdate,this),animationFrequency);
                Account.createWorkspace(
                    Account.getQueryParameterByName('username'),
                    Account.getQueryParameterByName('bearertoken'),
                    Account.getQueryParameterByName('workspace'),
                    Account.getQueryParameterByName('redirect_url'),
                    _.bind(function(d){
                        this.trigger("success",d);
                    },this),
                    _.bind(function(errors){

                        if(errors.length !== 0){
                            this.trigger(
                                "invalid",
                                errors[0].getFieldName(),
                                errors[0].getErrorDescription()
                            );
                        }
                    },this)
                );
            },

            onFrameUpdate : function(){
                var currentFrame = this.getCurrentFrame();
                this.getNextFrame().addClass("visible");
                currentFrame.removeClass("visible");
            },

            getNextFrame : function(){
                var currentFrame = this.getCurrentFrame();

                if(this.direction === "next"){
                    if(currentFrame.next(".loader").length !== 0){
                        return currentFrame.next(".loader");
                    }else{
                        this.direction = "prev";
                        return currentFrame.prev(".loader");
                    }
                }else{
                    if(currentFrame.prev(".loader").length !== 0){
                        return currentFrame.prev(".loader");
                    }else{
                        this.direction = "next";
                        return currentFrame.next(".loader");
                    }
                }
            },

            getCurrentFrame : function(){
                return $(".loader.visible");
            }
        });

        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }

                return new CreateWS({ el : form });
            }
        };

    }
);
