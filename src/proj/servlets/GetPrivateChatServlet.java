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
import proj.models.PrivateChannel;


/**
 * Servlet implementation class GetPrivateChatServlet
 */
@WebServlet("/GetPrivateChatServlet")
public class GetPrivateChatServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetPrivateChatServlet() {
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
		//usera initiated the request
		String nicknameA = jsonObject.get("usera").toString();
		String nicknameB = jsonObject.get("userb").toString();
		
		//finding channel in database according to nicknames
		PreparedStatement stmt;
		PrivateChannel privateChat = null;
				try {
					stmt = conn.prepareStatement(AppConstants.SELECT_PRIVATE_CHANNEL_BY_NICKNAME);
					stmt.setString(1, nicknameA);
					stmt.setString(2, nicknameB);
					ResultSet rs = stmt.executeQuery();
					while (rs.next()){
						privateChat = new PrivateChannel(proj.models.Type.PRIVATE,rs.getString(1), rs.getString(4), rs.getTimestamp(5));
					}
					rs.close();
					stmt.close();
					conn.close();
				} catch (SQLException e) {
					getServletContext().log("Error while querying for messages", e);
					response.sendError(500);//internal server error
				}
//				if (privateChat == null){
//					try {
//						stmt = conn.prepareStatement(AppConstants.SELECT_PRIVATE_CHANNEL_BY_NICKNAME);
//						stmt.setString(1, nicknameB);
//						stmt.setString(2, nicknameA);
//						ResultSet rs = stmt.executeQuery();
//						while (rs.next()){
//							privateChat = new PrivateChannel(proj.models.Type.PRIVATE,rs.getString(1), rs.getString(4), rs.getTimestamp(5));
//						}
//						rs.close();
//						stmt.close();
//						conn.close();
//					} catch (SQLException e) {
//						getServletContext().log("Error while querying for messages", e);
//						response.sendError(500);//internal server error
//					}
//				}
				//convert from channel to json
				String privateChatJsonResult = null;
				if (privateChat!=null)
				privateChatJsonResult = gson.toJson(privateChat, PrivateChannel.class);

				PrintWriter writer = response.getWriter();
				writer.println(privateChatJsonResult);
				writer.close();
	}

}
