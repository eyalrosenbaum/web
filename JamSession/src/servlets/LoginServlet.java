package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import JamSession.AppConstants;
import JamSession.AppVariables;
import models.User;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet(
		description = "Servlet to enable users to login to system", 
		urlPatterns = { 
				"/LoginServlet"
		})
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
    		
			Connection conn = AppVariables.db.getConnection();

    		User userResult = null;
    		String userName = "userName";
    		String password = "password";
    		boolean registered = false;
    		HttpSession session = request.getSession();
    		
    		//reading user details from the request
    		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
    		StringBuilder jsonDetails =new StringBuilder();
    		String line;
    		while ((line = br.readLine()) !=null){
    			jsonDetails.append(line);
    		}
    		
    		JsonParser parser = new JsonParser();
    		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();
    		
    		String userNameValue = jsonObject.get("userName").toString();
    		String passwordValue = jsonObject.get("password").toString();
    		
    		if ((userNameValue !=null) && (passwordValue != null)){
    			PreparedStatement stmt;
    			try {
    				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_USERNAME_AND_PASS_STMT);
    				stmt.setString(1, userNameValue);
    				stmt.setString(2, passwordValue);
    				
    				ResultSet rs = stmt.executeQuery();
    				Timestamp time = null;
    				//if there are results than user is registered to site and it is ok to proceed
    				if (rs.next()){
    					registered = true;
    					userResult = new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),
    							rs.getString(5));
    					userResult.setIslogged(true);
    					time = new Timestamp(System.currentTimeMillis());
    					userResult.setLastlogged(rs.getTimestamp(7));
    				}
    				rs.close();
    				stmt.close();
    				if (registered){
	    				//now we have to update users lastlogged and islogged fields
	    				stmt = conn.prepareStatement(AppConstants.UPDATE_LOGGED_USER_STMT);
	    				stmt.setTimestamp(1, time);
	    				stmt.setString(2, userResult.getUserNickname());
	    				stmt.executeUpdate();
	    				//we also enter the user to the general channel current users
	    				//updating our list of users for general channel
	    				ArrayList<User> currentList = AppVariables.usersByChannel.get(AppConstants.GENERAL_CHANNEL);
	    				currentList.add(userResult);
		    			AppVariables.usersByChannel.put(AppConstants.GENERAL_CHANNEL, currentList);
		    			conn.commit();
		    			stmt.close();
		    			//setting session attribute - username for future actions
	    				session.setAttribute(AppConstants.USERNAME, userResult.getUserName());
	    				
    				}
	    			} catch (SQLException e) {
	    				getServletContext().log("Error while querying for users", e);
	    	    		response.sendError(500);//internal server error
	    			}
    			}
    		

    		conn.close();
    		//send users details to client
			Gson gson = new Gson();
        	//convert from users collection to json
			String userJsonresult;
			if (userResult != null){
				userJsonresult = gson.toJson(userResult, User.class);
	        	PrintWriter writer = response.getWriter();
	        	writer.println(userJsonresult);
	        	writer.close();
	        	//setting session attribute - username for future actions
    			session.setAttribute(AppConstants.USERNAME, userResult.getUserName());
    			session.setAttribute(AppConstants.USERNICKNAME, userResult.getUserNickname());
			}
    		
    	} catch (SQLException e) {
    		getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
    	}

	}

}
