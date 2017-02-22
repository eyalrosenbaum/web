package models;

import java.sql.Timestamp;

public class PublicChannel extends Channel {
	private String channelDescription;
	
	public PublicChannel(Type channelType, String channelName, String channelCreator, String channelDescription,
			Timestamp channelCreationTime) {
		super(channelType, channelDescription, channelDescription, channelCreationTime);
		this.channelDescription = channelDescription;
	}

	public String getChannelDescription() {
		return channelDescription;
	}

	public void setChannelDescription(String channelDescription) {
		this.channelDescription = channelDescription;
	}

}
