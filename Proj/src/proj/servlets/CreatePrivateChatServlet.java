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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.PrivateChannel;
import proj.models.User;


/**
 * Servlet implementation class CreatePrivateChatServlet
 */
@WebServlet("/CreatePrivateChatServlet")
public class CreatePrivateChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CreatePrivateChatServlet() {
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

		String name = jsonObject.get("name").toString();
		String creator = jsonObject.get("creator").toString();
		Timestamp created = Timestamp.valueOf((jsonObject.get("created")).toString());
		//participant is this user
		String participantA = jsonObject.get("participanta").toString();
		String participantB = jsonObject.get("participantb").toString();

		//finding channel in database according to nicknames
		String channelName = name+AppVariables.privateChatCounter++;
		PreparedStatement stmt;
		PrivateChannel privateChat = null;
		//first check if the private chat is opened at the other participant console
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_PRIVATE_CHANNEL_BY_NICKNAME);
			stmt.setString(1, participantB);
			stmt.setString(2, participantA);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				privateChat = new PrivateChannel(proj.models.Type.PRIVATE,rs.getString(1), rs.getString(4), rs.getTimestamp(5));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while querying for messages", e);
			response.sendError(500);//internal server error
		}
		//if both of users don't have the channel right now
		if (privateChat == null){
			//creating channel in database
			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_CHANNEL_STMT);
				stmt.setString(1,channelName );
				stmt.setString(2, proj.models.Type.PRIVATE.toString());
				stmt.setString(4, creator);
				stmt.setTimestamp(5, created);
				stmt.setString(6, participantA);
				stmt.setString(7, participantB);
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				conn.close();

			} catch (SQLException e) {
				getServletContext().log("Error while inserting new private channel", e);
				response.sendError(500);//internal server error
			}

			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_SUBSCRIPTIONS);
				stmt.setString(1,creator );
				stmt.setString(2,channelName);
				stmt.setString(3, "private");
				stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				conn.close();

			} catch (SQLException e) {
				getServletContext().log("Error while inserting new private channel subscription", e);
				response.sendError(500);//internal server error
			}
			ArrayList<User> users = new ArrayList<User>();
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt.setString(1, participantA);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					users.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
					AppVariables.activeUsersByChannel.put(channelName, users);
				}
				stmt.close();
				conn.close();
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt.setString(1, participantB);
				rs = stmt.executeQuery();
				while (rs.next())
					users.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
				stmt.close();
				conn.close();

			} catch (SQLException e) {
				getServletContext().log("Error while querying for messages", e);
				response.sendError(500);//internal server error
			}
			AppVariables.usersByChannel.put(channelName,users);

			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_AND_TYPE);
				stmt.setString(1,channelName );
				stmt.setString(2, "private");
				stmt.setString(4, creator);
				ResultSet rs = stmt.executeQuery();
				while (rs.next())
					privateChat = new PrivateChannel(proj.models.Type.PRIVATE, rs.getString(1),rs.getString(4),
							rs.getTimestamp(5));
				ArrayList<String> participants = new ArrayList<String>();
				participants.add(rs.getString(6));
				participants.add(rs.getString(7));
				privateChat.setParticipants(participants);
				stmt.close();
				conn.close();

			} catch (SQLException e) {
				getServletContext().log("Error while inserting new private channel", e);
				response.sendError(500);//internal server error
			}
		}
		//the private chat exists for other user - we only need to create it for this user, using the name of the existing chat
		else{
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_PRIVATE_CHANNEL_BY_NICKNAME);
				stmt.setString(1, participantB);
				stmt.setString(2, participantA);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					privateChat = new PrivateChannel(proj.models.Type.PRIVATE,rs.getString(1), rs.getString(4), rs.getTimestamp(5));
				}
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while querying for messages", e);
				response.sendError(500);//internal server error
			}
			//inserting a copy of the chat for our user to database
			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_CHANNEL_STMT);
				stmt.setString(1,privateChat.getChannelName() );
				stmt.setString(2, proj.models.Type.PRIVATE.toString());
				stmt.setString(4, creator);
				stmt.setTimestamp(5, created);
				stmt.setString(6, participantA);
				stmt.setString(7, participantB);
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				conn.close();

			} catch (SQLException e) {
				getServletContext().log("Error while inserting new private channel", e);
				response.sendError(500);//internal server error
			}

			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_SUBSCRIPTIONS);
				stmt.setString(1,creator );
				stmt.setString(2,privateChat.getChannelName());
				stmt.setString(3, "private");
				stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				conn.close();

			} catch (SQLException e) {
				getServletContext().log("Error while inserting new private channel subscription", e);
				response.sendError(500);//internal server error
			}
			ArrayList<User> users = new ArrayList<User>();
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt.setString(1, participantA);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					users.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
					AppVariables.activeUsersByChannel.put(privateChat.getChannelName(), users);
				}
				stmt.close();
				conn.close();
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt.setString(1, participantB);
				rs = stmt.executeQuery();
				while (rs.next())
					users.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
				stmt.close();
				conn.close();

			} catch (SQLException e) {
				getServletContext().log("Error while querying for messages", e);
				response.sendError(500);//internal server error
			}


			ArrayList<String> participants = new ArrayList<String>();
			participants.add(participantA);
			participants.add(participantB);
			privateChat.setParticipants(participants);



			AppVariables.usersByChannel.put(privateChat.getChannelName(),users);
		}

		//convert from channel to json
		String privateChatJsonResult = gson.toJson(privateChat, PrivateChannel.class);

		PrintWriter writer = response.getWriter();
		writer.println(privateChatJsonResult);
		writer.close();
	}
}


