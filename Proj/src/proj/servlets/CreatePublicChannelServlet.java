package proj.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.PublicChannel;
import proj.models.Subscription;
import proj.models.User;


/**
 * Servlet implementation class CreatePublicChannelServlet
 */
@WebServlet("/CreatePublicChannelServlet")
public class CreatePublicChannelServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreatePublicChannelServlet() {
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
		
		PublicChannel newChannel = gson.fromJson(jsonObject, PublicChannel.class);
		PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_CHANNEL_STMT);
				stmt.setString(1, newChannel.getChannelName());
				stmt.setString(2, newChannel.getChannelType().toString());
				stmt.setString(3, newChannel.getChannelDescription());
				stmt.setString(4, newChannel.getChannelCreator());
				stmt.setTimestamp(5, newChannel.getChannelCreationTime());
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
			} catch (SQLException e) {
				getServletContext().log("Error while inserting new channel", e);
	    		response.sendError(500);//internal server error
			}
		

    	PrintWriter writer = response.getWriter();
    	if (checkSuccessful(newChannel)){
    		//create subscription for user
			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_SUBSCRIPTIONS);
				stmt.setString(1, newChannel.getChannelCreator());
				stmt.setString(2, newChannel.getChannelName());
				stmt.setString(3, newChannel.getChannelType().toString());
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
			} catch (SQLException e) {
				getServletContext().log("Error while inserting new channel", e);
	    		response.sendError(500);//internal server error
			}
			ArrayList<User> newChannelUsers = new ArrayList<User>();
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_USERNAME_STMT);
				stmt.setString(1, newChannel.getChannelCreator());
				ResultSet rs = stmt.executeQuery();
				while(rs.next())
					newChannelUsers.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),
							rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
				stmt.close();
			} catch (SQLException e) {
				getServletContext().log("Error while fetching user", e);
	    		response.sendError(500);//internal server error
			}
			AppVariables.usersByChannel.put(newChannel.getChannelName(), newChannelUsers);
			Subscription newsub = null;
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL);
				stmt.setString(1, newChannel.getChannelCreator());
				stmt.setString(2, newChannel.getChannelName());
				ResultSet rs = stmt.executeQuery();
				while(rs.next())
					newsub = new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),proj.models.Type.PUBLIC);
				stmt.close();
			} catch (SQLException e) {
				getServletContext().log("Error while fetching user", e);
	    		response.sendError(500);//internal server error
			}
			
			//convert from channel to json
			String publicchannelJsonResult = gson.toJson(newsub, Subscription.class);
    		writer.println(publicchannelJsonResult);
    	}
    	else
    		writer.println("fail");
    	try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	writer.close();
		
	}
	
	protected boolean checkSuccessful(PublicChannel channel) {
		Connection conn = null;
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_AND_CREATED);
				stmt.setString(1, channel.getChannelName());
				stmt.setTimestamp(2, channel.getChannelCreationTime());
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
				getServletContext().log("Error while looking for the new channel", e);
			}
		return false;
	}


}
