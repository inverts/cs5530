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
		} else if(operation.equals("addMovie")) {
			String title = request.getParameter("params[title]");
			String rating = request.getParameter("params[rating]");
			String format = request.getParameter("params[format]"); 
			int copies;
			try {
				copies = Integer.parseInt(request.getParameter("params[copies]"));
			} catch (Exception e) {
				copies = 0;
				out.println("Copy amount wasn't understood. Adding 0 copies.");
			}
			if (title == null || rating == null || format == null) {
				out.println("Could not add movie, not enough information provided.");
			} else {
				out.println((cs5530.VideoStore.addMovie(title, rating, format, copies) ? "Movie added successfully." : "There was a problem adding your movie. Try again."));
			}
		} else if(operation.equals("searchMovies")) {
			String title = request.getParameter("params[title]");
			String rating = request.getParameter("params[rating]");
			String director = request.getParameter("params[director]"); 
			String actorsRaw = request.getParameter("params[actors]");
			String matchAll = request.getParameter("params[matchall]");
			if (title == null || rating == null || director == null || actorsRaw == null || matchAll == null) {
				out.println("Not enough search data provided.");
			}
			String[] actors = actorsRaw.split(",");
			boolean match = (matchAll.equals("true") || matchAll.equals("yes") || matchAll.equals("1"));
					
			cs5530.VideoStore.DataSet result = cs5530.VideoStore.searchMovies(title, rating, director, actors, match);
			if (result==null) {
				out.println("There was a problem finding your movies. Try again.");
			} else {
				out.println(result.toString());
			}
		} else if(operation.equals("addVideosToInventory")) {
			//addVideosToInventory(int videoID, int copies)
		} else if(operation.equals("addVideoReview")) {
			//addVideoReview(String username, int videoID, int score, String review)
		} else if(operation.equals("declareTrust")) {
			//declareTrust(String truster, String trustee, boolean trusts)
		} else if(operation.equals("orderVideos")) {
			//orderVideos(String username, Integer[] videoIDs, Integer[] copies)
		} else if(operation.equals("getVideoSuggestions")) {
			//getVideoSuggestions(int videoID)
		}
	}
%>