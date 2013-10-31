<% /*
    CODENVY CONFIDENTIAL
    __________________

    [2012] - [2013] Codenvy, S.A.
    All Rights Reserved.

    NOTICE:  All information contained herein is, and remains
    the property of Codenvy S.A. and its suppliers,
    if any.  The intellectual and technical concepts contained
    herein are proprietary to Codenvy S.A.
    and its suppliers and may be covered by U.S. and Foreign Patents,
    patents in process, and are protected by trade secret or copyright law.
    Dissemination of this information or reproduction of this material
    is strictly forbidden unless prior written permission is obtained
    from Codenvy S.A..
*/ %>

<%@page language="java"%>

<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>
<%@ page import="java.io.*" %>

<%@page import="com.google.gson.*"%>

<%!

public String getFactoryJSON(String factoryURL) throws Exception {
    URL url = new URL(factoryURL);
	URLConnection conn = url.openConnection();

	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer html = new StringBuffer();

	while ((inputLine = in.readLine()) != null) {
		html.append(inputLine);
	}
	in.close();

	return html.toString();
}

public String getLink(JsonObject factoryObj, String rel) {
    JsonArray linksArr = factoryObj.get("links").getAsJsonArray();
    for (int i = 0; i < linksArr.size(); i++) {
        JsonObject linkObj = linksArr.get(i).getAsJsonObject();
        if (rel.equals(linkObj.get("rel").getAsString())) {
            return linkObj.get("href").getAsString();
        }
    }
    
    return null;
}

%>

<%

String _title = "";
String _description = "";
String _image_url = "";
String _create_project_url = "";

try {
    String factoryURL = request.getScheme() + "://" + request.getServerName() +
        (request.getServerPort() == 80 ? "" : ":" + request.getServerPort()) +
        "/api/factory/" + request.getParameter("factory");
    
    String jsonText = getFactoryJSON(factoryURL);
    
    Gson g = new Gson();
    JsonParser parser = new JsonParser();
    JsonObject json = parser.parse(jsonText).getAsJsonObject();    
    
    _title = json.get("projectattributes").getAsJsonObject().get("pname").getAsString() + " - Codenvy";
    _description = json.get("description").getAsString();
    
    _image_url = getLink(json, "image");
    if (_image_url == null) {
        _image_url = request.getScheme() + "://" + request.getServerName() +
            (request.getServerPort() == 80 ? "" : ":" + request.getServerPort()) +
            "/factory/resources/codenvy.png";
    }
    
    _create_project_url = getLink(json, "create-project");
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta property="og:title" content="<%=_title%>"/>
	<meta property="og:description" content="<%=_description%>"/>
	<meta property="og:image" content="<%=_image_url%>"/>	
</head>
<body></body>
<script>
  setTimeout(function() { window.location.href = "<%=_create_project_url%>"; }, 1);
</script>
</html>
<%    
} catch (Exception e) {
    response.sendError(500, e.getMessage());
}
%>
