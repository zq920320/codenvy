define(["jquery"],function($){

    /*
        AccountError is used to report errors through error callback function
        (see details below ). Example usage:

        new AccountError("password","Your password is too short")

    */

    var AccountError = function(fieldName, errorDescription){
        return {
            getFieldName : function(){
                return fieldName;
            },

            getErrorDescription : function(){
                return errorDescription;
            }
        };
    };



    /*
        Every method accepts 0 or more data values and two callbacks (success and error)

        Success callback spec:

            function success(data){
                // data includes any additonal data you want to pass back
            }

        Error callback spec:

            function error(errors){
                // errors is a list of AccountError instances
            }

    */


    return {

        signUp : function(email,domain,success,error){

            //example

            //hit the server with the data we got back


        },

        login : function(email,password,success,error){

        },

        createTenant : function(email,domain,success,error){

            var tenantServiceConfirmUrl = "/cloud-admin/rest/cloud-admin/public-tenant-service/create-with-confirm";
            var createTenantUrl = tenantServiceConfirmUrl + "/" + encodeURIComponent(domain) + "/" + email;

            var request = $.ajax({
                url : createTenantUrl,
                type: POST,
                success : function(output, status, xhr){

                        //Succes, redirect to thankyou.jsp
                        success({url: 'thankyou.jsp'});
                },
                error : function(xhr, status, err){

                    //if redirect status code, redirect to ResponseHeader
                    if((xhr.getReponseHeader("Location") != null) && (xhr.status != "") && (xhr.status == 301 || xhr.status == 302 || xhr.status == 303)){
                        
                        //redirect to Location
                        errors.push({url: xhr.getReponseHeader("Location")});
                    }

                    //else error code, display error message
                    else {
                        errors.push(new AccountError("domain","Something went wrong: " + err));
                        
                    }
                    error(errors);      
                }
            });
        },

        recoverPassword : function(email,success,error){

        }

    };
});
