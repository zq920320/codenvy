<!-- Prototypes -->
<script type="text/javascript" src="scripts/Presenter.js"></script>

<!-- Singletons -->
<script type="text/javascript" src="scripts/main.js"></script>

<script type="text/javascript" src="scripts/util.js"></script>
<script type="text/javascript" src="scripts/view.js"></script>
<script type="text/javascript" src="scripts/views/loader.js"></script>

<script type="text/javascript" src="scripts/model.js"></script>

<script type="text/javascript" src="scripts/factory.js"></script>
<script type="text/javascript" src="scripts/configuration.js"></script>

<% if (request.getParameterValues("javaScriptToLoad") != null) { 
       String[] javaScriptsToLoad = request.getParameterValues("javaScriptToLoad");
       for (int i = 0; i < javaScriptsToLoad.length; i++) { %>
           <script type="text/javascript" src="<%= javaScriptsToLoad[i] %>"></script>  
<%     }
   }%>
