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
import proj.models.PrivateChannel;


/**
 * Servlet implementation class FindPrivateChannelsServlet
 */
@WebServlet("/FindPrivateChannelsServlet")
public class FindPrivateChannelsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FindPrivateChannelsServlet() {
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
		String nicknameValue = (String) session.getAttribute(AppConstants.USERNICKNAME);
		
		//reading user details from the request
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuilder jsonDetails =new StringBuilder();
		String line;
		while ((line = br.readLine()) !=null){
			jsonDetails.append(line);
		}
		
		
		//finding subscriptions in database according to username
		PreparedStatement stmt;
		Collection<PrivateChannel> result = new ArrayList<PrivateChannel>();
			try {
				stmt = conn.prepareStatement(AppConstants.SELECT_PRIVATE_CHANNELS_BY_NICKNAME);
				stmt.setString(1, nicknameValue);
				stmt.setString(2, nicknameValue);
				
				ResultSet rs = stmt.executeQuery();
				while (rs.next()){
					PrivateChannel toadd = new PrivateChannel(proj.models.Type.PRIVATE,rs.getString(1),rs.getString(4),rs.getTimestamp(5));
					ArrayList<String> participantsToAdd = new ArrayList<String>();
					participantsToAdd.add(rs.getString(6));
					participantsToAdd.add(rs.getString(7));
					toadd.setParticipants(participantsToAdd);
					//setting channels name to other participant because it is a private chat
					if (toadd.getParticipants().get(0).equals(nicknameValue))
						toadd.setChannelName(toadd.getParticipants().get(1));
					else toadd.setChannelName(toadd.getParticipants().get(0));
					result.add(toadd);
				}
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				getServletContext().log("Error while querying for private channels", e);
	    		response.sendError(500);//internal server error
			}
			
		//convert from subscriptions collection to json
    	String privatChannelsJsonResult = gson.toJson(result, AppConstants.PRIVATE_CHANNELS_COLLECTION);

    	PrintWriter writer = response.getWriter();
    	writer.println(privatChannelsJsonResult);
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
