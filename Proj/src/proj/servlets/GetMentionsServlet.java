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


/**
 * Servlet implementation class GetMentionsServlet
 */
@WebServlet("/GetMentionsServlet")
public class GetMentionsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetMentionsServlet() {
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

		String userNickname = jsonObject.get("nickname").toString();
		Timestamp previousLog = Timestamp.valueOf(jsonObject.get("previousLog").toString());
		String channelName = jsonObject.get("channel").toString();

		//finding number of new notifications in database according to channel name and previouslog
		PreparedStatement stmt;
		int mentions = 0;
		String prefix = "@"+userNickname+"%";
		try {
			stmt = conn.prepareStatement(AppConstants.SELECT_MESSAGES_BY_DATE_AND_NICKNAME_AND_CHANNEL);
			stmt.setString(1, channelName);
			stmt.setTimestamp(2,previousLog);
			stmt.setString(3, prefix);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()){
				mentions++;
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			getServletContext().log("Error while querying for mentions", e);
			response.sendError(500);//internal server error
		}
		//convert from int  to json
		String mentionsJsonResult = gson.toJson(mentions, Integer.class);

		PrintWriter writer = response.getWriter();
		writer.println(mentionsJsonResult);
		writer.close();
	}

}
