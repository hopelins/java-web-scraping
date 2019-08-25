package jpo.util.crawler.model;

import java.util.Date;

import org.jongo.marshall.jackson.oid.MongoId;

public class JpDesignFullPageCrawlerLog {

	@MongoId
	private String patId; // query from postgre
	private String gazette_public_date; // query from postgre
	private String docNo; // gen from patId
//	private Integer totalPage; // get from webpage
	private String requestURL; // get from webpage
	private Boolean downloaded; // true when successfully downloaded
	private Date downloadDate;

	public String getPatId() {
		return patId;
	}

	public void setPatId(String patId) {
		this.patId = patId;
	}

	public String getGazette_public_date() {
		return gazette_public_date;
	}

	public void setGazette_public_date(String gazette_public_date) {
		this.gazette_public_date = gazette_public_date;
	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public Boolean getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(Boolean downloaded) {
		this.downloaded = downloaded;
	}

	public Date getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(Date downloadDate) {
		this.downloadDate = downloadDate;
	}

}
