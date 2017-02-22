package models;

import java.sql.Timestamp;
import java.util.ArrayList;

public class PrivateChannel extends Channel {

	private ArrayList<String> participants;
	
	public ArrayList<String> getParticipants() {
		return participants;
	}

	public void setParticipants(ArrayList<String> participants) {
		this.participants = participants;
	}

	public PrivateChannel(Type channelType, String channelName, String channelCreator, Timestamp channelCreationTime) {
		super(channelType, channelName, channelCreator, channelCreationTime);
		// TODO Auto-generated constructor stub
	}

}
