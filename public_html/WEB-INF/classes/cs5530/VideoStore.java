package cs5530;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Arrays;
import org.apache.commons.codec.binary.Base64;
import org.jgrapht.alg.*;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.DefaultEdge;


/**
 * Offers a set of operations you can perform on a video store.
 * A complete ER diagram and relational model of the video store database
 * is publicly available at <a href="https://cacoo.com/diagrams/WywUGHhWVhumW0E0#049C2">cacoo.com/diagrams/WywUGHhWVhumW0E0#049C2</a>.
 * 
 * 
 * @author Dave Wong
 *
 */
public class VideoStore {
	
	//Database credentials.
	private static final String DB_TYPE = "jdbc:mysql://";
	private static final String DB_HOST = "198.143.132.50";
	private static final String DB_PORT = "3306";
	private static final String DB_USER = "davidkai_cs5530";
	private static final String DB_PASS = "vJX@p&*kh*5H";
	private static final String DB_SCHEMA = "davidkai_cs5530";
	
	private static boolean DEBUG_MODE = false;
	
	/**
	 * Turns debug mode on/off;
	 */
	public static boolean toggleDebugMode() {
		DEBUG_MODE = !DEBUG_MODE;
		return DEBUG_MODE;
	}
	
	/**
	 * Makes a query or operation on the database.
	 * 
	 * @param queryString
	 * @param params
	 * @return A list of HashMaps (rows) mapping column names to values, or <code>null</code> if there was an error with the connection or statement.
	 */
	public static DataSet query(String statement, Object[] params) {
		Connection c;
		try {
			//Build the connection string and attempt to connect to the database.
			c = DriverManager.getConnection(DB_TYPE + DB_HOST + ":" + DB_PORT + "/" + DB_SCHEMA, DB_USER, DB_PASS);
			/*if (DEBUG_MODE)
				UserInterface.out("Connected successfully.");
			if (DEBUG_MODE)
				UserInterface.out("Unprepared statement: " + statement);
			if (DEBUG_MODE)
				UserInterface.out("Parameters: " + Arrays.toString(params));
			*/
			//Prevent injection attacks.
			 PreparedStatement preparedQuery = c.prepareStatement(statement);
			 if (params != null) 
				for(int i=0; i<params.length; i++) 
					preparedQuery.setObject(i+1, params[i]);
			 
			// This is what we will end up returning
			DataSet output;
			
			//Adjust for "updating" statements vs. "query" statements.
			if (statement.startsWith("INSERT") || statement.startsWith("UPDATE") || statement.startsWith("DELETE")) {
				int updateCount = preparedQuery.executeUpdate();
				
				//Obtain the last insert ID if it exists. Note that if no ID was created, it will return whatever last one was.
				ResultSet lastResult = preparedQuery.executeQuery("SELECT LAST_INSERT_ID();"); //dataSet has already extracted our last query's information, fyi.
				lastResult.next();
				int lastInsertID = lastResult.getInt(1);
				lastResult.close();
				output = new DataSet(null, updateCount, lastInsertID);
				
			} else {
				ResultSet results = preparedQuery.executeQuery();
				ResultSetMetaData metadata = results.getMetaData();
				int columns = metadata.getColumnCount();
				
				ArrayList<HashMap<String, Object>> dataSet = new ArrayList<HashMap<String, Object>>();
				while(results.next()) {
					HashMap<String, Object> row = new HashMap<String, Object>();
					//Collect data for each column.
					for(int i = 1; i<=columns; i++) {
						//UserInterface.out(metadata.getColumnName(i) + ": " + results.getString(i));
						row.put(metadata.getColumnLabel(i), results.getObject(i));
					}
					dataSet.add(row); //Add the row to the whole set.
				}
				output = new DataSet(dataSet, columns, -1);
				results.close();
			}
/*
			if (DEBUG_MODE)
				UserInterface.out("Rows returned: " + output.getRowCount());
			if (DEBUG_MODE)
				UserInterface.out("Rows updated: " + output.getUpdateCount());
			*/
			preparedQuery.close();
			c.close();
			
			return output;
			
		} catch (Exception e) {
			if (DEBUG_MODE)
				e.printStackTrace();
			return null;
		}
		
	}
	
	
	/**
	 * Checks a username and password in the database.
	 * @param username
	 * @param password
	 * @return
	 */
	public static boolean tryLogin(String username, String password) {
		String query = "SELECT passwordHash, passwordSalt FROM Users WHERE username=?;";
		Object[] params = {username};
		DataSet ds = VideoStore.query(query, params);
		if (ds==null || ds.getRowCount() < 1)
			return false;
		String passwordHash;
		String passwordSalt;
		passwordHash = ds.getCellData("passwordHash").toString();
		passwordSalt = ds.getCellData("passwordSalt").toString();
		try {
		return PasswordTools.check(password, passwordHash, passwordSalt);
		} catch (Exception e) {
			return false;	//There was a problem hashing passwords.
		}
	}
	
	
	/**
	 * Registers a new user in the system.
	 * 
	 * @param username
	 * @param password
	 * @param fullName
	 * @param phone
	 * @param address
	 * @param creditCard
	 * @return A boolean indicating success of registration.
	 */
	public static boolean registerUser(String username, String password, String fullName, String phone, String address, String creditCard) {
		if (username==null || password==null || fullName==null)
			return false;	//You cannot have any of these be null. But the rest are okay.
		String passwordSalt;
		String passwordHash;
		try {
			passwordSalt = PasswordTools.getRandomSalt();
			passwordHash = PasswordTools.hash(password, passwordSalt);
		} catch (Exception e) {
			return false;	//There was an error generating hashes.
		}
		
		String query = "INSERT INTO Users (username, passwordHash, passwordSalt, fullName, phone, address, creditCard) VALUES (?, ?, ?, ?, ?, ?, ?);";
		Object[] params = {username, passwordHash, passwordSalt, fullName, phone, address, creditCard};
		DataSet ds = VideoStore.query(query, params);
		
		return ds!=null && ds.updateSuccessful();
		/*
		 * Reasons why this might fail (non-typical):
		 * -Some fields have data that is larger than their types allow for
		 * -The username already exists
		 */
	}
	
