package models;

public class Subscription {
	private int id;
	private String username;
	private String channel;
	private Type type;
	
	//constructor to create new subscription of user to channel
	public Subscription(String username, String channel,Type type) {
		super();
		this.username = username;
		this.channel = channel;
		this.type = type;
	}

	//constructor to extract subscription data from table
	public Subscription(int id, String username, String channel,Type type) {
		super();
		this.id = id;
		this.username = username;
		this.channel = channel;
		this.type = type;
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
	
	
	
	
}
