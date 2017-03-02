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
import java.util.Collection;

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
import proj.models.Message;
import proj.models.Subscription;


/**
 * Servlet implementation class GetNewestThreadsServlet
 */
@WebServlet("/GetNewestThreadsServlet/channelName/*")
public class GetNewestThreadsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetNewestThreadsServlet() {
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

		String channelName = null;
		String uri = request.getRequestURI();
		if (uri.indexOf(AppConstants.CHANNELNAME)!=-1)
			channelName = uri.substring(uri.indexOf(AppConstants.CHANNELNAME)+AppConstants.CHANNELNAME.length()+1);
		if (channelName!=null)
			channelName.replaceAll("\\%20", " ");
		HttpSession session = request.getSession();
		System.out.println("get newest threads servlets, channel name is "+channelName);
		String username = session.getAttribute(AppConstants.USERNAME).toString();
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

				sub = new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4));
				System.out.println("there were results to SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL");
			}
			dateSubscribed = sub.getDate();
			rs.close();
			stmt.close();
		
		} catch (SQLException e) {
			getServletContext().log("Error while querying for messages", e);
			response.sendError(500);//internal server error
		}
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_THREADS_BY_CHANNEL);
			stmt.setString(1, channelName);
			stmt.setTimestamp(2, dateSubscribed);
			//stmt.setTimestamp(2, date);
			stmt.setMaxRows(10);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				channelThreads.add(new Message(rs.getInt(1), rs.getString(2), rs.getString(3),rs.getString(4), rs.getBoolean(5), rs.getInt(6), rs.getInt(7),
						rs.getTimestamp(8), rs.getTimestamp(9)));
				System.out.println("there were results to select_threads_by_channel");
			}
			rs.close();
			stmt.close();
			
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
					System.out.println("there were results to SELECT_USER_BY_NICKNAME_STMT");
				}
				rs.close();
				stmt.close();
			
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
					System.out.println("there were results to SELECT_MESSAGES_BY_REPLYTO_STMT");
				}
				rs.close();
				stmt.close();
				conn.close();
			}} catch (SQLException e) {
				getServletContext().log("Error while querying for threads creators", e);
				response.sendError(500);//internal server error
			}
		System.out.println("there should be "+channelThreads.size()+" threads");
		//convert from subscriptions collection to json
		String channelThreadsJsonResult = gson.toJson(channelThreads, AppConstants.MESSAGE_COLLECTION);

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