	public static String getCompleteUserData(String username) {
		String output = "";
		output += "General information:\n" + getUserData(username) + "\n";
		output += "Order history:\n" + getAllUserOrderData(username) + "\n";
		output += "Trust information:\n" + getUserTrustData(username) + "\n";
		output += "Review history:\n" + getUserReviewHistory(username) + "\n";
		//output += "Reviews ranked by usefulness:\n" + getUserReviewHistory(username) + "\n";
		return output;
	}

	/**
	 * Gets all user data.
	 * 
	 * @param username
	 * @return
	 */
	private static String getUserData(String username) {
		String query = "SELECT * FROM Users WHERE username=?";
		Object[] params = {username};
		DataSet ds = VideoStore.query(query, params);
		if (ds==null)
			return "Could not retrieve user data.";
		return ds.rowToString(ds.getRowData(0));
	}
	
	
	/**
	 * Gets all user order data.
	 * 
	 * @param username
	 * @return
	 */
	private static String getAllUserOrderData(String username) {
		String query = "SELECT title, v.videoID, COUNT(pc.copyID) AS copies, checkoutDate FROM checkedOut co, Videos v, PhysicalCopies pc WHERE " +
				"co.username=? AND pc.videoID=v.videoID AND pc.copyID=co.copyID GROUP BY v.videoID";
		Object[] params = {username};
		DataSet ds = VideoStore.query(query, params);
		if (ds==null)
			return "Could not retrieve user order data.";
		if (ds.getRowCount() < 1)
			return "This user has not ordered anything yet.";
		return ds.toString();
	}

	private static String getUserTrustData(String username) {
		String query = "SELECT trustee, trusts FROM Trusts WHERE truster=?";
		Object[] params = {username};
		DataSet ds = VideoStore.query(query, params);
		if (ds==null)
			return "Could not retrieve trust data.";
		if (ds.getRowCount() < 1)
			return "This user has not declared any trust yet.";
		return ds.toString();
	}

