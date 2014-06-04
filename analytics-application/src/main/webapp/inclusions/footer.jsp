<%-- 
 CODENVY CONFIDENTIAL
 ________________

 [2012] - [2014] Codenvy, S.A.
 All Rights Reserved.
 NOTICE: All information contained herein is, and remains
 the property of Codenvy S.A. and its suppliers,
 if any. The intellectual and technical concepts contained
 herein are proprietary to Codenvy S.A.
 and its suppliers and may be covered by U.S. and Foreign Patents,
 patents in process, and are protected by trade secret or copyright law.
 Dissemination of this information or reproduction of this material
 is strictly forbidden unless prior written permission is obtained
 from Codenvy S.A.. 
--%>

<!-- footer>
    <img src="/analytics/images/footerlogo.png" alt="Codenvy | Footer logo">
</footer -->

<!-- Prototypes -->
<script type="text/javascript" src="/analytics/scripts/Presenter.js"></script>

<!-- Singletons -->
<script type="text/javascript" src="/analytics/scripts/main.js"></script>

<script type="text/javascript" src="/analytics/scripts/util.js"></script>

<script type="text/javascript" src="/analytics/scripts/view.js"></script>
<script type="text/javascript" src="/analytics/scripts/views/loader.js"></script>
<script type="text/javascript" src="/analytics/scripts/views/accordion.js"></script>
<script type="text/javascript" src='/analytics/scripts/views/database-table.js'></script>
<script type="text/javascript" src="/analytics/scripts/views/line-chart.js"></script>

<script type="text/javascript" src="/analytics/scripts/model.js"></script>

<script type="text/javascript" src="/analytics/scripts/factory.js"></script>

<script type="text/javascript" src="/analytics/scripts/configuration.js"></script>

<script>
/** Load server configuration */
    jQuery(function() {
        analytics.configuration.serverConfiguration = {
            "reportGenerationDate": "<%=com.codenvy.analytics.metrics.Parameters.TO_DATE.getDefaultValue() %>",
        };        
    });
</script>


<% if (request.getParameterValues("javaScriptToLoad") != null) { 
       String[] javaScriptsToLoad = request.getParameterValues("javaScriptToLoad");
       for (int i = 0; i < javaScriptsToLoad.length; i++) { %>
           <script type="text/javascript" src="<%= javaScriptsToLoad[i] %>"></script>  
<%     }
   }%>

<!--  load calendar jquery plugin  -->
<script type="text/javascript">
    $(function () {
        $("#datepicker-from-date").datepicker({dateFormat: "yy-mm-dd"});
        $("#datepicker-to-date").datepicker({dateFormat: "yy-mm-dd"});
    });
</script>