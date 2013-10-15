package com.cs4274.news_butler;
import com.google.gson.annotations.SerializedName;

public class NewsItem {
	
	@SerializedName("__metadata")
	public Object __metadata; 
	
	@SerializedName("ID")
	public String id;
	
	@SerializedName("Title")
	public String title;
	
	@SerializedName("Url")
	public String url;
	
	@SerializedName("Source")
	public String source;
	
	@SerializedName("Description")
	public String description;
	
	@SerializedName("Date")
	public String date;
	
	
	public NewsItem(Object __metadata, String id, String title, String url, String source,
			String description, String date) {
		this.__metadata = __metadata;
		this.id = id;
		this.title = title;
		this.url = url;
		this.source = source;
		this.description = description;
		this.date = date;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public Object get__metadata() {
		return __metadata;
	}
	public void set__metadata(String __metadata) {
		this.__metadata = __metadata;
	}
	
	
	
}
