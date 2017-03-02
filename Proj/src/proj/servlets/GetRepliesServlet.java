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
import java.util.Collection;

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


/**
 * Servlet implementation class GetRepliesServlet
 */
@WebServlet("/GetRepliesServlet/threadID/*")
public class GetRepliesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetRepliesServlet() {
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

		
		Integer threadid = null;
		String uri = request.getRequestURI();
		if (uri.indexOf(AppConstants.THREADID)!=-1)
			threadid = Integer.parseInt(uri.substring(uri.indexOf(AppConstants.THREADID)+AppConstants.THREADID.length()+1));

		//finding messages in database according to channel name
		PreparedStatement stmt;
		Collection<Message> replies = new ArrayList<Message>();
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_REPLYTO_STMT);
			stmt.setInt(1, threadid);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				replies.add(new Message(rs.getInt(1), rs.getString(2), rs.getString(3),rs.getString(4), rs.getBoolean(5), rs.getInt(6), rs.getInt(7),
						rs.getTimestamp(8), rs.getTimestamp(9)));
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
			for (Message thread: replies){
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
				getServletContext().log("Error while querying for replies creators", e);
				response.sendError(500);//internal server error
			}
		//inserting number of replies to messge
		try {
			for (Message thread: replies){
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
		if (replies.isEmpty()){
			PrintWriter writer = response.getWriter();
			writer.println("fail");
			writer.close();
		}
		else{
		//convert from subscriptions collection to json
		String channelThreadsJsonResult = gson.toJson(replies, AppConstants.MESSAGE_COLLECTION);

		PrintWriter writer = response.getWriter();
		writer.println(channelThreadsJsonResult);
		writer.close();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
