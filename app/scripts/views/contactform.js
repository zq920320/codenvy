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
 
define(["underscore","views/form","models/contact"],function(_,Form,Contact){

    var ContactForm = Form.extend({

        settings : {
            invalidEmailErrorMessage : "Please provide a valid email address so we can get back to you",
            invalidMessageErrorMessage : "Please write something",
            failedToSendErrorMessage : "Something went wrong. Your message was not sent. Please try again later."
        },

        __validationRules : function(){
            return {
                email: {
                    required: true,
                    email: true
                },
                message: {
                    required : true
                }
            };
        },

        __validationMessages : function(){
return {
                email : {
                    required : this.settings.invalidEmailErrorMessage,
                    email : this.settings.invalidEmailErrorMessage
                },
                message : {
                    required : this.settings.invalidMessageErrorMessage
                }
            };
        },

        __submit : function(){

            this.__showProgress();

            this.trigger("submitting");

            Contact.sendMessage(
                this.$("input[name='email']").val(),
                this.$("textarea[name='message']").val(),
                _.bind(function(){
                    this.$(".success-message").removeClass("hidden");
                    this.$("label:contains('Your email address:')").removeClass("hidden");
                    this.__restoreForm();
                },this),
                _.bind(function(){
                    this.trigger("invalid",null,this.settings.failedToSendErrorMessage);
                    this.__restoreForm();
                },this)
            );
        },

        __showErrors : function(errorMap){
			this.$(".success-message").addClass("hidden");
			this.$("label:contains('Your email address:')").addClass("hidden");
            if(typeof errorMap.email !== 'undefined'){
                this.trigger("invalid","email",errorMap.email);
                return;
            }

            if(typeof errorMap.message !== 'undefined'){
                this.trigger("invalid","message",errorMap.message);
                return;
            }
        },

        __restoreForm : function(){
            this.$("input[type='submit']").removeAttr("disabled");
            this.$("input[name='email']").val("");
            this.$("textarea[name='message']").val("");
        },

        __showProgress : function(){
            this.$("input[type='submit']").attr("disabled","disabled");
 
        }
    });

    return {
        get : function(el){
            if(typeof el === 'undefined'){
                throw new Error("Need an element");
            }

            return new ContactForm({ el : el });
        },

        ContactForm : ContactForm
    };

});
