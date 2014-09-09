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
 
define(["jquery","underscore","views/accountformbase","models/account"], 

    function($,_,AccountFormBase,Account){

        jQuery.validator.addMethod("phone", function(phone_number, element) {
            phone_number = phone_number.replace(/\s+/g, ""); 
            return this.optional(element) || /^[+]?[\d\-\s()]+$/.test(phone_number);
        }, "Please, specify a phone number only with digits, '-', '()' or started '+' symbols");

        var SetProfileUserForm = AccountFormBase.extend({
            settings : {
                successUpdatingProfile : "Your profile was updated successfully",
                noFirstNameErrorMessage : "Please, enter your first name",
                noLastNameErrorMessage : "Please, enter your last name",
                noPhoneErrorMessage : "Please, enter your phone number",
                noCompanyErrorMessage : "Please, enter your company",
                noRoleErrorMessage : "Please, select your role"
            },

            __validationRules : function(){
                return {
                    first_name: {
                        required : true
                    },
                    last_name: {
                        required : true
                    },
                    phone_work: {
                        required : true,
                        phone : true
                    },
                    company: {
                        required : true
                    },
                    title: {
                        required: true
                    }
                };
            },
            
            __validationMessages : function(){
                return {
                    first_name: {
                        required: this.settings.noFirstNameErrorMessage
                    },
                    last_name: {
                        required: this.settings.noLastNameErrorMessage
                    },
                    phone_work: {
                        required: this.settings.noPhoneErrorMessage
                    },
                    company: {
                        required: this.settings.noCompanyErrorMessage
                    },
                    title: {
                        required: this.settings.noRoleErrorMessage
                    }

                };
            },

            __submit : function(){
                this.trigger("submitting");
                //set request body
                var submitProfileForm = $(".cloud-ide-profile")[0];
                if (submitProfileForm)
                {
                    var body =  {  
                           "firstName" : Account.escapeSpecialSymbols(submitProfileForm.first_name.value.trim()),
                           "lastName" : Account.escapeSpecialSymbols(submitProfileForm.last_name.value.trim()),
                           "phone" : Account.escapeSpecialSymbols(submitProfileForm.phone_work.value.trim()),
                           "employer" : Account.escapeSpecialSymbols(submitProfileForm.company.value.trim()),
                           "jobtitle" : Account.escapeSpecialSymbols(submitProfileForm.title.value)
                     };

                    Account.updateProfile(
                        body,
                        _.bind(function(){
                            this.trigger("success");
                        },this),
                        _.bind(function(errors){
                            this.__restoreForm();
                            if(errors.length !== 0){
                                this.trigger(
                                    "invalid",
                                    errors[0].getFieldName(),
                                    errors[0].getErrorDescription()
                                );
                            }
                        },this)
                    );
                }
            },
            
            getUserProfileInfo : function(error){
                Account.getUserProfile(error);
            }
        });

        return {
            get : function(form){
                if(typeof form === 'undefined'){
                    throw new Error("Need a form");
                }
                return new SetProfileUserForm({el:form});
            },

            SetProfileUserForm : SetProfileUserForm
        };
    }
);