package models;

import java.sql.Timestamp;

public abstract class Channel {

	private String channelName;
	private Type channelType;
	private String channelCreator;
	private Timestamp channelCreationTime;

	public Channel(Type channelType, String channelName, String channelCreator, 
			Timestamp channelCreationTime) {
		super();
		this.channelType = channelType;
		this.channelName = channelName;
		this.channelCreator = channelCreator;
		this.channelCreationTime = channelCreationTime;
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
	
	public String getChannelCreator() {
		return channelCreator;
	}

	public void setChannelCreator(String channelCreator) {
		this.channelCreator = channelCreator;
	}

	public Timestamp getChannelCreationTime() {
		return channelCreationTime;
	}

	public void setChannelCreationTime(Timestamp channelCreationTime) {
		this.channelCreationTime = channelCreationTime;
	}



}
