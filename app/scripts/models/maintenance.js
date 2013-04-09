define(["jquery","models/account"],function($,Account){
    
    var tenantName;

    return {

        getUserQueueInfo : function(success,error){
            tenantName = Account.Utils.getQueryParameterByName("tenant");
            $.ajax({
                url : "/cloud-admin/rest/cloud-admin/update/state?tenant=" + tenantName,
                type : "GET",
                success : function(output/*,status, xhr*/){
                        if (output.queuePosition === "-1") {window.location = window.location.protocol + "//" + tenantName + "." + window.location.host;}
                        else{
                            success({
                                queue : output.queuePosition,
                                readyTime : new Date(parseFloat(output.queuePosition,10)*parseFloat(output.timeTenantUpdate,10) + new Date().getTime()).toLocaleString()
                            });
                        }
                },
                error : function(){
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