	/**
	 * Gets all the reviews this user has ever submitted.
	 * 
	 * @param username
	 * @return
	 */
	private static String getUserReviewHistory(String username) {
		String query = "SELECT v.title, v.videoID, r.date, r.score, r.review FROM Reviews r, Videos v WHERE r.videoID=v.videoID AND r.author=?";
		Object[] params = {username};
		DataSet ds = VideoStore.query(query, params);
		if (ds==null)
			return "Could not retrieve review data.";
		if (ds.getRowCount() < 1)
			return "This user has not reviewed any videos yet.";
		return ds.toString();
	}
	
	/**
	 * Registers a new movie in the database and adds a number of copies to our collection.
	 * 
	 * @param title
	 * @param rating
	 * @param format
	 * @param copies
	 * @return
	 */
	public static boolean addMovie(String title, String rating, String format, int copies) {
		String query = "INSERT INTO Videos (title, rating, format) VALUES (?, ?, ?);";
		Object[] params = {title, rating, format};
		DataSet ds = VideoStore.query(query, params);
		 
		if (ds == null || !ds.updateSuccessful())
			return false;
		
		int videoID = ds.getLastInsertID(); //This was obtained WHILE performing the last query.
		for (int i=0; i<copies; i++) {
			query = "INSERT INTO PhysicalCopies (videoID) VALUES (?);";
			ds = VideoStore.query(query, new Object[] {videoID});
		}
		//We could technically put all the statements into one long query then call it.
		
		return (ds != null && ds.updateSuccessful());
	}
	
	
	/**
	 * Searches the database for a movie or set of movies that match the search criteria.
	 * 
	 * @param title The title of the movie (optional)
	 * @param rating The movie rating (optional)
	 * @param director The director of the movie (optional)
	 * @param actors An array of actor names (optional)
	 * @param MatchAll A boolean indicating whether the search should match ALL of the provided details (true), or just one (false).
	 * @return	Returns a DataSet containing all the movies that match the specified criteria.
	 */
	public static DataSet searchMovies(String title, String rating, String director, String[] actors, boolean MatchAll) {
		ArrayList<Object> paramsList = new ArrayList<Object>();
		title = title.trim();
		rating = rating.trim();
		director = director.trim();
		if (title.isEmpty())
			title = null;
		if (rating.isEmpty())
			rating = null;
		if (director.isEmpty())
			director = null;
		
		String query = "SELECT * FROM Videos v WHERE ";
		if (title!=null) {
			query += "title LIKE (?) ";
			paramsList.add("%" + title + "%");
		}
		if (rating!=null) {
			query += (MatchAll ? "AND" : "OR") + " rating=? ";
			paramsList.add(rating);
		}
		if (director!=null) {
			query +=  (MatchAll ? "AND" : "OR") + " EXISTS(SELECT * FROM hasDirector hd, Directors d WHERE hd.videoID=v.videoID AND hd.directorID=d.directorID AND d.directorName LIKE ?) ";
			paramsList.add("%" + director + "%");
		}
		if (actors!=null && actors.length!=0) {
			query +=  (MatchAll ? "AND" : "OR");
			query += " EXISTS(SELECT * FROM hasActor ha, Actors a WHERE ha.videoID=v.videoID AND ha.actorID=a.actorID AND (";
			for(String actor : actors) {
				query += "a.actorName=? OR ";
				paramsList.add(actor);
			}
			query += "'0'='1') )"; //To keep the last "OR" from breaking a valid statement, and closing the parentheses.
		}
		
		if (query.equals("SELECT * FROM Videos v WHERE "))
			query = "SELECT * FROM Videos v";
		
		Object[] params = paramsList.toArray();
		DataSet ds = VideoStore.query(query, params);
		return ds;
	}
	
	
	/**
	 * 
	 * @param videoID
	 * @param copies
	 * @return
	 */
	public static boolean addVideosToInventory(int videoID, int copies) {
		String query;
		DataSet ds = null;
		for (int i=0; i<copies; i++) {
			query = "INSERT INTO PhysicalCopies (videoID) VALUES (?);";
			ds = VideoStore.query(query, new Object[] {videoID});
		}
		return (ds!=null && ds.updateSuccessful());
	}
	
