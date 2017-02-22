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

import JamSession.AppConstants;
import JamSession.AppVariables;
import models.Channel;

/**
 * Servlet implementation class SearchChannelByNameServlet
 */
@WebServlet("/SearchChannelByNameServlet")
public class SearchChannelByNameServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchChannelByNameServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
			Connection conn = null;
			try {
				conn = AppVariables.db.getConnection();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		String channelName = "channelName";
    		HttpSession session = request.getSession();
    		String userName = (String) session.getAttribute(AppConstants.USERNAME);
    		
    		//reading user details from the request
    		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
    		StringBuilder jsonDetails =new StringBuilder();
    		String line;
    		while ((line = br.readLine()) !=null){
    			jsonDetails.append(line);
    		}
    		
    		JsonParser parser = new JsonParser();
    		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();
    		
    		String channelNameValue = jsonObject.get("channelName").toString();
    		Collection<Channel> result = new ArrayList<Channel>();
    		if (channelNameValue !=null){
    			PreparedStatement stmt;
    			try {
    				stmt = conn.prepareStatement(AppConstants.SELECT_CHANNEL_BY_NAME_AND_TYPE);
    				stmt.setString(1, channelNameValue);
    				stmt.setString(2, "public");
    				
    				ResultSet rs = stmt.executeQuery();
    				//if there are results than user is registered to site and it is ok to proceed
    				if (!rs.next()){
    					result.add(new Channel(models.Type.PUBLIC,rs.getString(1),rs.getString(3),rs.getString(4),rs.getTimestamp(5)));
    				}
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
    		//send users details to client
			Gson gson = new Gson();
        	//convert from users collection to json
        	String channelsJsonresult = gson.toJson(result, Channel.class);

        	PrintWriter writer = response.getWriter();
        	writer.println(channelsJsonresult);
        	writer.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
