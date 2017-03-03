/**
 * simple class that represents a message in a channel
 */
package proj.models;

import java.sql.Timestamp;

public class Message {
	
	private int id;
	private String author;
	private String authorPhotoUrl;
	private String channel;
	private String content;
	private boolean isThread;
	private int isReplyTo;
	private int threadID;
	private Timestamp lastUpdate;
	private Timestamp date;
	private int numberOfReplies;
	
	//message constructor to enter new message
	public Message(String author, String channel, String content, boolean isThread, int isReplyTo, int threadID,
			Timestamp date) {
		super();
		this.author = author;
		this.channel = channel;
		this.content = content;
		this.isThread = isThread;
		this.isReplyTo = isReplyTo;
		this.threadID = threadID;
		this.date = date;
		this.numberOfReplies = 0;
	}
	
	//message constructor for use when extracting messages data from database
	public Message(int id, String author, String channel, String content, boolean isThread, int isReplyTo, int threadID,
			Timestamp lastUpdate, Timestamp date) {
		super();
		this.id = id;
		this.author = author;
		this.channel = channel;
		this.content = content;
		this.isThread = isThread;
		this.isReplyTo = isReplyTo;
		this.threadID = threadID;
		this.lastUpdate = lastUpdate;
		this.date = date;
		this.numberOfReplies = 0;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isThread() {
		return isThread;
	}

	public void setThread(boolean isThread) {
		this.isThread = isThread;
	}

	public int getIsReplyTo() {
		return isReplyTo;
	}

	public void setIsReplyTo(int isReplyTo) {
		this.isReplyTo = isReplyTo;
	}

	public int getThreadID() {
		return threadID;
	}

	public void setThreadID(int threadID) {
		this.threadID = threadID;
	}

	public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}
	
	public String getAuthorPhotoUrl() {
		return authorPhotoUrl;
	}

	public void setAuthorPhotoUrl(String authorPhotoUrl) {
		this.authorPhotoUrl = authorPhotoUrl;
	}

	public int getNumberOfReplies() {
		return numberOfReplies;
	}

	public void addtoumberOfReplies() {
		this.numberOfReplies++;
	}
	
	

	


}
