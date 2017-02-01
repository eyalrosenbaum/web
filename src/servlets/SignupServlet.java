package servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import JamSession.AppConstants;
import models.Channel;
import models.User;

/**
 * Servlet implementation class SignupServlet
 */
@WebServlet(
		description = "Servlet to enable users to login to system", 
		urlPatterns = { 
				"/SignupServlet"
		})
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
//		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
    		Gson gson = new Gson();
        	//obtain UserDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(
    				getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
    		Connection conn = ds.getConnection();

    		HttpSession session = request.getSession();
    		User user = null;
    		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
    		String jsonDetails = "";
    		if (br!=null){
    			jsonDetails = br.readLine();
    		}
    		user = gson.fromJson(jsonDetails,User.class);
    		
    		boolean badUserNameIndication = false;
    		
    		int userID = 0;
    		
    		if ((user.getUserName() !=null) && (user.getPassword() != null) && (user.getUserNickname() != null) &&
    				(user.getUserDescription() != null) ){
    			PreparedStatement stmt;
    			try {
    				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_USERNAME_STMT);
    				stmt.setString(1, user.getUserName());
    				
    				ResultSet rs = stmt.executeQuery();
    				if (rs.next()){
    					badUserNameIndication = true;
    				}
    				rs.close();
    				stmt.close();
    			} catch (SQLException e) {
    				getServletContext().log("Error while querying for users", e);
    	    		response.sendError(500);//internal server error
    			}

    			if (!badUserNameIndication){
    				//if we entered here it means that the username selected was legal and we can
    				//continue registration
    				try {
	    				//getting users ID
	    				stmt = conn.prepareStatement(AppConstants.SELECT_TOP_USERID_STMT);
	    				ResultSet rs = stmt.executeQuery();
	    				while (rs.next()){
	    					userID = rs.getInt(1);
	    				}
	    				//inserting user into users table
	    				stmt = conn.prepareStatement(AppConstants.INSERT_USER_STMT);
	    				stmt.setInt(1, userID);
	    				stmt.setString(2, user.getUserName());
	    				stmt.setString(3, user.getPassword());
	    				stmt.setString(4, user.getUserNickname());
	    				stmt.setString(5, user.getUserDescription());
	    				stmt.setString(6, user.getPhotoURL());
	    				stmt.executeUpdate();
    				} catch (SQLException e) {
        				getServletContext().log("Error while inserting new user", e);
        	    		response.sendError(500);//internal server error
        			}
	    				user.setId(userID);
	    				Channel theGeneralChannel = new Channel(models.Type.PUBLIC,AppConstants.GENERAL_CHANNEL,AppConstants.GENERAL_CHANNEL_DESC);
	    				user.addChannel(theGeneralChannel);
	    				//entering the general channel subscription for this user
	    				try {
		    				//getting users ID
		    				stmt = conn.prepareStatement(AppConstants.UPDATE_CHANNEL_USER);
		    				stmt.setString(1, AppConstants.GENERAL_CHANNEL);
		    				stmt.setInt(2, userID);
		    				stmt.setString(3, user.getUserName());
		    				stmt.executeUpdate();
	    				} catch (SQLException e) {
	        				getServletContext().log("Error while inserting new user", e);
	        	    		response.sendError(500);//internal server error
	        			}
	    				//setting session attribute - user ID for future actions
	    				session.setAttribute(AppConstants.USER_ID, userID);
    		}

    		conn.close();
    		
        	//convert from users collection to json
        	String userJsonResult = gson.toJson(user);

        	PrintWriter writer = response.getWriter();
        	writer.println(userJsonResult);
        	writer.close();
        	//inserting new user to user files
        	saveNewUserToFile(user);
    		}
    	} catch (SQLException | NamingException e) {
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


