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
import models.Message;
import models.Subscription;

/**
 * Servlet implementation class GetNextTenThreadsUpServlet
 */
@WebServlet("/GetNextTenThreadsUpServlet")
public class GetNextTenThreadsUpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetNextTenThreadsUpServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
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

		String channelName = jsonObject.get("name").toString();
		Timestamp date = Timestamp.valueOf(jsonObject.get("date").toString());
		String username = jsonObject.get("username").toString();
		Timestamp dateSubscribed = null;
		Subscription sub = null;
		//finding messages in database according to channel name
		PreparedStatement stmt;
		Collection<Message> channelThreads = new ArrayList<Message>();
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL);
			stmt.setString(1, username);
			stmt.setString(2, channelName);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				if (rs.getString(4).equals("public"))
				sub = new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),models.Type.PUBLIC);
				else
					sub = new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),models.Type.PRIVATE);
			}
			dateSubscribed = sub.getDate();
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while querying for messages", e);
			response.sendError(500);//internal server error
		}
		try {
			if((date.after(dateSubscribed))||(date.equals(dateSubscribed))){
			stmt = conn.prepareStatement(AppConstants.SELECT_THREADS_BY_CHANNEL_AND_DATE_DESC);
			stmt.setString(1, channelName);
			stmt.setTimestamp(2, date);
			stmt.setMaxRows(10);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				channelThreads.add(new Message(rs.getInt(1), rs.getString(2), rs.getString(3),rs.getString(4), rs.getBoolean(5), rs.getInt(6), rs.getInt(7),
						rs.getTimestamp(8), rs.getTimestamp(9)));
			}
			rs.close();
			stmt.close();
			conn.close();
			}
		} catch (SQLException e) {
			getServletContext().log("Error while querying for messages", e);
			response.sendError(500);//internal server error
		}
		//adding photourl to message
		try {
			for (Message thread: channelThreads){
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt.setString(1, thread.getAuthor());
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					thread.setAuthorPhotoUrl(rs.getString(5));
				}
				rs.close();
				stmt.close();
				conn.close();
			}} catch (SQLException e) {
				getServletContext().log("Error while querying for threads creators", e);
				response.sendError(500);//internal server error
			}
		
		try {
			for (Message thread: channelThreads){
				stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_REPLYTO_STMT);
				stmt.setInt(1, thread.getId());
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					thread.addtoumberOfReplies();
				}
				rs.close();
				stmt.close();
				conn.close();
			}} catch (SQLException e) {
				getServletContext().log("Error while querying for threads creators", e);
				response.sendError(500);//internal server error
			}
		//convert from subscriptions collection to json
		String channelThreadsJsonResult = gson.toJson(channelThreads, AppConstants.MESSAGE_COLLECTION);

		PrintWriter writer = response.getWriter();
		writer.println(channelThreadsJsonResult);
		writer.close();
	}

}