	/**
	 * Adds a review of a video to the database.
	 * 
	 * @param username The author leaving the review
	 * @param videoID The id of the video being reviewed
	 * @param score The score the user gave this video
	 * @param review The actual review text. This may be NULL, it is optional.
	 * @return A boolean indicating success
	 */
	public static boolean addVideoReview(String username, int videoID, int score, String review) {
		String query = "INSERT INTO Reviews (author, videoID, score, review, date) VALUES (?, ?, ?, ?, ?);";
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String time = sdf.format(date);
		
		Object[] params = {username, videoID, score, review, time};
		DataSet ds = VideoStore.query(query, params);
		
		return (ds!=null);
	}
	
	/**
	 * 
	 * @param truster
	 * @param trustee
	 * @param trusts
	 * @return
	 */
	public static boolean declareTrust(String truster, String trustee, boolean trusts) {
		String query = "INSERT INTO Trusts (truster, trustee, trusts) VALUES (?, ?, ?);";
		Object[] params = {truster, trustee, trusts};
		DataSet ds = VideoStore.query(query, params);
		//Fails if the record already exists.
		return (ds!=null & ds.updateSuccessful());
	}
	
	
	/**
	 * This will need to be altered if we need to be able to check in videos as well.
	 * 
	 * @param videoIDs
	 * @param copies
	 * @return The amount the order will cost, in pennies. If there is a problem, -1.
	 */
	public static int orderVideos(String username, Integer[] videoIDs, Integer[] copies) {
		int uniqueVideos = videoIDs.length;
		if (uniqueVideos!=copies.length)
			return -1; //Not okay.
		DataSet[] datasets = new DataSet[uniqueVideos];
		for (int i=0; i<uniqueVideos; i++) {
			//Gets all physical copies of this video, and then removes the ones that are checked out.
			String query = "SELECT DISTINCT copyID, price FROM Videos v, PhysicalCopies pc WHERE v.videoID=? AND v.videoID=pc.videoID AND NOT EXISTS " +
					"(SELECT * FROM checkedOut co WHERE pc.copyID=co.copyID)"; 
			datasets[i] = VideoStore.query(query, new Object[] {videoIDs[i]});
			if (datasets[i]==null)
				return -1; //There was a problem executing the query.
			if (datasets[i].getRowCount() < copies[i])
				return -1; //There are not enough copies in inventory to order.
		}
		
		int total = 0;
		for (int i=0; i<uniqueVideos; i++) {
			total += Integer.parseInt(datasets[i].getCellData("price").toString()) * copies[i]; //They will all have the same price
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String time = sdf.format(date);
			for (int j=0; j<copies[i]; j++){
				String query = "INSERT INTO checkedOut (username, copyID, checkoutDate) VALUES (?, ?, ?);";
				Object[] params = new Object[] {username, datasets[i].getCellData(j, "copyID"), time};
				DataSet ds = VideoStore.query(query, params);
				if (ds==null || !ds.updateSuccessful()) //<-- problem is here
					return -1; //Could not make the actual order.
			}
		}
		
		
		
		return total; //Total price of order, in pennies.
		
	}
	
	
	/**
	 * Given a particular video, produces a list of videos that are suggested/related.
	 * 
	 * @param videoID
	 * @return
	 */
	public static String getVideoSuggestions(int videoID) {
		String query = "SELECT v.title, suggestions.videoID FROM (SELECT co2.videoID FROM (SELECT DISTINCT username, videoID FROM checkedOut co, PhysicalCopies pc WHERE co.copyID=pc.copyID GROUP BY pc.videoID) co1, " +
				"(SELECT DISTINCT username, videoID FROM checkedOut co, PhysicalCopies pc WHERE co.copyID=pc.copyID GROUP BY pc.videoID) co2 " +
				"WHERE co1.username=co2.username AND co1.videoID<>co2.videoID AND co1.videoID=?) suggestions, Videos v WHERE suggestions.videoID=v.videoID;";	//Get all actors.
		Object[] params = {videoID};
		DataSet ds = VideoStore.query(query, params);
		if (ds==null)
			return "No video suggestions available.";
		return ds.toString();
		
	}
	
	
	/**
	 * Builds a report of statistical data on popularity of various movies, directors, and actors.
	 * 
	 * @return
	 */
	public static String getStatisticalData() {
		//TODO: Check for null query results.
		String output = "";
		output += "Most popular movies: \n" + getMostPopularMovies().toString() + "\n";
		output += "Most popular directors: \n" + getMostPopularDirectors().toString() + "\n";
		output += "Most popular actors: \n" + getMostPopularActors().toString() + "\n";
		return output;
	}
	
