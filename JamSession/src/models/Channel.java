package models;

public class Channel {
	
	private Type channelType;
	private String channelName;
	private int[] userList;
	private String channelDescription;
	
	//constructor which does not recieve a list of users of the channel as a parameter
	public Channel(Type channelType, String channelName, String channelDescription) {
		super();
		this.channelType = channelType;
		this.channelName = channelName;
		this.userList = userList;
		this.channelDescription = channelDescription;
	}
	//constructor which recieves a list of users of the channel as a parameter
	public Channel(Type channelType, String channelName, int[] userList, String channelDescription) {
		super();
		this.channelType = channelType;
		this.channelName = channelName;
		this.userList = userList;
		this.channelDescription = channelDescription;
	}
	
	public Type getChannelType() {
		return channelType;
	}
	public void setChannelType(Type channelType) {
		this.channelType = channelType;
	}
	public String getChannelName() {
		return channelName;
	}
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	public int[] getUserList() {
		return userList;
	}
	public void setUserList(int[] userList) {
		this.userList = userList;
	}
	public String getChannelDescription() {
		return channelDescription;
	}
	public void setChannelDescription(String channelDescription) {
		this.channelDescription = channelDescription;
	}
	
	
}
