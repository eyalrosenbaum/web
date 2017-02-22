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
import models.Subscription;
import models.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

import JamSession.AppConstants;
import JamSession.AppVariables;
import JamSession.DatabaseConnection;

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
    @Override
	public void contextInitialized(ServletContextEvent event)  { 
    	ServletContext cntx = event.getServletContext();
    	
    	try {
    		if (AppVariables.db  == null)
    			DatabaseConnection.createDB(cntx);
		} catch (NamingException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	try{
    		
    		//obtain JamSessionDB data source from Tomcat's context
    		Connection conn = AppVariables.db.getConnection();
    		
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
    				pstmt.setString(1,user.getUserName());
    				pstmt.setString(2,user.getPassword());
    				pstmt.setString(3,user.getUserNickname());
    				pstmt.setString(4,user.getUserDescription());
    				pstmt.setString(5,user.getPhotoURL());
    				pstmt.setBoolean(6, false);
    				pstmt.setTimestamp(7, user.getLastlogged());
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
    				pstmt.setString(1,channel.getChannelName());
    				pstmt.setString(2,channel.getChannelType().toString());
    				if (channel.getChannelType().equals(models.Type.PUBLIC))
    					pstmt.setString(3,((models.PublicChannel) channel).getChannelDescription());
    				else pstmt.setString(3,"");
    				pstmt.setString(4,channel.getChannelCreator());
    				pstmt.setTimestamp(5, channel.getChannelCreationTime());
    				if(channel.getChannelType().equals(models.Type.PRIVATE)){
    					pstmt.setString(6, ((models.PrivateChannel) channel).getParticipants().get(0));
    					pstmt.setString(7, ((models.PrivateChannel) channel).getParticipants().get(1));
    				}
    					
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
    				pstmt.setInt(1, message.getId());
    				pstmt.setString(2, message.getAuthor());
    				pstmt.setString(3, message.getChannel());
    				pstmt.setString(4, message.getContent());
    				pstmt.setBoolean(5, message.isThread());
    				pstmt.setInt(6, message.getIsReplyTo());
    				pstmt.setInt(7, message.getThreadID());
    				pstmt.setTimestamp(8, message.getLastUpdate());
    				pstmt.setTimestamp(9, message.getDate());
    				pstmt.executeUpdate();
    			}

    			//commit update
    			conn.commit();
    			//close statements
    			pstmt.close();
    		}
    		
    		
    		/*Creating subscriptions table*/
    		created = false;
    		try{
    			//create subscriptions table
    			Statement stmt = conn.createStatement();
    			stmt.executeUpdate(AppConstants.CREATE_SUBSCRIPTIONS_TABLE);
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
    		
    		//if no subscriptions table exist in the past - further populate its records in the table
    		if (!created){
    			//populate subscriptions table with subscriptions data from json file
    			Collection<Subscription> subscriptions = loadSubscriptions(cntx.getResourceAsStream(File.separator +
    														   AppConstants.SUBSCRIPTIONS_FILE));
    			PreparedStatement pstmt = conn.prepareStatement(AppConstants.INSERT_SUBSCRIPTIONS);
    			for (Subscription subscription : subscriptions){
    				pstmt.setInt(1, subscription.getId());
    				pstmt.setString(2, subscription.getUsername());
    				pstmt.setString(3, subscription.getChannel());
    				pstmt.setString(4, subscription.getType().toString());
    				pstmt.executeUpdate();
    			}

    			//commit update
    			conn.commit();
    			//close statements
    			pstmt.close();
    		}

    		//close connection
    		conn.close();

    	} catch (IOException | SQLException e) {
    		//log error 
    		cntx.log("Error during database initialization",e);
    	}
    }
	
    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    @Override
	public void contextDestroyed(ServletContextEvent event)  { 
    		ServletContext cntx = event.getServletContext();
    	 
         //shut down database
    	 try {
     		//obtain CustomerDB data source from Tomcat's context and shutdown
     		Context context = new InitialContext();
     		BasicDataSource ds = (BasicDataSource)context.lookup("java:comp/env/jdbc/JamSessionDatasource");
    // 				cntx.getInitParameter(AppConstants.DB_NAME) + AppConstants.SHUTDOWN);
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
	
private Collection<Subscription> loadSubscriptions(InputStream is) throws IOException{
		
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
		Type type = new TypeToken<Collection<Subscription>>(){}.getType();
		Collection<Subscription> subscriptions = gson.fromJson(jsonFileContent.toString(), type);
		//close
		br.close();	
		return subscriptions;

	}

}
