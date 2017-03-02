package proj.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import porj.helpers.AppConstants;
import proj.models.User;

/**
 * Servlet implementation class GetSessionDetailsServlet
 */
@WebServlet("/GetSessionDetailsServlet")
public class GetSessionDetailsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetSessionDetailsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		Gson gson = new Gson();
		PrintWriter writer = response.getWriter();
		//setting session attribute - username for future actions
		String username = null;
		String usernickname = null;
		String userDescription = null;
		String userPhotoURL = null;
		String password = "pass";
		Timestamp lastlog = null;
		Timestamp lastlastlog = null;
		/*trying to fetch user details - if not exist if does not take place*/
		if (session.getAttribute(AppConstants.USERNAME)!= null){
			username = session.getAttribute(AppConstants.USERNAME).toString();
			usernickname = session.getAttribute(AppConstants.USERNICKNAME).toString();
			userDescription = session.getAttribute(AppConstants.DESCRIPTION).toString();
			userPhotoURL = session.getAttribute(AppConstants.PHOTOURL).toString();
			lastlog = (Timestamp) session.getAttribute(AppConstants.LASTLOG);
			lastlastlog = (Timestamp) session.getAttribute(AppConstants.LASTLASTLOG);
			
			User user = new User(username,password,usernickname,userDescription,userPhotoURL);
//			long s = Long.parseLong(lastlog);
//			Date dater = new Date(s);
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(dater);
//			cal.set(Calendar.MILLISECOND, 0);
//			
//			Timestamp lastlogdate = new Timestamp(cal.getTimeInMillis());
//			user.setLastlogged(lastlogdate);
//			s = Long.parseLong(lastlastlog);
//			dater = new Date(s);
//			cal.setTime(dater);
//			cal.set(Calendar.MILLISECOND, 0);
//			Timestamp lastlastlogdate = new Timestamp(cal.getTimeInMillis());
//			user.setLastlastlogged(lastlastlogdate);
			
			//convert from subscriptions collection to json
			user.setLastlogged(lastlog);
			user.setLastlastlogged(lastlastlog);
			String userJsonResult = gson.toJson(user, User.class);
			writer.println(userJsonResult);
			writer.close();
		}
		else{
			writer.println("fail");
			writer.close();
		}
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
