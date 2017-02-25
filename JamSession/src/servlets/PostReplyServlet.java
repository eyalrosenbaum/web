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
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import JamSession.AppConstants;
import JamSession.AppVariables;
import models.Message;
import models.User;

/**
 * Servlet implementation class PostReplyServlet
 */
@WebServlet("/PostThreadServlet")
public class PostReplyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public PostReplyServlet() {
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
		Message reply = gson.fromJson(jsonDetails.toString(), Message.class);
		Message parent = null;
		PreparedStatement stmt;
		try {
			/*getting thread number based on the reply's parent*/
			stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_ID_STMT);
			stmt.setInt(1, reply.getIsReplyTo());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				parent = new Message(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getBoolean(5),rs.getInt(6),rs.getInt(7),rs.getTimestamp(8),rs.getTimestamp(9));
			}
			stmt.close();

		} catch (SQLException e) {
			getServletContext().log("Error while finding parent message", e);
			response.sendError(500);//internal server error
		}
		reply.setThreadID(parent.getThreadID());
		ArrayList<User> usersInChannel = AppVariables.usersByChannel.get(reply.getChannel());
		/*finding whether the parent's author is still subscribed to channel, or else there will be 
		no reply*/
		boolean repliable = false;
		Iterator itr = usersInChannel.iterator();
		while(itr.hasNext()){
			User user = (User) itr.next();
			if (user.getUserNickname().equals(parent.getAuthor()))
				repliable = true;
		}
		if (repliable){
			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_MESSAGE_STMT);
				stmt.setString(1, reply.getAuthor());
				stmt.setString(2, reply.getChannel());
				stmt.setString(3, reply.getContent());
				stmt.setBoolean(3, reply.isThread());
				stmt.setInt(3, reply.getIsReplyTo());
				stmt.setInt(3, reply.getThreadID());
				stmt.setTimestamp(3, reply.getLastUpdate());
				stmt.setTimestamp(3, reply.getDate());
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while inserting new thread", e);
				response.sendError(500);//internal server error
			}


			PrintWriter writer = response.getWriter();
			if (checkSuccessful(reply)){
				writer.println("success");
				writer.close();
				/*updating all of the posts in thread lastupdated column to this reoly's time*/
				try {
					stmt = conn.prepareStatement(AppConstants.UPDATE_LAST_UPDATE_STMT);
					stmt.setTimestamp(1, reply.getLastUpdate());
					stmt.setInt(2, reply.getThreadID());
					stmt.executeUpdate();
					conn.commit();
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					getServletContext().log("Error while inserting new thread", e);
					response.sendError(500);//internal server error
				}}
		}
		else{
			PrintWriter writer = response.getWriter();
			writer.println("fail");
			writer.close();
		}

	}

	protected boolean checkSuccessful(Message reply) {
		Connection conn = null;
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_AUTHOR_AND_DATE_STMT);
			stmt.setString(1, reply.getAuthor());
			stmt.setTimestamp(2, reply.getDate());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()){
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
			getServletContext().log("Error while looking for the new subscription", e);
		}
		return false;
	}

}
