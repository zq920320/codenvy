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
 
define(["jquery","models/account"],function($,Account){
    
    var tenantName;

    return {

        getUserQueueInfo : function(success,error){
            tenantName = Account.getQueryParameterByName("tenant");
            $.ajax({
                url : "/cloud-admin/rest/cloud-admin/update/state?tenant=" + tenantName,
                type : "GET",
                success : function(output/*,status, xhr*/){
                        if (output.queuePosition === "-1") {window.location = window.location.protocol + "//" + tenantName + "." + window.location.host;}
                        else{
                            success({
                                queue : parseFloat(output.queuePosition) + 1,
                                readyTime : new Date(parseFloat(output.queuePosition,10)*parseFloat(output.timeTenantUpdate,10) + new Date().getTime()).toLocaleString()
                            });
                        }
                },
                error : function(){
                    Account.removeCookie("autologin");
                    error();
                }
            });
        },
        speedUp : function(success,error){
            $.ajax({
                url : "/cloud-admin/rest/cloud-admin/update/promote?tenant=" + tenantName,
                type : "POST",
                success : function(output/*,status, xhr*/){
                        success({
                            queue : output.queuePosition,
                            readyTime : new Date(parseFloat(output.queuePosition,10)*parseFloat(output.timeTenantUpdate,10) + new Date().getTime()).toLocaleString()
                        });
                },
                error : function(){
                        error();
                }
            });
            
        }

    };

});
