package proj.servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.Subscription;
import proj.models.User;


/**
 * Servlet implementation class SignupServlet
 */
@WebServlet("/SignupServlet")
public class SignupServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SignupServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
    		Gson gson = new Gson();
    		Connection conn = AppVariables.db.getConnection();

    		HttpSession session = request.getSession();
    		User user = null;
    		
    		//reading user details from the request
    		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
    		StringBuilder jsonDetails =new StringBuilder();
    		String line;
    		while ((line = br.readLine()) !=null){
    			jsonDetails.append(line);
    		}
    		user = gson.fromJson(jsonDetails.toString(),User.class);
    		
    		boolean badUserNameIndication = false;
    		boolean badNicknameIndication = false;
    		
    		int userID = 0;
    		
    		if ((user.getUserName() !=null) && (user.getPassword() != null) && (user.getUserNickname() != null) &&
    				(user.getUserDescription() != null) ){
    			PreparedStatement stmt;
    			try {
    				//first checking that there is no other user with the same username
    				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_USERNAME_STMT);
    				stmt.setString(1, user.getUserName());
    				
    				ResultSet rs = stmt.executeQuery();
    				if (rs.next()){
    					badUserNameIndication = true;
    				}
    				rs.close();
    				stmt.close();
    				//checking that there is no other user with the same nickname
    				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
    				stmt.setString(1, user.getUserNickname());
    				
    				rs = stmt.executeQuery();
    				if (rs.next()){
    					badNicknameIndication = true;
    				}
    				rs.close();
    				stmt.close();
    			} catch (SQLException e) {
    				getServletContext().log("Error while querying for users", e);
    	    		response.sendError(500);//internal server error
    			}
    			//if username and nickname are valid carry on with sign up
    			if ((!badUserNameIndication)&&(!badNicknameIndication)){
    				try {
    					//updating users logged and lastlogged attributes
    					Timestamp time = new Timestamp(System.currentTimeMillis());
    					user.setIslogged(true);
    					user.setLastlogged(time);
	    				//inserting user into users table
	    				stmt = conn.prepareStatement(AppConstants.INSERT_USER_STMT);
	    				stmt.setString(1, user.getUserName());
	    				stmt.setString(2, user.getPassword());
	    				stmt.setString(3, user.getUserNickname());
	    				stmt.setString(4, user.getUserDescription());
	    				stmt.setString(5, user.getPhotoURL());
	    				stmt.setBoolean(6, true);
	    				stmt.setTimestamp(7, time);
	    				stmt.executeUpdate();
	    				conn.commit();
	    				stmt.close();
    				} catch (SQLException e) {
        				getServletContext().log("Error while inserting new user", e);
        	    		response.sendError(500);//internal server error
        			}
    				//every user is registered to the general channel
    				Subscription firstSubscription = new Subscription(user.getUserName(),AppConstants.GENERAL_CHANNEL,proj.models.Type.PUBLIC);
	    			//updating our list of users for general channel
    				ArrayList<User> currentList = AppVariables.usersByChannel.get(firstSubscription.getChannel());
    				if (currentList == null)
    					currentList = new ArrayList<User>();
    				currentList.add(user);
	    			AppVariables.usersByChannel.put(firstSubscription.getChannel(), currentList);
    				//entering the general channel subscription for this user
	    			try {
		    			stmt = conn.prepareStatement(AppConstants.INSERT_SUBSCRIPTIONS);
		    			stmt.setString(1, firstSubscription.getUsername());
		    			stmt.setString(2, firstSubscription.getChannel());
		    			stmt.executeUpdate();
		    			conn.commit();
		    			stmt.close();
	    				} catch (SQLException e) {
	        				getServletContext().log("Error while inserting new subscription", e);
	        	    		response.sendError(500);//internal server error
	        			}
	    			//setting session attribute - username for future actions
	    			session.setAttribute(AppConstants.USERNAME, user.getUserName());
	    				
	    				
    		}

    		conn.close();
    		
    		String userJsonResult = null;
    		PrintWriter writer = response.getWriter();
        	//convert from user to json
    		if (badUserNameIndication)
    			user = new User("Error username taken");
    		else
    			user = new User("Error nickname taken");
    		userJsonResult = gson.toJson(user);
			writer.println(userJsonResult);
        	writer.close();
        	//inserting new user to user files
        	if ((!badUserNameIndication)&&(!badNicknameIndication))
        		saveNewUserToFile(user);
        	conn.close();
    		}
    	} catch (SQLException e) {
    		getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
    	}

	}
	
	public void saveNewUserToFile(User newUser) throws IOException{
		Gson gson = new Gson();
		ServletContext cntx = this.getServletContext();
		InputStream is = cntx.getResourceAsStream(File.separator +
				AppConstants.USERS_FILE);
		//wrap input stream with a buffered reader to allow reading the file line by line
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder jsonFileContent = new StringBuilder();
		//read line by line from file
		String nextLine = null;
		while ((nextLine = br.readLine()) != null){
			jsonFileContent.append(nextLine);
		}

		//this is a require type definition by the Gson utility so Gson will 
		//understand what kind of object representation should the json file match
		Type type = new TypeToken<Collection<User>>(){}.getType();
		Collection<User> users = gson.fromJson(jsonFileContent.toString(), type);
		//close
		users.add(newUser);
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileOutputStream fooStream = null;
		try {
			fooStream = new FileOutputStream(AppConstants.USERS_FILE,false);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonString = gson.toJson(users);
		
		byte[] myBytes = jsonString.getBytes();
		try {
			fooStream.write(myBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fooStream.close();


		
	}

}
