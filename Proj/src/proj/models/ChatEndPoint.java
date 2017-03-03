/**
 * chat end point - to connect and manage websocket transferred data
 */
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import porj.helpers.AppConstants;
import porj.helpers.AppVariables;


@ServerEndpoint("/chat/{nickname}")
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
    	System.out.println("websocking login called");
    		if (session.isOpen()) {
    			//add new user to managed chat sessions
    			chatUsers.put(session,nickname);
    	    	
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
    		System.out.println("deliverChatMessege  called");
    		if (session.isOpen()) {
    			JsonParser parser = new JsonParser();
    			JsonObject jsonObject = parser.parse(msg).getAsJsonObject();
    			String author = jsonObject.get("author").toString();
    			author = author.replaceAll("\"", "");
    			doNotify(author, msg, null);
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
    	System.out.println("websocking logout called");
    		String user = chatUsers.remove(session);//fake user just for removal
    		
    		
    }

    /*
     * Helper method for message delivery to chat participants. skip parameter is used to avoid delivering a message 
     * to a certain client (e.g., one that has just left) 
     */
    private void doNotify(String author, String message, Session skip) throws IOException{
    	for (Entry<Session,String> usernickname : chatUsers.entrySet()){
    		//send only to members of channel
    		if (usernickname.getValue().equals(author)){
	    		Session session = usernickname.getKey();
	    		if (!session.equals(skip) && session.isOpen()){
	    			session.getBasicRemote().sendText(message);
	    		}
    		}
    	}
    }


}
