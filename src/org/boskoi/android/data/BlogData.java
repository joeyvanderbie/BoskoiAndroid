package org.boskoi.android.data;

public class BlogData {
	
	private String title;
	private String date;
	private String description;;
	private String link;
	private boolean isUnread = false;
	private int id;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUnread(boolean isUnread) {
		this.isUnread = isUnread;
	}

	public boolean isUnread() {
		return isUnread;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
}
