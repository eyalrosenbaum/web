package JamSession;

import java.lang.reflect.Type;
import java.util.Collection;
import com.google.gson.reflect.TypeToken;
import models.User;

public interface AppConstants {
	public final String USERS = "users";
	public final String USER_ID = "userID";
	public final String USERS_FILE = USERS + ".json";
	public final String NAME = "name";
	public final Type USER_COLLECTION = new TypeToken<Collection<User>>() {}.getType();
	//derby constants
	public final String DB_NAME = "DB_NAME";
	public final String DB_DATASOURCE = "DB_DATASOURCE";
	public final String PROTOCOL = "jdbc:derby:"; 
	public final String OPEN = "Open";
	public final String SHUTDOWN = "Shutdown";
	
	//sql statements
	public final String CREATE_USERS_TABLE = "CREATE TABLE USERS(Name varchar(100),"
			+ "City varchar(100),"
			+ "Country varchar(100))";
	public final String INSERT_USER_STMT = "INSERT INTO USERS VALUES(?,?,?,?,?,?)";
	public final String SELECT_ALL_USERS_STMT = "SELECT * FROM USERS";
	public final String SELECT_USER_BY_NAME_STMT = "SELECT * FROM USERS "
			+ "WHERE Name=?";
	
	public final String CHANNELS = "channels";
	public final String CREATE_CHANNELS_TABLE = "CREATE TABLE CHANNELS(Name varchar(100),"
			+ "City varchar(100),"
			+ "Country varchar(100))";
	public final String CHANNELS_FILE = CHANNELS + ".json";
	public final String INSERT_CHANNEL_STMT = "INSERT INTO CHANNELS VALUES(?,?,?)";
	
	public final String MESSAGES = "messages";
	public final String CREATE_MESSAGES_TABLE = "CREATE TABLE MESSAGES(Name varchar(100),"
			+ "City varchar(100),"
			+ "Country varchar(100))";
	public final String MESSAGES_FILE = MESSAGES + ".json";
	public final String INSERT_MESSAGE_STMT = "INSERT INTO MESSAGES VALUES(?,?,?)";
	
	public final String SELECT_USER_BY_USERNAME_AND_PASS_STMT = "SELECT * FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";
	public final String SELECT_USER_BY_USERNAME_STMT = "SELECT * FROM USERS WHERE USERNAME = ?";
	public final String SELECT_TOP_USERID_STMT = "SELECT MAX(ID) FROM USERS";
	public final String GENERAL_CHANNEL = "the general channel";
	public final String GENERAL_CHANNEL_DESC = "this channel is a general channel which is open for all users";
	public final String UPDATE_CHANNEL_USER = "INSERT INTO CHANNELUSERS VALUES(?,?,?)";
	
}
