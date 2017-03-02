package proj.models;

import java.sql.Timestamp;

public class User {
	private String userName;
	private String password;
	private String userNickname;
	private String userDescription;
	private String photoURL;
	private boolean islogged;
	private Timestamp lastlogged;
	private Timestamp lastlastlogged;
	
	

	//constructor for creating new user
	public User(String userName, String password, String userNickname, String userDescription, String photoURL) {
		super();
		this.userName = userName;
		this.password = password;
		this.userNickname = userNickname;
		this.userDescription = userDescription;
		this.photoURL = photoURL;
		this.islogged = false;
		this.lastlogged = null;
	}

	//constructor for extracting user data from database
	public User(String userName, String password, String userNickname, String userDescription, String photoURL,
			boolean islogged, Timestamp lastlogged) {
		super();
		this.userName = userName;
		this.password = password;
		this.userNickname = userNickname;
		this.userDescription = userDescription;
		this.photoURL = photoURL;
		this.islogged = islogged;
		this.lastlogged = lastlogged;
	}

	public User(String errorMsg) {
		this.userName = errorMsg;
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

	public boolean isIslogged() {
		return islogged;
	}

	public void setIslogged(boolean islogged) {
		this.islogged = islogged;
	}

	public Timestamp getLastlogged() {
		return lastlogged;
	}

	public void setLastlogged(Timestamp lastlogged) {
		this.lastlogged = lastlogged;
	}

	public Timestamp getLastlastlogged() {
		return lastlastlogged;
	}

	public void setLastlastlogged(Timestamp lastlastlogged) {
		this.lastlastlogged = lastlastlogged;
	}	

	
}
