package proj.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.Message;


/**
 * Servlet implementation class PostReplyServlet
 */
@WebServlet("/PostReplyServlet")
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
		Message thread = gson.fromJson(jsonDetails.toString(), Message.class);
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(AppConstants.INSERT_MESSAGE_STMT);
			stmt.setString(1, thread.getAuthor());
			stmt.setString(2, thread.getChannel());
			stmt.setString(3, thread.getContent());
			stmt.setBoolean(3, thread.isThread());
			stmt.setInt(3, thread.getIsReplyTo());
			stmt.setInt(3, thread.getThreadID());
			stmt.setTimestamp(3, thread.getLastUpdate());
			stmt.setTimestamp(3, thread.getDate());
			stmt.executeUpdate();
			conn.commit();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while inserting new thread", e);
			response.sendError(500);//internal server error
		}


		PrintWriter writer = response.getWriter();
		if (checkSuccessful(thread)){
			writer.println("success");
			int id = 0;
			/*update threadid to be the thread's id*/
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_AUTHOR_AND_DATE_STMT);
				stmt.setString(1, thread.getAuthor());
				stmt.setTimestamp(2, thread.getDate());
				ResultSet rs = stmt.executeQuery();
				if (rs.next()){

					id = rs.getInt(1);
					stmt.close();
					conn.close();
				} }catch (SQLException e) {
					getServletContext().log("Error while looking for the new message", e);
				}
			try {
				stmt = conn.prepareStatement(AppConstants.UPDATE_THREAD_THREADID_STMT);
				stmt.setInt(1, thread.getId());
				stmt.setInt(2, thread.getId());
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while looking for the new message", e);
			}
		}
		else
			writer.println("fail");
		writer.close();

	}

	protected boolean checkSuccessful(Message thread) {
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
			stmt.setString(1, thread.getAuthor());
			stmt.setTimestamp(2, thread.getDate());
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
			getServletContext().log("Error while looking for the new message", e);
		}
		return false;
	}


}
