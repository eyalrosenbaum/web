/**
 * a servlet that handles a users subscription to a public channel
 */
package proj.servlets;

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

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.Subscription;
import proj.models.User;


/**
 * Servlet implementation class PublicChannelSubscribeServlet
 */
@WebServlet("/PublicChannelSubscribeServlet")
public class PublicChannelSubscribeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PublicChannelSubscribeServlet() {
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
		HttpSession session = request.getSession();
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
		
		String channel = jsonObject.get("channel").toString();
		channel = channel.replaceAll("\"", "");
		String user = jsonObject.get("user").toString();
		user = user.replaceAll("\"", "");
		Timestamp time = new Timestamp(System.currentTimeMillis());
		String username = session.getAttribute(AppConstants.USERNAME).toString(); 
		boolean subscribedAlready = false;
		Subscription newSubscription = null;
		
		/*first check that subscription does not already exist*/
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL);
			stmt.setString(1, username);
			stmt.setString(2, channel);
			ResultSet rs = stmt.executeQuery();
			if (rs.next())
				subscribedAlready = true;
			stmt.close();
			
		} catch (SQLException e) {
			getServletContext().log("Error while inserting new channel", e);
    		response.sendError(500);//internal server error
		}
		if (!subscribedAlready){
			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_SUBSCRIPTIONS);
				stmt.setString(1, username);
				stmt.setString(2, channel);
				stmt.setString(3, "public");
				stmt.setTimestamp(4, time);
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				
			} catch (SQLException e) {
				getServletContext().log("Error while inserting new channel", e);
	    		response.sendError(500);//internal server error
			}
		
			newSubscription = new Subscription(username,channel,"public");
			newSubscription.setDate(time);
			//convert from subscription to json
			
		}
	PrintWriter writer = response.getWriter();
	if ((newSubscription != null)&&(checkSuccessful(newSubscription))){
		String channelJsonresult = gson.toJson(newSubscription, Subscription.class);
		writer.println(channelJsonresult);
		writer.close();
		//updating userlist for channel in hashmap
		ArrayList<User> userList = AppVariables.usersByChannel.get(channel);
		if (userList == null)
			userList = new ArrayList<User>();
		else{
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_USERNAME_STMT);
				stmt.setString(1, username);
				ResultSet rs = stmt.executeQuery();
				while(rs.next()){
					userList.add(new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
							rs.getBoolean(6), rs.getTimestamp(7)));
				}
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while inserting new channel", e);
	    		response.sendError(500);//internal server error
			}
			AppVariables.usersByChannel.put(channel, userList);
		}
	}
	else
		writer.println("fail");
		writer.close();
	}
	
	protected boolean checkSuccessful(Subscription subscription) {
		Connection conn = null;
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL);
				stmt.setString(1, subscription.getUsername());
				stmt.setString(2, subscription.getChannel());
				ResultSet rs = stmt.executeQuery();
				if (rs.next()){
					stmt.close();
					conn.close();
					return true;
				}
				else{
					stmt.close();
					conn.close();
					return false;
				}
			
			} catch (SQLException e) {
				getServletContext().log("Error while looking for the new subscription", e);
			}
		return false;
	}

}
