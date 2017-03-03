/**
 * a servlet that handles a users unsubscription to a private channel
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
import java.util.ArrayList;
import java.util.Iterator;

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
import proj.models.PrivateChannel;
import proj.models.User;


/**
 * Servlet implementation class RemovePrivateChannelServlet
 */
@WebServlet("/RemovePrivateChannelServlet")
public class RemovePrivateChannelServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemovePrivateChannelServlet() {
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

		//reading private channel details from the request
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuilder jsonDetails =new StringBuilder();
		String line;
		while ((line = br.readLine()) !=null){
			jsonDetails.append(line);
		}
		
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();
		
		String participanta = jsonObject.get("first").toString();
		String participantb = jsonObject.get("second").toString();
		String user = jsonObject.get("user").toString();
		
		PrivateChannel theChannel = null;
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_PRIVATE_CHANNEL_BY_NICKNAME);
			stmt.setString(1,participanta);
			stmt.setString(2,participantb);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
				theChannel = new PrivateChannel(proj.models.Type.PRIVATE, rs.getString(1),rs.getString(4),rs.getTimestamp(5));
				ArrayList<String> participants = new ArrayList<String>();
				if (user.equals(participanta)){
					participants.add(user);
					participants.add(participantb);
				}
				else{
					participants.add(user);
					participants.add(participanta);				
				}
				theChannel.setParticipants(participants);
			}
				stmt.close();
		} catch (SQLException e) {
			getServletContext().log("Error while deleting channel", e);
    		response.sendError(500);//internal server error
		}
	
			try {
			stmt = conn.prepareStatement(AppConstants.DELETE_PRIVATE_CHANNEL_BY_NICKNAME);
			//for the user who wants to remove the channel his nickname will be first
			if (participanta.equals(user)){
				stmt.setString(1,participanta);
				stmt.setString(2,participantb);
			}
			else{
				stmt.setString(1,participantb);
				stmt.setString(2,participanta);
			}
			stmt.executeUpdate();
			conn.commit();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while deleting channel", e);
    		response.sendError(500);//internal server error
		}
	

	PrintWriter writer = response.getWriter();
	if (checkSuccessful(theChannel)){
		writer.println(theChannel);
		//updating userlist for channelS
		ArrayList<User> userlist = AppVariables.usersByChannel.get(theChannel.getChannelName());
		Iterator itr = userlist.iterator();
		User userToDelete = null;
		while(itr.hasNext()){
			userToDelete = (User) itr.next();
			if (userToDelete.getUserNickname().equals(user))
				userlist.remove(userToDelete);
		}
		AppVariables.usersByChannel.put(theChannel.getChannelName(), userlist);
		userlist = AppVariables.activeUsersByChannel.get(theChannel.getChannelName());
		 itr = userlist.iterator();
		 userToDelete = null;
		while(itr.hasNext()){
			userToDelete = (User) itr.next();
			if (userToDelete.getUserNickname().equals(user))
				userlist.remove(userToDelete);
		}
		AppVariables.activeUsersByChannel.put(theChannel.getChannelName(), userlist);
	}
	else
		writer.println("fail");
	writer.close();
	}
	
	
	protected boolean checkSuccessful(PrivateChannel subscription) {
		Connection conn = null;
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_PRIVATE_CHANNEL_BY_NICKNAME);
				stmt.setString(1,subscription.getParticipants().get(0));
				stmt.setString(2,subscription.getParticipants().get(1));
				ResultSet rs = stmt.executeQuery();
				if (!rs.next()){
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
				getServletContext().log("Error while looking for the deleted subscription", e);
			}
		return false;
	}

}
