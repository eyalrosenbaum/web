package listeners;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import models.Channel;
import models.Message;
import models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.sql.DataSource;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import JamSession.AppConstants;

/**
 * Application Lifecycle Listener implementation class ManageJamSessionDBFromJsonFile
 *
 */
@WebListener
public class ManageJamSessionDBFromJsonFile implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public ManageJamSessionDBFromJsonFile() {
        // TODO Auto-generated constructor stub
    }

    //utility that checks whether the customer tables already exists
    private boolean tableAlreadyExists(SQLException e) {
        boolean exists;
        if(e.getSQLState().equals("X0Y32")) {
            exists = true;
        } else {
            exists = false;
        }
        return exists;
    }
    
	
	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event)  { 
    	ServletContext cntx = event.getServletContext();
    	
    	try{
    		
    		//obtain CustomerDB data source from Tomcat's context
    		Context context = new InitialContext();
    		BasicDataSource ds = (BasicDataSource)context.lookup(
    				cntx.getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.OPEN);
    		Connection conn = ds.getConnection();
    		
    		
    		boolean created = false;
    		
    		/*Creating users table*/
    		try{
    			//create Users table
    			Statement stmt = conn.createStatement();
    			stmt.executeUpdate(AppConstants.CREATE_USERS_TABLE);
    			//commit update
        		conn.commit();
        		stmt.close();
    		}catch (SQLException e){
    			//check if exception thrown since table was already created (so we created the database already 
    			//in the past
    			created = tableAlreadyExists(e);
    			if (!created){
    				throw e;//re-throw the exception so it will be caught in the
    				//external try..catch and recorded as error in the log
    			}
    		}
    		
    		//if no users table exist in the past - further populate its records in the table
    		if (!created){
    			//populate customers table with customer data from json file
    			Collection<User> users = loadUsers(cntx.getResourceAsStream(File.separator +
    														   AppConstants.USERS_FILE));
    			PreparedStatement pstmt = conn.prepareStatement(AppConstants.INSERT_USER_STMT);
    			for (User user : users){
    				pstmt.setInt(1,user.getId());
    				pstmt.setString(2,user.getUserName());
    				pstmt.setString(3,user.getPassword());
    				pstmt.setString(4,user.getUserNickname());
    				pstmt.setString(5,user.getUserDescription());
    				pstmt.setString(6,user.getPhotoURL());
    				pstmt.executeUpdate();
    			}

    			//commit update
    			conn.commit();
    			//close statements
    			pstmt.close();
    		}
    		
    		
    		/*Creating channels table*/
    		created = false;
    		try{
    			//create Channels table
    			Statement stmt = conn.createStatement();
    			stmt.executeUpdate(AppConstants.CREATE_CHANNELS_TABLE);
    			//commit update
        		conn.commit();
        		stmt.close();
    		}catch (SQLException e){
    			//check if exception thrown since table was already created (so we created the database already 
    			//in the past
    			created = tableAlreadyExists(e);
    			if (!created){
    				throw e;//re-throw the exception so it will be caught in the
    				//external try..catch and recorded as error in the log
    			}
    		}
    		
    		//if no channels table exist in the past - further populate its records in the table
    		if (!created){
    			//populate customers table with customer data from json file
    			Collection<Channel> channels = loadChannels(cntx.getResourceAsStream(File.separator +
    														   AppConstants.CHANNELS_FILE));
    			PreparedStatement pstmt = conn.prepareStatement(AppConstants.INSERT_CHANNEL_STMT);
    			for (Channel channel : channels){
    				pstmt.setString(1,channel.getChannelType().toString());
    				pstmt.setString(2,channel.getChannelName());
    				pstmt.setString(3,channel.getChannelDescription());
    				pstmt.executeUpdate();
    			}

    			//commit update
    			conn.commit();
    			//close statements
    			pstmt.close();
    		}
    		
    		
    		/*Creating messages table*/
    		created = false;
    		try{
    			//create Messages table
    			Statement stmt = conn.createStatement();
    			stmt.executeUpdate(AppConstants.CREATE_MESSAGES_TABLE);
    			//commit update
        		conn.commit();
        		stmt.close();
    		}catch (SQLException e){
    			//check if exception thrown since table was already created (so we created the database already 
    			//in the past
    			created = tableAlreadyExists(e);
    			if (!created){
    				throw e;//re-throw the exception so it will be caught in the
    				//external try..catch and recorded as error in the log
    			}
    		}
    		
    		//if no messages table exist in the past - further populate its records in the table
    		if (!created){
    			//populate customers table with customer data from json file
    			Collection<Message> messages = loadMessages(cntx.getResourceAsStream(File.separator +
    														   AppConstants.MESSAGES_FILE));
    			PreparedStatement pstmt = conn.prepareStatement(AppConstants.INSERT_MESSAGE_STMT);
    			for (Message message : messages){
    				pstmt.setDate(1,message.getDate());
    				pstmt.setString(2,message.getAuthorNickname());
    				pstmt.setString(3,message.getAuthorPhotoURL());
    				pstmt.executeUpdate();
    			}

    			//commit update
    			conn.commit();
    			//close statements
    			pstmt.close();
    		}
    		

    		//close connection
    		conn.close();

    	} catch (IOException | SQLException | NamingException e) {
    		//log error 
    		cntx.log("Error during database initialization",e);
    	}
    }
	
    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent event)  { 
    		ServletContext cntx = event.getServletContext();
    	 
         //shut down database
    	 try {
     		//obtain CustomerDB data source from Tomcat's context and shutdown
     		Context context = new InitialContext();
     		BasicDataSource ds = (BasicDataSource)context.lookup(
     				cntx.getInitParameter(AppConstants.DB_DATASOURCE) + AppConstants.SHUTDOWN);
     		ds.getConnection();
     		ds = null;
		} catch (SQLException | NamingException e) {
			cntx.log("Error shutting down database",e);
		}
    }
    
    /**
	 * Loads users data from json file that is read from the input stream into 
	 * a collection of User objects
	 * @param is input stream to json file
	 * @return collection of users
	 * @throws IOException
	 */
	private Collection<User> loadUsers(InputStream is) throws IOException{
		
		//wrap input stream with a buffered reader to allow reading the file line by line
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder jsonFileContent = new StringBuilder();
		//read line by line from file
		String nextLine = null;
		while ((nextLine = br.readLine()) != null){
			jsonFileContent.append(nextLine);
		}

		Gson gson = new Gson();
		//this is a require type definition by the Gson utility so Gson will 
		//understand what kind of object representation should the json file match
		Type type = new TypeToken<Collection<User>>(){}.getType();
		Collection<User> users = gson.fromJson(jsonFileContent.toString(), type);
		//close
		br.close();	
		return users;

	}
	
	/**
	 * Loads channels data from json file that is read from the input stream into 
	 * a collection of Channel objects
	 * @param is input stream to json file
	 * @return collection of channels
	 * @throws IOException
	 */
	private Collection<Channel> loadChannels(InputStream is) throws IOException{
		
		//wrap input stream with a buffered reader to allow reading the file line by line
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder jsonFileContent = new StringBuilder();
		//read line by line from file
		String nextLine = null;
		while ((nextLine = br.readLine()) != null){
			jsonFileContent.append(nextLine);
		}

		Gson gson = new Gson();
		//this is a require type definition by the Gson utility so Gson will 
		//understand what kind of object representation should the json file match
		Type type = new TypeToken<Collection<Channel>>(){}.getType();
		Collection<Channel> channels = gson.fromJson(jsonFileContent.toString(), type);
		//close
		br.close();	
		return channels;

	}

	/**
	 * Loads messages data from json file that is read from the input stream into 
	 * a collection of Message objects
	 * @param is input stream to json file
	 * @return collection of messages
	 * @throws IOException
	 */
	private Collection<Message> loadMessages(InputStream is) throws IOException{
		
		//wrap input stream with a buffered reader to allow reading the file line by line
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder jsonFileContent = new StringBuilder();
		//read line by line from file
		String nextLine = null;
		while ((nextLine = br.readLine()) != null){
			jsonFileContent.append(nextLine);
		}

		Gson gson = new Gson();
		//this is a require type definition by the Gson utility so Gson will 
		//understand what kind of object representation should the json file match
		Type type = new TypeToken<Collection<Message>>(){}.getType();
		Collection<Message> messages = gson.fromJson(jsonFileContent.toString(), type);
		//close
		br.close();	
		return messages;

	}

}