	/**
	 * 
	 * @param quantity
	 * @return
	 */
	public static DataSet getMostPopularMovies() {
		String query = "SELECT pc.videoID, COUNT(pc.copyID) AS totalSales FROM checkedOut co, PhysicalCopies pc WHERE co.copyID=pc.copyID GROUP BY pc.videoID ORDER BY totalSales DESC";
		Object[] params = {};
		DataSet ds = VideoStore.query(query, params);
		return ds;
	}
	

	/**
	 * Gets the most popular directors as calculated by the average review score of movies they have directed.
	 * @return
	 */
	public static DataSet getMostPopularDirectors() {
		String query = "SELECT directorID, AVG(r.score) AS avgScore FROM hasDirector hd, Reviews r WHERE hd.videoID=r.videoID GROUP BY hd.directorID ORDER BY avgScore DESC";
		Object[] params = {};
		DataSet ds = VideoStore.query(query, params);
		return ds;
	}
	
	
	/**
	 * Gets the most popular actors as calculated by the average review score of movies they have acted in.
	 * @return
	 */
	public static DataSet getMostPopularActors() {
		String query = "SELECT actorID, AVG(r.score) AS avgScore FROM hasActor ha, Reviews r WHERE ha.videoID=r.videoID GROUP BY ha.actorID ORDER BY avgScore DESC";
		Object[] params = {};
		DataSet ds = VideoStore.query(query, params);
		return ds;
	}
	
	/**
	 * Given two actor names, determine their 'degree of separation', defined 
	 * as follows: Two actors 'A' and 'B' are 1-degree away if they played in at least one movie together; they are
	 * 2-degrees away if there exists an actor 'C' who is 1-degree away from each of 'A' and 'B'; and so on.
	 * 
	 * @param actor1
	 * @param actor2
	 * @return
	 */
	public static int degreesOfSeparation(Integer actor1, Integer actor2) {
		SimpleGraph<Integer, DefaultEdge> sg = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
		String query = "SELECT * FROM Actors;";	//Get all actors.
		Object[] emptyparams = {};
		DataSet ds = VideoStore.query(query, emptyparams);
		
		//Get a list of pairs of actors that share movies together.
		String query1 = "SELECT a.videoID AS videoID, a.actorID AS actor1, b.actorID AS actor2 FROM hasActor a, hasActor b WHERE a.videoID=b.videoID AND a.actorID<>b.actorID;";	
		DataSet ds1 = VideoStore.query(query1, emptyparams);

		if(ds==null || ds1==null)
			return -1;
		
		// TODO: Check to see if actors even exist.
		
		//First, add all the vertices to the graph. We iterate through the query selecting all of the actors.
		for (int i=0; i<ds.getRowCount(); i++) 
			sg.addVertex((Integer)ds.getCellData(i, "actorID"));
		
		//Next, add the edges
		for (int i=0; i<ds1.getRowCount(); i++) 
			sg.addEdge((Integer)ds1.getCellData(i, "actor1"), (Integer)ds1.getCellData(i, "actor2"));
		
		DijkstraShortestPath<Integer, DefaultEdge> dsp = new DijkstraShortestPath<Integer, DefaultEdge>(sg, actor1, actor2); //Possible equals issue
		double distance = dsp.getPathLength();
		
		if (distance==Double.POSITIVE_INFINITY)	//No path exists.
			return 0;
		else if (distance==0)
			return 1; //By the spec definition, "Two actors 'A' and 'B' are 1-degree away if they played in at least one movie together".
		return (int) distance;
	}
	

