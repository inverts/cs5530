<%@ page language="java" import="cs5530.*" %>
<% 
	//First, get session variable
	String username = session.getAttribute("username").toString();
	//If there is no active session with a username, send them back to the login page.
	if (username==null) {
		%>
		<jsp:forward page="login.jsp" />
		<%
	}
%>
<!DOCTYPE HTML>
<html>
<head>
<title>Dashboard</title>
<link href="style.css" rel="stylesheet" type="text/css" />
</head>
<body>
<div id="main">
<div id="input">
	Username:<input type="text" id="userDataUsername" value="<%=username%>" /><input type="button" value="Get Complete User Data" onclick="apiCall('getCompleteUserData', {username: document.getElementById('userDataUsername').value});" />
</div>
<div id="output">
	<textarea id="outputarea">
	</textarea>
</div>
</div>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js" ></script>
<script type="text/javascript">
/*
 * Makes an api call with the given operation and parameters (as an object).
 */
function apiCall(operation, params) {
$.ajax({
  type: "POST",
  url: "VideoStore.jsp",
  data: {operation:operation, params:params},
  success: callback
});
}

/*
 * Takes a data string and prints it out to the output textarea.
 */
function callback(data) {
	$('#outputarea').html(data);
}
</script>
</body>
</html>