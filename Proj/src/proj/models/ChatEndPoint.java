package proj.models;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.DecodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;


@ServerEndpoint(
		value = "/JamSession/{nickname}",
		decoders = MsgDecoder.class,
		encoders = MsgEncoder.class)
public class ChatEndPoint{
	
	//tracks all active chat users
    private static Map<Session,String> chatUsers = Collections.synchronizedMap(new HashMap<Session,String>()); 
    MsgDecoder decoder= new MsgDecoder();
    /**
     * Joins a new user to the chat on login to site
     * @param session 
     * 			client end point session
     * @throws IOException
     * @throws SQLException 
     */
    @OnOpen
    public void login(Session session, @PathParam("nickname") String nickname) throws IOException, SQLException{
    	//try {
    		if (session.isOpen()) {
    			//add new user to managed chat sessions
    	    	chatUsers.put(session,nickname);
    	    	Connection conn = AppVariables.db.getConnection();
    	    	Timestamp time = new Timestamp(System.currentTimeMillis());
    			PreparedStatement stmt;
    			stmt = conn.prepareStatement(AppConstants.UPDATE_LOGGED_USER_STMT);
    			stmt.setTimestamp(1, time);
    			stmt.setString(1, nickname);
    			stmt.executeUpdate();
    			conn.commit();
    			stmt.close();
    			conn.close();
    		}
    }
    
    /**
     * Message delivery between chat participants
     * @param session
     * 			client end point session
     * @param msg
     * 			message to deliver		
     * @throws IOException
     * @throws DecodeException 
     * @throws SQLException 
     */
    @OnMessage
    public void deliverChatMessege(Session session, String msg) throws IOException, DecodeException, SQLException{
    	try {
    		if (session.isOpen()) {
    			//deliver message only to users who are active in this channel
    			String user = chatUsers.get(session);
    			User sender = null;
    			Message message = decoder.decode(msg);
    			message.setDate(new Timestamp(System.currentTimeMillis()));
    			message.setLastUpdate(message.getDate());
    			ArrayList<User> channelUsers = AppVariables.usersByChannel.get(message.getChannel());
    			//now we will send the message to all users in the channel
    			for (User channelUser : channelUsers){
    				doNotify(channelUser.getUserNickname(), msg, null);
    			}
    			//insert message into database
    			Connection conn = AppVariables.db.getConnection();
    			PreparedStatement stmt;
    			//stmt = conn.prepareStatement(AppConstants.INSERT_MESSAGE_STMT);
    			stmt = conn.prepareStatement(AppConstants.INSERT_MESSAGE_STMT);
    			stmt.setString(1, message.getAuthor());
    			stmt.setString(2, message.getChannel());
    			stmt.setString(3, message.getContent());
    			stmt.setBoolean(4, message.isThread());
    			stmt.setInt(5, message.getIsReplyTo());
    			stmt.setInt(6, message.getThreadID());
    			stmt.setTimestamp(7, message.getLastUpdate());
    			stmt.setTimestamp(8, message.getDate());
    			stmt.executeUpdate();
    			conn.commit();
    			stmt.close();
    			conn.close();

    		}

    	} catch (IOException e) {
    		session.close();
    	}
    }
    
    /**
     * Removes a client from the chat
     * @param session
     * 			client end point session
     * @throws IOException
     * @throws SQLException 
     */
    @OnClose
    public void logOut(Session session) throws IOException, SQLException{
    //	try {
    		String user = chatUsers.remove(session);//fake user just for removal
    		Connection conn = AppVariables.db.getConnection();
			PreparedStatement stmt;
			Timestamp time = new Timestamp(System.currentTimeMillis());
			//stmt = conn.prepareStatement(AppConstants.INSERT_MESSAGE_STMT);
			stmt = conn.prepareStatement(AppConstants.UPDATE_UNLOGGED_USER_STMT);
			stmt.setTimestamp(1, time);
			stmt.setString(2, user);
			stmt.executeUpdate();
			conn.commit();
			stmt.close();
			conn.close();
    		
    }

    /*
     * Helper method for message delivery to chat participants. skip parameter is used to avoid delivering a message 
     * to a certain client (e.g., one that has just left) 
     */
    private void doNotify(String usernickname, String message, Session skip) throws IOException{
    	for (Entry<Session,String> user : chatUsers.entrySet()){
    		//send only to members of channel
    		if (user.getValue().equals(usernickname)){
	    		Session session = user.getKey();
	    		if (!session.equals(skip) && session.isOpen()){
	    			session.getBasicRemote().sendText(message);
	    		}
    		}
    	}
    }


}
