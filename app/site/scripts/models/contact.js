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
 
define(["jquery","config","models/originextractor"],function($,Config,OriginExtractor){

    function __isValidEmail(value){
        // http://projects.scottsplayground.com/email_address_validation/
        return (/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))$/i).test(value);
    }


    function __sendData(email, ticketSubject, ticketMessage, data, success,error){

        var origin = OriginExtractor.getFromLocation(),
            dataBundle = $.extend(
                {
                    client: Config.userVoiceClientKey,
                    email : email,
                    ticket : {
                        subject : ticketSubject,
                        message : ticketMessage
                    }
                },data);

        if(typeof origin !== 'undefined'){
            dataBundle.ticket.referrer = origin;
        }

        // Execute the JSONP request to submit the ticket
        $.ajax({
            url: 'https://' + Config.userVoiceSubdomain + '.uservoice.com/api/v1/tickets/create_via_jsonp.json?callback=?',
            data: dataBundle,
            dataType : "jsonp",
            success: function() { success(); },
            error: function() { error(); }
        });
    }

    return {

        sendMessage : function(sender,message,success,error){
            if(typeof sender === 'undefined'){
                throw new Error("Needs a sender");
            }

            if(typeof message === 'undefined'){
                throw new Error("Needs a message");
            }

            if(!__isValidEmail(sender)){
                throw new Error("Sender must be a valid email address");
            }

            __sendData(sender, "Contact", message, {}, success, error);
        }

    };

});
