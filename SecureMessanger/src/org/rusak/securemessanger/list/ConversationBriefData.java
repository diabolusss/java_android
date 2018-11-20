package org.rusak.securemessanger.list;

public class ConversationBriefData {
	private String 
		linkToContact,
		thumbnailURL,
		contactName,
		lastMessageCut
		;
	private int 
		firstMsgID,
		totalMessageCount
		;
	private long timestamp;

	public ConversationBriefData() {
	}

	public ConversationBriefData(String name, String thumbnailUrl, int firstMsgID, long timestamp) {
		this.setContactName(name);
		this.setThumbnail(thumbnailUrl);
		this.setFirstMsgID(firstMsgID);
		this.setTimestamp(timestamp);
	}

	public String getLinkToContact() {
		return linkToContact;
	}

	public void setLinkToContact(String linkToContact) {
		this.linkToContact = linkToContact;
	}

	public String getThumbnail() {
		return thumbnailURL;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnailURL = thumbnail;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public int getFirstMsgID() {
		return firstMsgID;
	}

	public void setFirstMsgID(int firstMsgID) {
		this.firstMsgID = firstMsgID;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getLastMessageCut() {
		return lastMessageCut;
	}

	public void setLastMessageCut(String lastMessageCut) {
		this.lastMessageCut = lastMessageCut;
	}

	public int getTotalMessageCount() {
		return totalMessageCount;
	}

	public void setTotalMessageCount(int totalMessageCount) {
		this.totalMessageCount = totalMessageCount;
	}

}
