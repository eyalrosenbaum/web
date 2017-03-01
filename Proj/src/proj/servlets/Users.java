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
import proj.models.Subscription;
import proj.models.User;

/**
 * Servlet implementation class Users
 */
@WebServlet("/Users")
public class Users extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Users() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection conn = null;
		Collection<User> usersResult = new ArrayList<User>();
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {

			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM USERS";
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()){
				usersResult.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
				
				
			}
			rs.close();
			stmt.close();
			
		} catch (SQLException e) {
			getServletContext().log("Error while querying for users", e);
			response.sendError(500);//internal server error
		}
		
		Gson gson = new Gson();
		String JsonResult = gson.toJson(usersResult,AppConstants.USER_COLLECTION);
		System.out.println(JsonResult);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
