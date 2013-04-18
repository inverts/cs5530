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
 	<h3>Get User Data</h3>
	Username:<input type="text" id="userDataUsername" value="<%=username%>" /><input type="button" value="Get Complete User Data" onclick="apiCall('getCompleteUserData', {username: document.getElementById('userDataUsername').value});" />
	
	<h3>Add New Movie</h3>
	Movie Title:<input type="text" id="movieTitle" value="" /><br />
	Movie Format:<input type="text" id="movieFormat" value="" /><br />
	Movie Rating:<input type="text" id="movieRating" value="" /><br />
	Copies to add:<input type="text" id="movieCopies" value="0" /><br />
	<input type="button" value="Add Movie" onclick="apiCall('addMovie', {title: document.getElementById('movieTitle').value, format: document.getElementById('movieFormat').value, rating: document.getElementById('movieRating').value, copies: document.getElementById('movieCopies').value});" />
	
	<h3>Search Movies</h3>
	Movie Title:<input type="text" id="searchTitle" value="" /><br />
	Movie Rating:<input type="text" id="searchRating" value="" /><br />
	Movie Director:<input type="text" id="searchDirector" value="" /><br />
	Movie Actors (comma-separated):<input type="text" id="searchActors" value="" /><br />
	Match All? (boolean):<input type="text" id="searchMatchAll" value="" /><br />
	<input type="button" value="Search Movies" onclick="apiCall('searchMovies', {title: document.getElementById('searchTitle').value, rating: document.getElementById('searchRating').value, director: document.getElementById('searchDirector').value, actors: document.getElementById('searchActors').value, matchall: document.getElementById('searchMatchAll').value});" />
	
</div>
<div id="output">
	<textarea id="outputarea">
	</textarea>
</div>
</div>
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js" ></script>
<script type="text/javascript">
/*
 * Makes an API call with the given operation and parameters (as an object).
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