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

		//finding messages in database according to channel name
		PreparedStatement stmt;
		Message channelThread = null;
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_THREADS_BY_CHANNEL_AND_DATE_DESC);
			stmt.setString(1, channelName);
			stmt.setTimestamp(2, date);
			stmt.setMaxRows(1);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				channelThread = new Message(rs.getInt(1), rs.getString(2), rs.getString(3),rs.getString(4), rs.getBoolean(5), rs.getInt(6), rs.getInt(7),
						rs.getTimestamp(8), rs.getTimestamp(9));
			}
			rs.close();
			stmt.close();
			conn.close();
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
		String channelThreadsJsonResult = gson.toJson(channelThread, Message.class);

		PrintWriter writer = response.getWriter();
		writer.println(channelThreadsJsonResult);
		writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
