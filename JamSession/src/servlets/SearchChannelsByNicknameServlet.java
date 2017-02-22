package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import JamSession.AppConstants;
import JamSession.AppVariables;
import models.Channel;
import models.Subscription;
import models.User;

/**
 * Servlet implementation class SearchChannelsByNicknameServlet
 */
@WebServlet("/SearchChannelsByNicknameServlet")
public class SearchChannelsByNicknameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchChannelsByNicknameServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection conn = null;
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String nickname = "userNickname";
		
		//reading user details from the request
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuilder jsonDetails =new StringBuilder();
		String line;
		while ((line = br.readLine()) !=null){
			jsonDetails.append(line);
		}
		
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();
		
		String nicknameValue = jsonObject.get(nickname).toString();
		User userResult = null;
		if (nicknameValue !=null){
			PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt.setString(1, nicknameValue);
				
				ResultSet rs = stmt.executeQuery();
				//if there are results than user is registered to site and it is ok to proceed
				if (!rs.next()){
					userResult = new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7));
				}
				rs.close();
				stmt.close();
				
    			} catch (SQLException e) {
    				getServletContext().log("Error while querying for users", e);
    	    		response.sendError(500);//internal server error
    			}
				Collection<Subscription> result = new ArrayList<Subscription>();
				try {
					stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME);
					stmt.setString(1, userResult.getUserName());
					
					ResultSet rs = stmt.executeQuery();
					while (rs.next()){
						String type = rs.getString(4);
						if (type.equals("public"))
							result.add(new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),models.Type.PUBLIC));
					}
					rs.close();
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					getServletContext().log("Error while querying for subscriptions", e);
		    		response.sendError(500);//internal server error
				}
				Collection<Channel> channelResult = new ArrayList<Channel>();
				for(Subscription subscription:result){
	    			try {
	    				stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_AND_TYPE);
	    				stmt.setString(1, subscription.getChannel());
	    				stmt.setString(2, "public");
	    				
	    				ResultSet rs = stmt.executeQuery();
	    				//if there are results than user is registered to site and it is ok to proceed
	    				if (!rs.next()){
	    					channelResult.add(new Channel(models.Type.PUBLIC,rs.getString(1),rs.getString(3),rs.getString(4),rs.getTimestamp(5)));
	    				}
	    				rs.close();
	    				stmt.close();
	    				
		    			} catch (SQLException e) {
		    				getServletContext().log("Error while querying for users", e);
		    	    		response.sendError(500);//internal server error
		    			}
	    			}
				
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		//send channels details to client
				Gson gson = new Gson();
	        	//convert from channels collection to json
	        	String channelsJsonresult = gson.toJson(channelResult, Channel.class);

	        	PrintWriter writer = response.getWriter();
	        	writer.println(channelsJsonresult);
	        	writer.close();
		}
	    		
	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