	/**
	 * Adds an actor to our list of actors that can be added to movies.
	 * 
	 * @param actorName
	 * @return
	 */
	public static DataSet addActor(String actorName) {
		String query = "INSERT INTO Actors (actorName) VALUES (?);";
		DataSet ds = VideoStore.query(query, new Object[] {actorName});
		return ds;
	}
	
	
	/**
	 * Declares that an actor is part of a movie.
	 * 
	 * @param actorID
	 * @param movieID
	 * @return
	 */
	public static boolean addActorToMovie(int actorID, int movieID) {
		String query = "INSERT INTO hasActor (actorID, videoID) VALUES (?, ?);";
		DataSet ds = VideoStore.query(query, new Object[] {actorID, movieID});
		return (ds!=null);
	}
	
	/**
	 * Adds an director to our list of actors that can be added to movies.
	 * 
	 * @param directorName
	 * @return
	 */
	public static DataSet addDirector(String directorName) {
		String query = "INSERT INTO Directors (directorName) VALUES (?);";
		DataSet ds = VideoStore.query(query, new Object[] {directorName});
		return ds;
	}
	
	
	/**
	 * Declares that an director directed a movie.
	 * 
	 * @param directorID
	 * @param movieID
	 * @return
	 */
	public static boolean addDirectorToMovie(int directorID, int movieID) {
		String query = "INSERT INTO hasDirector (directorID, videoID) VALUES (?, ?);";
		DataSet ds = VideoStore.query(query, new Object[] {directorID, movieID});
		return (ds!=null);
	}
	
	
	/**
	 * Determines whether an actor name exists already.
	 * It is okay to have two actors with the same name, but this can be useful to
	 * inform the user in case they are accidentally adding a duplicate.
	 * 
	 * @param actorName
	 * @return
	 */
	public static boolean actorNameExists(String actorName) {
		String query = "SELECT * FROM Actors WHERE actorName=?;";
		DataSet ds = VideoStore.query(query, new Object[] {actorName});
		return (ds != null && ds.getRowCount() > 0);
	}	
	
	
	/**
	 * Determines whether an director name exists already.
	 * It is okay to have two directors with the same name, but this can be useful to
	 * inform the user in case they are accidentally adding a duplicate.
	 * 
	 * @param actorName
	 * @return
	 */
	public static boolean directorNameExists(String directorName) {
		String query = "SELECT * FROM Directors WHERE directorName=?;";
		DataSet ds = VideoStore.query(query, new Object[] {directorName});
		return (ds != null && ds.getRowCount() > 0);
	}
	
	/**
	 * Checks whether or not a username already exists in the database.
	 * 
	 * @param username
	 * @return A boolean indicating whether the usernmae exists or not.
	 */
	public static boolean usernameExists (String username) {
		String query = "SELECT * FROM Users WHERE username=?";
		Object[] params = {username};
		DataSet ds = VideoStore.query(query, params);
		return (ds != null && ds.getRowCount() > 0);
	}
	
	
	//-----------------------MEMBER CLASSES---------------------------------
	
	
	/**
	 * Represents a data set - a table of values from a database result.
	 * @author Dave Wong
	 *
	 */
	public static class DataSet {
		private ArrayList<HashMap<String, Object>> dataSet;
		private int updateCount;
		int lastInsertID;
		
		public DataSet(ArrayList<HashMap<String, Object>> dataSet, int updateCount, int lastInsertID) {
			this.dataSet = (dataSet!=null) ? dataSet : new ArrayList<HashMap<String, Object>>();
			this.updateCount = updateCount;
			this.lastInsertID = lastInsertID;
		}


