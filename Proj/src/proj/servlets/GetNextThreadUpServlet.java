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
import java.util.Calendar;
import java.util.Date;

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
import proj.models.Message;
import proj.models.Subscription;

/**
 * Servlet implementation class GetNextThreadUpServlet
 */
@WebServlet("/GetNextThreadUpServlet")
public class GetNextThreadUpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetNextThreadUpServlet() {
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

		String channelName = jsonObject.get("name").toString();
		long s = Long.parseLong(jsonObject.get("date").toString());
		Date dater = new Date(s);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dater);
		cal.set(Calendar.MILLISECOND, 0);
		
		Timestamp date = new Timestamp(cal.getTimeInMillis());
		String username = jsonObject.get("username").toString();
		Timestamp dateSubscribed = null;
		Subscription sub = null;
		//finding messages in database according to channel name
		PreparedStatement stmt;
		Message channelThread = null;
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL);
			stmt.setString(1, username);
			stmt.setString(2, channelName);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				sub = new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4));

					sub.setDate(rs.getTimestamp(5));
					dateSubscribed = sub.getDate();
			}
			
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while querying for messages", e);
			response.sendError(500);//internal server error
		}
		try {
			/*if the first message's date is not after the date the user subscribed to channel, user can't see the threads*/
			if(date.after(dateSubscribed)){
			stmt = conn.prepareStatement(AppConstants.SELECT_THREADS_BY_CHANNEL_AND_DATE_DESC);
			stmt.setString(1, channelName);
			stmt.setTimestamp(2, dateSubscribed);
			stmt.setTimestamp(3, date);
			stmt.setMaxRows(1);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				channelThread = new Message(rs.getInt(1), rs.getString(2), rs.getString(3),rs.getString(4), rs.getBoolean(5), rs.getInt(6), rs.getInt(7),
						rs.getTimestamp(8), rs.getTimestamp(9));
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
				stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
				stmt.setString(1, channelThread.getAuthor());
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					channelThread.setAuthorPhotoUrl(rs.getString(5));
				}
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while querying for threads creators", e);
				response.sendError(500);//internal server error
			}
		//checking number of replies to thread
		try {
				stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_REPLYTO_STMT);
				stmt.setInt(1, channelThread.getId());
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					channelThread.addtoumberOfReplies();
				}
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while querying for threads creators", e);
				response.sendError(500);//internal server error
			}
		//convert from subscriptions collection to json
		String channelThreadJsonResult = gson.toJson(channelThread, Message.class);

		PrintWriter writer = response.getWriter();
		writer.println(channelThreadJsonResult);
		writer.close();
	}

}
