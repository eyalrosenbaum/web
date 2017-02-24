package JamSession;

import java.lang.reflect.Type;
import java.util.Collection;
import com.google.gson.reflect.TypeToken;

import models.Message;
import models.PrivateChannel;
import models.Subscription;
import models.User;

public interface AppConstants {

	public final String USERS = "users";
	public final String CHANNELS = "channels";
	public final String MESSAGES = "messages";
	public final String SUBSCRIPTIONS = "subscriptions";
	public final String USERNAME = "username";
	public final String USERS_FILE = USERS + ".json";
	public final String CHANNELS_FILE = CHANNELS + ".json";
	public final String MESSAGES_FILE = MESSAGES + ".json";
	public final String SUBSCRIPTIONS_FILE = SUBSCRIPTIONS + ".json";
	public final String NAME = "name";
	public final Type USER_COLLECTION = new TypeToken<Collection<User>>() {}.getType();
	public final Type SUBSCRIPTION_COLLECTION = new TypeToken<Collection<Subscription>>() {}.getType();
	public final Type PRIVATE_CHANNELS_COLLECTION = new TypeToken<Collection<PrivateChannel>>() {}.getType();
	public final Type MESSAGE_COLLECTION = new TypeToken<Collection<Message>>() {}.getType();;
	//derby constants
	public final String DB_NAME = "JamSessionDB";
	public final String DB_DATASOURCE = "JamSessionDatasource";
	public final String PROTOCOL = "jdbc:derby:"; 
	public final String OPEN = "Open";
	public final String SHUTDOWN = "Shutdown";
	public final String GENERAL_CHANNEL = "General Chat";
	
	//sql statements
	public final String CREATE_USERS_TABLE = "CREATE TABLE USERS("
			+ "username varchar(10) not null primary key,"
			+ "password varchar(8) not null,"
			+ "usernickname varchar(20) not null unique,"
			+ "shortdescription varchar (50),"
			+ "photourl varchar (100),"
			+ "islogged boolean not null,"
			+ "lastlogged timestamp not null)";
	
	public final String INSERT_USER_STMT = "INSERT INTO USERS VALUES(?,?,?,?,?,?,?)";
	public final String SELECT_ALL_USERS_STMT = "SELECT * FROM USERS";

	public final String CREATE_CHANNELS_TABLE = "CREATE TABLE CHANNELS("
			+ "name varchar(100) primary key,"
			+ "type varchar(100),"
			+ "description varchar(100),"
			+ "creator varchar(10) not null,"
			+ "created timestamp not null,"
			+ "participanta varchar(20),"
			+ "participantb varchar(20))";
	
	public final String INSERT_CHANNEL_STMT = "INSERT INTO CHANNELS VALUES(?,?,?,?,?,?,?)";
	
	public final String CREATE_MESSAGES_TABLE = "CREATE TABLE MESSAGES("
			+ "id integer not null generated always as identity (start with 1, increment by 1) primary key,"
			+ "author not null varchar(20),"
			+ "channel varchar(100),"
			+ "content varchar(500),"
			+ "isthread boolean not null,"
			+ "isreplyto integer default 0,"
			+ "threadid integer not null,"
			+ "lastupdate timestamp not null,"
			+ "date timestamp not null)";
	
	public final String INSERT_MESSAGE_STMT = "INSERT INTO MESSAGES VALUES(?,?,?,?,?,?,?,?)";
	
	public final String CREATE_SUBSCRIPTIONS_TABLE = "CREATE TABLE SUBSCRIPTIONS("
			+ "id integer not null generated always as identity (start with 1, increment by 1) primary key,"
			+ "username varchar(10) not null references users(username) on delete cascade,"
			+ "channel varchar(100) not null references channels(name) on delete cascade),"
			+ "type varchar(10) not null)";
	
	public final String INSERT_SUBSCRIPTIONS = "INSERT INTO SUBSCRIPTIONS VALUES(?,?,?)";
	
