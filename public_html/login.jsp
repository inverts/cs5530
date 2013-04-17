<%@ page language="java" import="cs5530.*" %>
<!DOCTYPE HTML>
<html>
<head>
<title>Login</title>
</head>
<body>
<% 
	String attributeValue = request.getParameter("param");
out.println(attributeValue);
%>
<h1>Login</h1>
<form type="post" action="dashboard.jsp">
<input type="text" name="username" /><br />
<input type="password" name="password" /><br />
</form>
<%
String s = cs5530.VideoStore.getUserData("dave");
out.println(s);
%>
</body>
</html>