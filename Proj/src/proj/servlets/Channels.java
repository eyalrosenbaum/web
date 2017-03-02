package proj.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
import proj.models.PrivateChannel;
import proj.models.PublicChannel;
import proj.models.User;

/**
 * Servlet implementation class Channels
 */
@WebServlet("/Channels")
public class Channels extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Channels() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Connection conn = null;
		Collection<Channel> channelResult = new ArrayList<Channel>();
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT * FROM CHANNELS";
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()){
				String type = rs.getString(2);
				if (type.equals("public")){
					channelResult.add(new PublicChannel(proj.models.Type.PUBLIC,rs.getString(1),rs.getString(5),rs.getString(3),rs.getTimestamp(5)));
				}
				else{
					channelResult.add(new PrivateChannel(proj.models.Type.PRIVATE,rs.getString(1),rs.getString(5),rs.getTimestamp(5)));
				}
				
			}
			rs.close();
			stmt.close();
			
		} catch (SQLException e) {
			getServletContext().log("Error while querying for users", e);
			response.sendError(500);//internal server error
		}
		
		Gson gson = new Gson();
		String JsonResult = gson.toJson(channelResult,AppConstants.CHANNELS_COLLECTION);
		System.out.println("channels :" + JsonResult);
		

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
