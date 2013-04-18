<%@ page language="java" import="cs5530.*" %>
<% 
	boolean tryLogin = (request.getParameter("login")=="1");
	boolean tryRegister = (request.getParameter("register")=="1");
	String username = request.getParameter("username");
	String password = request.getParameter("password");
	boolean loginSuccess = (username!=null && password!=null && cs5530.VideoStore.tryLogin(username, password));
	boolean registerSuccess = false;
	if (loginSuccess) {
		//First, save session variable
		session.setAttribute("username", username);
		%>
		<jsp:forward page="dashboard.jsp" />
		<%
	} else {
		String registerfullname = request.getParameter("registerfullname");
		String registerusername = request.getParameter("registerusername");
		String registerpassword = request.getParameter("registerpassword");
		registerSuccess = (registerfullname!=null && registerusername!=null && registerpassword!=null && cs5530.VideoStore.registerUser( registerusername, registerpassword, registerfullname, null, null, null));
	}
%>
<!DOCTYPE HTML>
<html>
<head>
<title>Login</title>
<link href="style.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div id="main">
<h1>Login</h1>
<%
if(!loginSuccess && tryLogin) {
	out.println("Sorry, your login failed.");
} else if(registerSuccess) {
	out.println("You have successfully registered! Please log in below.");
} else if(!registerSuccess && tryRegister) {
	out.println("Sorry, your registration failed. Please provide all details and a unique username.");
}
	
%>
<form type="post" action="?login=1">
Username:
<input type="text" name="username" /><br />
Password
<input type="password" name="password" /><br />
<input type="submit" value="Login" />
</form>

<h1>Register</h1>
<form type="post" action="?register=1">
Name:
<input type="text" name="registerfullname" /><br />
Username:
<input type="text" name="registerusername" /><br />
Password:
<input type="text" name="registerpassword" /><br />
<input type="submit" value="Register" />
</form>
</div>
</body>
</html>