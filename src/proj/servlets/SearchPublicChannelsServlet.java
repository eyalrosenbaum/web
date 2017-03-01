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
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;
import proj.models.PublicChannel;
import proj.models.Subscription;
import proj.models.User;


/**
 * Servlet implementation class SearchPublicChannelsServlet
 */
@WebServlet("/SearchPublicChannelsServlet")
public class SearchPublicChannelsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchPublicChannelsServlet() {
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
		HttpSession session = request.getSession();

		System.out.println("attribute set "+ session.getAttribute(AppConstants.USERNAME).toString());

		System.out.println("attribute set "+ session.getAttribute(AppConstants.USERNICKNAME).toString());

		System.out.println("attribute set "+ session.getAttribute(AppConstants.DESCRIPTION).toString());

		System.out.println("attribute set "+ session.getAttribute(AppConstants.PHOTOURL).toString());
	
		System.out.println("attribute set "+ session.getAttribute(AppConstants.LASTLOG).toString());
		
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
		Collection<PublicChannel> result = new ArrayList<PublicChannel>();
		String parameter = jsonObject.get("parameter").toString();
		parameter = parameter.replaceAll("\"","");
		String value = jsonObject.get("value").toString();
		value = value.replaceAll("\"","");
		System.out.println("parameter is "+parameter+" and value is "+value);
		if (parameter.equals("name")){
			/*search by channel's name*/
			if (value !=null){
				
				PreparedStatement stmt;
				try {
					stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_AND_TYPE);
					stmt.setString(1, value);
					
					System.out.println("lalalala");
					ResultSet rs = stmt.executeQuery();
					//if there are results than user is registered to site and it is ok to proceed
					if (rs.next()){
						PublicChannel channelToAdd = null;
						if (rs.getString(2).equals("public")){
						 channelToAdd = new PublicChannel(proj.models.Type.PUBLIC,rs.getString(1),rs.getString(4),rs.getString(3),rs.getTimestamp(5));
						 if (AppVariables.activeUsersByChannel.get(channelToAdd.getChannelName()) != null)
							 channelToAdd.setNumberOfUsers(AppVariables.activeUsersByChannel.get(channelToAdd.getChannelName()).size());
						 else {
							 ArrayList<User> channelUsers = new ArrayList<User>();
							 User user = new User(session.getAttribute(AppConstants.USERNAME).toString(),"pass",session.getAttribute(AppConstants.USERNICKNAME).toString(),session.getAttribute(AppConstants.DESCRIPTION).toString(),session.getAttribute(AppConstants.PHOTOURL).toString());
							 channelUsers.add(user);
							 AppVariables.activeUsersByChannel.put(channelToAdd.getChannelName(),channelUsers);
							 channelToAdd.setNumberOfUsers(1);
						 }
						 result.add(channelToAdd);
						System.out.println("result is "+result);
						}
						else System.out.println("lalalala");
					}
					else System.out.println("lululu");
					rs.close();
					stmt.close();

				} catch (SQLException e) {
					getServletContext().log("Error while querying for users", e);
					response.sendError(500);//internal server error
				}
			}


			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//send channels details to client
			//convert from channel collection to json
			String channelsJsonresult = gson.toJson(result, AppConstants.PUBLIC_CHANNELS_COLLECTION);

			PrintWriter writer = response.getWriter();
			writer.println(channelsJsonresult);
			writer.close();
		}
		else{
			/*searching channels by nickname*/
			/*first looking for usesr's username by their nickname*/
			
			if (value !=null){
				PreparedStatement stmt;
				User userResult = null;
				try {
					stmt = conn.prepareStatement(AppConstants.SELECT_USER_BY_NICKNAME_STMT);
					stmt.setString(1, value);

					ResultSet rs = stmt.executeQuery();
					//if there are results than user is registered to site and it is ok to proceed
					if (rs.next()){
						userResult = new User(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
								rs.getBoolean(6), rs.getTimestamp(7));
					}
					rs.close();
					stmt.close();

				} catch (SQLException e) {
					getServletContext().log("Error while querying for users", e);
					response.sendError(500);//internal server error
				}
				String username = userResult.getUserName();
				/*getting channels names from subscription table*/
				ArrayList<Subscription> list = new ArrayList<Subscription>();
				try {
					stmt = conn.prepareStatement(AppConstants.SELECT_SUBSCRIPTIONS_BY_USERNAME);
					stmt.setString(1, username);

					ResultSet rs = stmt.executeQuery();
					//if there are results than user is registered to site and it is ok to proceed
					if (rs.next()){
						list.add(new Subscription(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)));
					}
					rs.close();
					stmt.close();

				} catch (SQLException e) {
					getServletContext().log("Error while querying for users", e);
					response.sendError(500);//internal server error
				}
				/*getting channels details according to channels names from channels table*/
				for (Subscription sub : list){
					try {
						stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_AND_TYPE);
						stmt.setString(1, sub.getChannel());

						ResultSet rs = stmt.executeQuery();
						//if there are results than user is registered to site and it is ok to proceed
						if (rs.next()){
							PublicChannel channelToAdd = null;
							if (rs.getString(2).equals("public")){
							channelToAdd = new PublicChannel(proj.models.Type.PUBLIC,rs.getString(1),rs.getString(4),rs.getString(3),rs.getTimestamp(5));
				
								 if (AppVariables.activeUsersByChannel.get(channelToAdd.getChannelName()) != null)
									 channelToAdd.setNumberOfUsers(AppVariables.activeUsersByChannel.get(channelToAdd.getChannelName()).size());
								 else {
									 ArrayList<User> channelUsers = new ArrayList<User>();
									 User user = new User(session.getAttribute(AppConstants.USERNAME).toString(),"pass",session.getAttribute(AppConstants.USERNICKNAME).toString(),session.getAttribute(AppConstants.DESCRIPTION).toString(),session.getAttribute(AppConstants.PHOTOURL).toString());
									 channelUsers.add(user);
									 AppVariables.activeUsersByChannel.put(channelToAdd.getChannelName(),channelUsers);
									 channelToAdd.setNumberOfUsers(1);
								 }
								 result.add(channelToAdd);
								System.out.println("result is "+result);
								}
								else System.out.println("lalalala");;
							}
							
						rs.close();
						stmt.close(); 

					} catch (SQLException e) {
						getServletContext().log("Error while querying for users", e);
						response.sendError(500);//internal server error
					}
				}
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			//send channels details to client
			//convert from channel collection to json
			String channelsJsonresult = gson.toJson(result, AppConstants.PUBLIC_CHANNELS_COLLECTION);
			System.out.println(channelsJsonresult);
			PrintWriter writer = response.getWriter();
			writer.println(channelsJsonresult);
			writer.close();
	

	}

}
