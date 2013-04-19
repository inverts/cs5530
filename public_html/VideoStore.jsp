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
			} else {
			String[] actors = actorsRaw.split(",");
			boolean match = (matchAll.equals("true") || matchAll.equals("yes") || matchAll.equals("1"));
					
			cs5530.VideoStore.DataSet result = cs5530.VideoStore.searchMovies(title, rating, director, actors, match);
			if (result==null) {
				out.println("There was a problem finding your movies. Try again.");
			} else {
				out.println(result.toString());
			}
			}
		} else if(operation.equals("addVideosToInventory")) {
			String videoIDRaw = request.getParameter("params[videoID]");
			String copiesRaw = request.getParameter("params[copies]");
			if (videoIDRaw == null || copiesRaw == null) {
				out.println("Sorry, not enough information provided.");
			} else {
			int copies;
			int videoID;
			try {
				copies = Integer.parseInt(copiesRaw);
				videoID = Integer.parseInt(videoIDRaw );
			} catch (Exception e) {
				out.println("Invalid video ID or copy amount. Please submit numbers.");
				return;
			}
			if(addVideosToInventory(videoID, copies)) {
				out.println("Videos successfully added.");
			} else {
				out.println("Sorry, your videos could not be added. Please make sure you have the correct video ID.");
			}
			}
		} else if(operation.equals("addVideoReview")) {
			String username = request.getParameter("params[reviewUsername]");
			String videoIDRaw = request.getParameter("params[reviewVideoID]");
			String scoreRaw = request.getParameter("params[reviewScore]");
			String review = request.getParameter("params[review]");
			if (videoIDRaw == null || username == null || scoreRaw == null || review == null) {
				out.println("Sorry, not enough information provided.");
			} else {
			int videoID;
			int score;
			try {
				score = Integer.parseInt(scoreRaw);
				videoID = Integer.parseInt(videoIDRaw );
			} catch (Exception e) {
				out.println("Invalid video ID or score. Please submit numbers.");
				return;
			}

			if(addVideoReview(username, videoID, score, String review)) {
				out.println("Videos successfully added.");
			} else {
				out.println("Sorry, your videos could not be added. Please make sure you have the correct video ID.");
			}
			}
		} else if(operation.equals("declareTrust")) {
			//declareTrust(String truster, String trustee, boolean trusts)
		} else if(operation.equals("orderVideos")) {
			//orderVideos(String username, Integer[] videoIDs, Integer[] copies)
		} else if(operation.equals("getVideoSuggestions")) {
			//getVideoSuggestions(int videoID)
		}
	}
%>