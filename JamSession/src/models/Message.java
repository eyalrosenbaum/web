package models;

import java.sql.Date;

public class Message {
	private Date date;
	private String authorNickname;
	private String authorPhotoURL;
	private Message[] relpys;
	
	//constructor which does not recieve an array of messages as a parameter
	public Message(Date date, String authorNickname, String authorPhotoURL) {
		super();
		this.date = date;
		this.authorNickname = authorNickname;
		this.authorPhotoURL = authorPhotoURL;
	}
	//constructor which recieves an array of messages as a parameter
	public Message(Date date, String authorNickname, String authorPhotoURL, Message[] relpys) {
		super();
		this.date = date;
		this.authorNickname = authorNickname;
		this.authorPhotoURL = authorPhotoURL;
		this.relpys = relpys;
	}
	
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getAuthorNickname() {
		return authorNickname;
	}
	public void setAuthorNickname(String authorNickname) {
		this.authorNickname = authorNickname;
	}
	public String getAuthorPhotoURL() {
		return authorPhotoURL;
	}
	public void setAuthorPhotoURL(String authorPhotoURL) {
		this.authorPhotoURL = authorPhotoURL;
	}
	public Message[] getRelpys() {
		return relpys;
	}
	public void setRelpys(Message[] relpys) {
		this.relpys = relpys;
	}
}
