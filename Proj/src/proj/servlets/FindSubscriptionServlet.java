package proj.servlets;

import java.io.IOException;
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
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.Subscription;


/**
 * Servlet implementation class FindSubscriptionServlet
 */
@WebServlet("/FindSubscriptionServlet")
public class FindSubscriptionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FindSubscriptionServlet() {
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

		HttpSession session = request.getSession();
		String userName = (String) session.getAttribute(AppConstants.USERNAME);
		String userNickname = (String) session.getAttribute(AppConstants.USERNICKNAME);
		
		//finding subscriptions in database according to username
		PreparedStatement stmt;
		Collection<Subscription> result = new ArrayList<Subscription>();
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME);
				stmt.setString(1, userName);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					String type = rs.getString(4);
					if (type.equals("public"))
						result.add(new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),proj.models.Type.PUBLIC));
//					else
//						result.add(new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),models.Type.PRIVATE));
				}
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while querying for subscriptions", e);
	    		response.sendError(500);//internal server error
			}
			
		//convert from subscriptions collection to json
    	String subscriptionsJsonResult = gson.toJson(result, AppConstants.SUBSCRIPTION_COLLECTION);

    	PrintWriter writer = response.getWriter();
    	writer.println(subscriptionsJsonResult);
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
