package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import com.google.gson.Gson;

import JamSession.AppConstants;
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
    		
        	//obtain UserDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(
    				getServletContext().getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
    		Connection conn = ds.getConnection();

    		Collection<User> usersResult = new ArrayList<User>(); 
    		String userName = "userName";
    		String password = "password";
    		
    		String userNameValue = request.getParameter(userName);
    		String passwordValue = request.getParameter(password);
    		
    		if ((userNameValue !=null) && (passwordValue != null)){
    			PreparedStatement stmt;
    			try {
    				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_USERNAME_AND_PASS_STMT);
    				stmt.setString(1, userNameValue);
    				stmt.setString(2, passwordValue);
    				
    				ResultSet rs = stmt.executeQuery();
    				while (!rs.next()){
    					//if there are results we add them to userResult
    					usersResult.add(new User(rs.getInt(1),rs.getString(2),rs.getString(3),
    							rs.getString(4),rs.getString(5),rs.getString(6)));
    				}
    				rs.close();
    				stmt.close();
    			} catch (SQLException e) {
    				getServletContext().log("Error while querying for users", e);
    	    		response.sendError(500);//internal server error
    			}
    		}

    		conn.close();
    		
    		Gson gson = new Gson();
        	//convert from users collection to json
        	String userJsonresult = gson.toJson(((ArrayList<User>) usersResult).get(0), User.class);

        	PrintWriter writer = response.getWriter();
        	writer.println(userJsonresult);
        	writer.close();
    	} catch (SQLException | NamingException e) {
    		getServletContext().log("Error while closing connection", e);
    		response.sendError(500);//internal server error
    	}

	}

}
