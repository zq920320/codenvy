(function(window){

    define(["config"],function(Config){

        return {
            getFromQueryString : function(queryString){
                var m = new RegExp(
                    Config.originTrackerUrlParameterName+"=([^&]+)",
                    "gi").exec(queryString);
                if(m){ return m[1]; }
            },

            getFromLocation : function(){
                return this.getFromQueryString(window.location.search);
            }
        };

    });

}(window));
