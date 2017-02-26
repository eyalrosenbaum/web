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
import proj.models.PrivateChannel;
import proj.models.User;


/**
 * Servlet implementation class CreatePrivateChatServlet
 */
@WebServlet("/CreatePrivateChatServlet")
public class CreatePrivateChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CreatePrivateChatServlet() {
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

		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();

		String name = jsonObject.get("name").toString();
		String creator = jsonObject.get("creator").toString();
		Timestamp created = Timestamp.valueOf((jsonObject.get("created")).toString());
		String participantA = jsonObject.get("participanta").toString();
		String participantB = jsonObject.get("participantb").toString();
		
		//finding channel in database according to nicknames
		String channelName = name+AppVariables.privateChatCounter++;
		PreparedStatement stmt;
		PrivateChannel privateChat = null;
				try {
					stmt = conn.prepareStatement(AppConstants.INSERT_CHANNEL_STMT);
					stmt.setString(1,channelName );
					stmt.setString(2, proj.models.Type.PRIVATE.toString());
					stmt.setString(4, creator);
					stmt.setTimestamp(5, created);
					stmt.setString(6, participantA);
					stmt.setString(7, participantB);
					stmt.executeUpdate();
					conn.commit();
					stmt.close();
					conn.close();
					
				} catch (SQLException e) {
					getServletContext().log("Error while querying for messages", e);
					response.sendError(500);//internal server error
				}
				
				
				ArrayList<User> users = new ArrayList<User>();
				try {
					stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
					stmt.setString(1, participantA);
					ResultSet rs = stmt.executeQuery();
					while (rs.next())
						users.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
					stmt.close();
					conn.close();
					stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
					stmt.setString(1, participantB);
					rs = stmt.executeQuery();
					while (rs.next())
						users.add(new User(rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getBoolean(6),rs.getTimestamp(7)));
					stmt.close();
					conn.close();
					
				} catch (SQLException e) {
					getServletContext().log("Error while querying for messages", e);
					response.sendError(500);//internal server error
				}
				
				//updating hashmap of channels-users
				AppVariables.usersByChannel.put(channelName, users);
				//convert from channel to json
				String privateChatJsonResult = gson.toJson(privateChat, PrivateChannel.class);

				PrintWriter writer = response.getWriter();
				writer.println(privateChatJsonResult);
				writer.close();
	}
	}


