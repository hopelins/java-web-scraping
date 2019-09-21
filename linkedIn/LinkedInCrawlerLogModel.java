package linkedIn;

import org.jongo.marshall.jackson.oid.MongoId;

public class LinkedInCrawlerLogModel {

	@MongoId
	private String Id;
	private String Excepetion;
	
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	public String getExcepetion() {
		return Excepetion;
	}
	public void setExcepetion(String excepetion) {
		Excepetion = excepetion;
	}
	
}
