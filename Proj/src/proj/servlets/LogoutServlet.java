/**
 * a servlet that handles the logout of an existing user
 */
package proj.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.User;


/**
 * Servlet implementation class LogoutServlet
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogoutServlet() {
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
		Gson gson = new Gson();
		Connection conn = null;
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//reading channel details from the request
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuilder jsonDetails =new StringBuilder();
		String line;
		while ((line = br.readLine()) !=null){
			jsonDetails.append(line);
		}
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();
		
		String usernickname = jsonObject.get("userName").toString();
		usernickname.replaceAll("\"", "");
		Timestamp date = new Timestamp(System.currentTimeMillis());
		String channel = jsonObject.get("lastActiveChannel").toString();
		channel.replaceAll("\"", "");
		
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(AppConstants.UPDATE_UNLOGGED_USER_STMT);
			stmt.setTimestamp(1, date);
			stmt.setString(2, usernickname);
			stmt.executeUpdate();
			conn.commit();
			stmt.close();
			
			

		} catch (SQLException e) {
			getServletContext().log("Error while updating users is logged status", e);
			response.sendError(500);//internal server error
		}
		//removing user from active users in channels hashmap
		ArrayList<User> UserList = AppVariables.activeUsersByChannel.get(channel);
		Iterator itr;
		if (UserList!=null){
		itr = UserList.iterator();
		while(itr.hasNext()){
			User user = (User)itr.next();
			if (user.getUserNickname().equals(usernickname))
				itr.remove();
		}
		
		AppVariables.activeUsersByChannel.put(channel, UserList);
		}
		ArrayList<User> users = AppVariables.usersByChannel.get(channel);
		if (users!=null){
			itr = users.iterator();
			while(itr.hasNext()){
				User user = (User)itr.next();
				if (user.getUserNickname().equals(usernickname))
					itr.remove();
			}
			
			AppVariables.usersByChannel.put(channel, users);
			
		}
	try {
		conn.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//send channels details to client
	//convert from channel collection to json

	PrintWriter writer = response.getWriter();
	writer.println("success");
	writer.close();
	HttpSession session = request.getSession();
	if (session != null){
		session.removeAttribute(AppConstants.USERNAME);
		session.removeAttribute(AppConstants.USERNICKNAME);
		session.removeAttribute(AppConstants.DESCRIPTION);
		session.removeAttribute(AppConstants.PHOTOURL);
		session.removeAttribute(AppConstants.LASTLOG);
		session.removeAttribute(AppConstants.LASTLASTLOG);
		session.invalidate();
	}
	}

}
