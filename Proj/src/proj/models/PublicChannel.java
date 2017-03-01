package proj.models;

import java.sql.Timestamp;

public class PublicChannel extends Channel {
	private String channelDescription;
	int numberOfUsers;
	
	public PublicChannel(Type channelType, String channelName, String channelCreator, String channelDescription,
			Timestamp channelCreationTime) {
		super(channelType, channelName, channelCreator, channelCreationTime);
		this.channelDescription = channelDescription;
	}
	
	public PublicChannel(String channelName, String channelCreator, String channelDescription,
			Timestamp channelCreationTime) {
		super(proj.models.Type.PUBLIC, channelName, channelCreator, channelCreationTime);
		this.channelDescription = channelDescription;
	}

	public PublicChannel(String channelName, String channelCreator, String channelDescription) {
		super(proj.models.Type.PUBLIC, channelName, channelCreator);
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
