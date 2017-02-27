package proj.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

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
import proj.models.User;

/**
 * Servlet implementation class RemoveUserFromChannelListServlet
 */
@WebServlet("/RemoveUserFromChannelListServlet")
public class RemoveUserFromChannelListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RemoveUserFromChannelListServlet() {
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
		
		HttpSession session = request.getSession();
		String nickname = session.getAttribute(AppConstants.USERNICKNAME).toString();
		//reading channel details from the request
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuilder jsonDetails =new StringBuilder();
		String line;
		while ((line = br.readLine()) !=null){
			jsonDetails.append(line);
		}

		JsonParser parser = new JsonParser();
		JsonObject jsonObject = parser.parse(jsonDetails.toString()).getAsJsonObject();

		String channelName = jsonObject.get("name").toString();
		ArrayList<User> list = AppVariables.activeUsersByChannel.get(channelName);
		Iterator itr = list.iterator();
		while (itr.hasNext()){
			User user = (User) itr.next();
			if (user.getUserNickname().equals(nickname))
				list.remove(user);
		}
		AppVariables.activeUsersByChannel.put(channelName, list);
		PrintWriter writer = response.getWriter();
		writer.println("user deleted from channels active list");
		writer.close();
	}

}