		/**
		 * Gets the data for the column specified at the specified row of the data set.
		 * @param column
		 * @return
		 */
		public Object getCellData(int row, String column) {
			return dataSet.get(row).get(column);
		}
		
		
		/**
		 * Gets the data for the column specified at the first row of the data set.
		 * @param column
		 * @return
		 */
		public Object getCellData(String column) {
			return dataSet.get(0).get(column);
		}
		

		/**
		 * 
		 * @return
		 */
		public HashMap<String, Object> getRowData(int row) {
			return dataSet.get(row);
		}
		
		/**
		 * Gets the number of rows in this result.
		 * @return
		 */
		public int getRowCount() {
			return dataSet.size();
		}

		/**
		 * Gets the number of successful updated rows.
		 * @return
		 */
		public int getUpdateCount() {
			return updateCount;
		}

		/**
		 * Gets the last insert ID after this call.
		 * @return
		 */
		public int getLastInsertID() {
			return lastInsertID;
		}
		
		
		public boolean updateSuccessful() {
			return updateCount > 0;
		}
		
		/**
		 * Creates a string representation of an ArrayList<HashMap<String, Object>>. (essentially a table of values)
		 */
		@Override
		public String toString() {
			if (this.dataSet==null)
				return null;
			String output = "";
			for (int i=0; i<this.dataSet.size(); i++) {
				output += "Result #" + i + ": \n";
				HashMap<String, Object> row = this.dataSet.get(i);
				for (Entry<String, Object> cell : row.entrySet()) {
					output += "\t" + cell.getKey() + ": " + (cell.getValue() != null ? cell.getValue().toString() : "null") + "\n";
				}
			}
			return output;
		}
		
		
		/**
		 * Outputs a nicely formatted string illustrating the field data in a particular row.
		 * 
		 * @param row
		 * @return
		 */
		public String rowToString(HashMap<String, Object> row) {
			Iterator<Entry<String, Object>> iterator = row.entrySet().iterator();
			String output = "";
			while (iterator.hasNext()) {
				Entry<String, Object> cell = iterator.next();
				output += cell.getKey() + ": " + (cell.getValue()==null ? "(no info)" : cell.getValue().toString()) + "\n";
			}
			return output;
		}
	
	}

	
	
/**
 * The following is a mildly checked and altered version of code deriving from Martin Konicek's
 * answer on the following StackOverflow forum question: http://stackoverflow.com/questions/2860943/suggestions-for-library-to-hash-passwords-in-java
 * 
 * Since password hashing was not a requirement for this checkpoint of the project (as explicitly told by the professor), 
 * I figured I could hold off on writing my own password hashing code for now.
 * 
 * @author Martin Konicek, edited by Dave Wong
 * 
 *  
 */
	private static class PasswordTools {
	    private static final int iterations = 10*1024;
	    private static final int saltLen = 32;
	    private static final int desiredKeyLen = 256;
	
	    /**
	     * Generates a random salt.
	     * @return
	     * @throws Exception
	     */
	    public static String getRandomSalt() throws Exception {
	        return Base64.encodeBase64String(SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen));
	    }
	
	    /** Checks whether given plaintext password corresponds 
	        to a stored salted hash of the password. */
	    public static boolean check(String password, String passwordHash, String passwordSalt) throws Exception {
	        String hashOfInput = hash(password, passwordSalt);
	        return hashOfInput.equals(passwordHash);
	    }
	
	    // using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
	    // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
	    private static String hash(String password, String salt) throws Exception {
	        if (password == null || password.length() == 0)
	            throw new IllegalArgumentException("Empty passwords are not supported.");
	        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        SecretKey key = f.generateSecret(new PBEKeySpec(
	            password.toCharArray(), Base64.decodeBase64(salt), iterations, desiredKeyLen)
	        );
	        return Base64.encodeBase64String(key.getEncoded());
	    }
	}
	
	
}