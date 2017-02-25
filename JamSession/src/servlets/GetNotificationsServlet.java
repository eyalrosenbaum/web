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
 * Servlet implementation class GetPublicNotificationsServlet
 */
@WebServlet("/GetNotificationsServlet")
public class GetNotificationsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetNotificationsServlet() {
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

		String userNickname = jsonObject.get("nickname").toString();
		Timestamp previousLog = Timestamp.valueOf(jsonObject.get("previousLog").toString());
		String channelName = jsonObject.get("channel").toString();

		//finding number of new notifications in database according to channel name and previouslog
		PreparedStatement stmt;
		int notifications = 0;
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_DATEL_AND_CHANNEL);
			stmt.setString(1, channelName);
			stmt.setTimestamp(2,previousLog);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				notifications++;
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while querying for messages", e);
			response.sendError(500);//internal server error
		}
		//convert from int  to json
		String notificationsJsonResult = gson.toJson(notifications, Integer.class);

		PrintWriter writer = response.getWriter();
		writer.println(notificationsJsonResult);
		writer.close();
	}

}