	public final String UPDATE_LOGGED_USER_STMT = "UPDATE USERS SET ISLOGGED = TRUE, LASTLOGGED = ? WHERE USERNICKNAME = ?";
	public final String UPDATE_UNLOGGED_USER_STMT = "UPDATE USERS SET ISLOGGED = FALSE, LASTLOGGED = ? WHERE USERNICKNAME = ?";
	public final String SELECT_USER_BY_USERNAME_AND_PASS_STMT = "SELECT * FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";
	public final String SELECT_USER_BY_USERNAME_STMT = "SELECT * FROM USERS WHERE USERNAME = ?";
	public final String SELECT_TOP_USERID_STMT = "SELECT MAX(ID) FROM USERS";
	public final String SELECT_SUBSCRIPTIONS_BY_USERNAME = "SELECT * FROM SUBSCRIPTIONS WHERE USERNAME = ?";
	public final String SELECT_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL = "SELECT * FROM SUBSCRIPTIONS WHERE USERNAME = ? AND CHANNEL = ?";
	public final String DELETE_SUBSCRIPTIONS_BY_USERNAME_AND_CHANNEL = "DELETE FROM SUBSCRIPTIONS WHERE USERNAME = ? AND CHANNEL = ?";
	public final String SELECT_CHANNEL_BY_NAME_AND_CREATED = "SELECT * FROM CHANNELS WHERE NAME = ? AND CREATED = ?";
	public final String DELETE_CHANNEL_STMT = "DELETE FROM CHANNELS WHERE NAME = ? AND CREATED = ?";
	public final String SELECT_CHANNEL_BY_NAME_AND_TYPE =  "SELECT * FROM CHANNELS WHERE NAME = ? AND TYPE = ?";
	public final String SELECT_USER_BY_NICKNAME_STMT = "SELECT * FROM USERS WHERE USERNICKNAME = ?";
	public final String SELECT_PRIVATE_CHANNELS_BY_NICKNAME = "SELECT * FROM CHANNELS WHERE PARTICIPANTA = ? OR PARTICIPANTB = ?";
	public final String DELETE_PRIVATE_CHANNEL_BY_NICKNAME = "DELETE FROM CHANNELS WHERE PARTICIPANTA = ? AND PARTICIPANTB = ?";
	public final String SELECT_PRIVATE_CHANNEL_BY_NICKNAME = "SELECT * FROM CHANNELS WHERE PARTICIPANTA = ? AND PARTICIPANTB = ?";
	public final String SELECT_THREADS_BY_CHANNEL = "SELECT * FROM MESSAGES WHERE CHANNEL = ? AND ISTHREAD = TRUE ORDER BY LASTUPDATE ASC" ;
	public final String SELECT_THREADS_BY_CHANNEL_DESC = "SELECT * FROM MESSAGES WHERE CHANNEL = ? AND ISTHREAD = TRUE ORDER BY LASTUPDATE DESC" ;
	public final String SELECT_MESSAGES_BY_REPLYTO_STMT = "SELECT * FROM MESSAGES WHERE ISREPLYTO = ?";
	public final String SELECT_MESSAGES_BY_AUTHOR_AND_DATE_STMT = "SELECT * FROM MESSAGES WHERE AUTHOR = ? AND DATE = ?";
	public final String SELECT_MESSAGES_BY_ID_STMT = "SELECT * FROM MESSAGES WHERE ID = ?";
	public final String UPDATE_LAST_UPDATE_STMT = "UPDATE MESSAGES SET LASTUPDATE = ? WHERE THREADID = ?";
	public final String UPDATE_THREAD_THREADID_STMT = "UPDATE MESSAGES SET THREADID = ? WHERE ID = ?";
	public final String SELECT_MESSAGES_BY_DATEL_AND_CHANNEL = "SELECT * FROM MESSAGES WHERE CHANNEL = ? AND DATE > ?";
	public final String SELECT_MESSAGES_BY_DATE_AND_NICKNAME_AND_CHANNEL = "SELECT * FROM MESSAGES WHERE CHANNEL = ? AND DATE > ? AND CONTENT LIKE ?";
	public final String SELECT_THREADS_BY_CHANNEL_AND_DATE_DESC = "SELECT * FROM MESSAGES WHERE CHANNEL = ? AND ISTHREAD = TRUE AND LASTUPDATE < ? ORDER BY LASTUPDATE ASC" ;
	public final String SELECT_THREADS_BY_CHANNEL_AND_DATE_ASC = "SELECT * FROM MESSAGES WHERE CHANNEL = ? AND ISTHREAD = TRUE AND LASTUPDATE > ? ORDER BY LASTUPDATE ASC" ;


	
}
