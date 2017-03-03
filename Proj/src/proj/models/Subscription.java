/**
 * simple class to mark a subscription of a user to a public or private chat
 */
package proj.models;

import java.sql.Timestamp;

public class Subscription {
	private int id;
	private String username;
	private String channel;
	private Type type;
	private Timestamp date;
	

	//constructor to create new subscription of user to channel
	public Subscription(String username, String channel,String type) {
		super();
		this.username = username;
		this.channel = channel;
		if (type.equals("public"))
			this.type = proj.models.Type.PUBLIC;
		else this.type = proj.models.Type.PRIVATE;
	}

	//constructor to extract subscription data from table
	public Subscription(int id, String username, String channel,String type) {
		super();
		this.id = id;
		this.username = username;
		this.channel = channel;
		if (type.equals("public"))
			this.type = proj.models.Type.PUBLIC;
		else this.type = proj.models.Type.PRIVATE;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	
	

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}
	
}
