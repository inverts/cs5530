<%@ page language="java" import="cs5530.*" %>
<%
	//Get the operation name desired.
	String operation = request.getParameter("operation").toString();

	if (operation==null) {
		out.println("No operation directive");
	} else {
	out.println("Operation: " + operation);
	if (operation.equals("getCompleteUserData")) {
		String username = request.getParameter("params[username]");
		if (username == null) {
			out.println("Could not get user data, no username provided.");
		} else {
			out.println(cs5530.VideoStore.getCompleteUserData(username));
		}
	}
	}
%>