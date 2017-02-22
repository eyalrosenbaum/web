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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import JamSession.AppConstants;
import JamSession.AppVariables;
import models.Subscription;
import models.User;

/**
 * Servlet implementation class UnsubscribeServlet
 */
@WebServlet("/UnsubscribeServlet")
public class UnsubscribeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UnsubscribeServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
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
		
		Subscription subscriptionToDelete = gson.fromJson(jsonObject, Subscription.class);
		PreparedStatement stmt;
		try {
			stmt = conn.prepareStatement(AppConstants.DELETE_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL);
			stmt.setString(1, subscriptionToDelete.getUsername());
			stmt.setString(2, subscriptionToDelete.getChannel());
			stmt.executeUpdate();
			conn.commit();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while deleting channel", e);
    		response.sendError(500);//internal server error
		}
	

	PrintWriter writer = response.getWriter();
	if (checkSuccessful(subscriptionToDelete)){
		writer.println("success");
		//updating userlist for channelS
		ArrayList<User> userlist = AppVariables.usersByChannel.get(subscriptionToDelete.getChannel());
		Iterator itr = userlist.iterator();
		User userToDelete = null;
		while(itr.hasNext()){
			userToDelete = (User) itr.next();
			if (userToDelete.getUserName().equals(subscriptionToDelete.getUsername()))
				userlist.remove(userToDelete);
		}
		AppVariables.usersByChannel.put(subscriptionToDelete.getChannel(), userlist);
	}
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
				if (!rs.next()){
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
				getServletContext().log("Error while looking for the deleted subscription", e);
			}
		return false;
	}


}
