/**
 * a servlet that checks and update notifications for the user
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

/**
 * Servlet implementation class GetNotificationsServlet
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
		//parsing details sent from client
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		String s = jsonObject.get("previousLog").toString();
		System.out.println(s);
		s = s.replaceAll("\"", "");
		System.out.println(s);
		GregorianCalendar datum = new GregorianCalendar();
		
		try {
			datum.setTime(sdf.parse(s));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Timestamp previousLog = new Timestamp(datum.getTimeInMillis());
		
		
		String channelName = jsonObject.get("channel").toString();
		channelName = channelName.replaceAll("\"", "");
		
		//finding number of new notifications in database according to channel name and previouslog
		PreparedStatement stmt;
		int notifications = 0;
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_DATEL_AND_CHANNEL);
			stmt.setString(1, channelName);
			stmt.setTimestamp(2,previousLog);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				/*for each message sent in the channel after the last time the user has logged
				(on his leave of site) we add 1 to the notifications*/
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
