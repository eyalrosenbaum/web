package proj.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.Channel;
import proj.models.Message;
import proj.models.PrivateChannel;
import proj.models.PublicChannel;

/**
 * Servlet implementation class Messages
 */
@WebServlet("/Messages")
public class Messages extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Messages() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection conn = null;
		Collection<Message> messagesResult = new ArrayList<Message>();
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM MESSAGES";
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()){
				messagesResult.add(new Message(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),
						rs.getBoolean(5),rs.getInt(6),rs.getInt(7),rs.getTimestamp(8),rs.getTimestamp(9)));
				
			}
			rs.close();
			stmt.close();
			
		} catch (SQLException e) {
			getServletContext().log("Error while querying for users", e);
			response.sendError(500);//internal server error
		}
		
		Gson gson = new Gson();
		String JsonResult = gson.toJson(messagesResult,AppConstants.MESSAGE_COLLECTION);
		System.out.println("messages " +JsonResult);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
