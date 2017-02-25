package models;

import java.sql.Timestamp;

public class PublicChannel extends Channel {
	private String channelDescription;
	int numberOfUsers;
	
	public PublicChannel(Type channelType, String channelName, String channelCreator, String channelDescription,
			Timestamp channelCreationTime) {
		super(channelType, channelName, channelCreator, channelCreationTime);
		this.channelDescription = channelDescription;
	}

	public String getChannelDescription() {
		return channelDescription;
	}

	public void setChannelDescription(String channelDescription) {
		this.channelDescription = channelDescription;
	}

	public int getNumberOfUsers() {
		return numberOfUsers;
	}

	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}

}
