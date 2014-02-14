<!-- Prototypes -->
<script type="text/javascript" src="/analytics/scripts/Presenter.js"></script>

<!-- Singletons -->
<script type="text/javascript" src="/analytics/scripts/main.js"></script>

<script type="text/javascript" src="/analytics/scripts/util.js"></script>
<script type="text/javascript" src="/analytics/scripts/view.js"></script>
<script type="text/javascript" src="/analytics/scripts/views/loader.js"></script>

<script type="text/javascript" src="/analytics/scripts/model.js"></script>

<script type="text/javascript" src="/analytics/scripts/factory.js"></script>
<script type="text/javascript" src="/analytics/scripts/configuration.js"></script>

<% if (request.getParameterValues("javaScriptToLoad") != null) { 
       String[] javaScriptsToLoad = request.getParameterValues("javaScriptToLoad");
       for (int i = 0; i < javaScriptsToLoad.length; i++) { %>
           <script type="text/javascript" src="<%= javaScriptsToLoad[i] %>"></script>  
<%     }
   }%>
