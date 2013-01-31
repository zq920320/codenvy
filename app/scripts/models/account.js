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

        createTenant : function(success,error){

        },

        recoverPassword : function(email,success,error){

        }

    };
});
