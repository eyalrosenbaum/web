/**
 * a servlet that creates a subscription to a channel
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
import proj.models.Subscription;


/**
 * Servlet implementation class CreateSubscriptionServlet
 */
@WebServlet("/CreateSubscriptionServlet")
public class CreateSubscriptionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreateSubscriptionServlet() {
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

		//reading subscription details from the request
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuilder jsonDetails =new StringBuilder();
		String line;
		while ((line = br.readLine()) !=null){
			jsonDetails.append(line);
		}
		
		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();
		
		Subscription newSubscription = gson.fromJson(jsonObject, Subscription.class);
		PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(AppConstants.INSERT_SUBSCRIPTIONS);
				stmt.setString(1, newSubscription.getUsername());
				stmt.setString(2, newSubscription.getChannel());
				stmt.setString(3, newSubscription.getType().toString());
				stmt.executeUpdate();
				conn.commit();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while inserting new channel", e);
	    		response.sendError(500);//internal server error
			}
		

    	PrintWriter writer = response.getWriter();
    	if (checkSuccessful(newSubscription))
    		writer.println("success");
    	else
    		writer.println("fail");
    	writer.close();
	}
	
	protected boolean checkSuccessful(Subscription subscription) {
		Connection conn = null;
		try {
			conn = AppVariables.db.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PreparedStatement stmt;
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL);
				stmt.setString(1, subscription.getUsername());
				stmt.setString(2, subscription.getChannel());
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


