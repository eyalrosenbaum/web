package models;

import java.util.ArrayList;
import java.util.Iterator;

public class User {
	private int id;
	private String userName;
	private String password;
	private String userNickname;
	private String userDescription;
	private String photoURL;
	private ArrayList<Channel> userPrivateChannels;
	private ArrayList<Channel> userPublicChannels;
	
	//constructor which does not recieve userID and arraylist of user's channels
		public User(String userName, String password, String userNickname, String userDescription, String photoURL) {
			super();
			this.id = 0;
			this.userName = userName;
			this.password = password;
			this.userNickname = userNickname;
			this.userDescription = userDescription;
			this.photoURL = photoURL;
		}
	//constructor which does not recieve arraylist of user's channels
	public User(int id, String userName, String password, String userNickname, String userDescription, String photoURL) {
		super();
		this.id = id;
		this.userName = userName;
		this.password = password;
		this.userNickname = userNickname;
		this.userDescription = userDescription;
		this.photoURL = photoURL;
	}
	//constructor which recieves arraylist of user's channels
	public User(int id, String userName, String password, String userNickname, String userDescription, String photoURL,
			ArrayList<Channel> userPrivateChannels, ArrayList<Channel> userPublicChannels) {
		super();
		this.id = id;
		this.userName = userName;
		this.password = password;
		this.userNickname = userNickname;
		this.userDescription = userDescription;
		this.photoURL = photoURL;
		this.userPrivateChannels = userPrivateChannels;
		this.userPublicChannels = userPublicChannels;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUserNickname() {
		return userNickname;
	}
	public void setUserNickname(String userNickname) {
		this.userNickname = userNickname;
	}
	public String getUserDescription() {
		return userDescription;
	}
	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}
	public String getPhotoURL() {
		return photoURL;
	}
	public void setPhotoURL(String photoURL) {
		this.photoURL = photoURL;
	}
	public ArrayList<Channel> getUserPrivateChannels() {
		return userPrivateChannels;
	}
	public void setUserPrivateChannels(ArrayList<Channel> userPrivateChannels) {
		this.userPrivateChannels = userPrivateChannels;
	}
	
	public void addChannel(Channel newChannel){
		if (newChannel.getChannelType()==Type.PRIVATE)
			this.userPrivateChannels.add(newChannel);
		else this.userPublicChannels.add(newChannel);
	}
	
	public void removeChannel(Channel channelToRemove){
		if (channelToRemove.getChannelType()==Type.PRIVATE){
			Iterator itr = this.getUserPrivateChannels().iterator();
			while (itr.hasNext()){
				Channel channel = (Channel) itr.next();
				if (channel.equals(channelToRemove))
					this.getUserPrivateChannels().remove(channel);
				}
		}
		else{
			Iterator itr = this.getUserPublicChannels().iterator();
			while (itr.hasNext()){
				Channel channel = (Channel) itr.next();
				if (channel.equals(channelToRemove))
					this.getUserPublicChannels().remove(channel);
			
			}
		}
	}
	public ArrayList<Channel> getUserPublicChannels() {
		return userPublicChannels;
	}
	public void setUserPublicChannels(ArrayList<Channel> userPublicChannels) {
		this.userPublicChannels = userPublicChannels;
	}
	
}
