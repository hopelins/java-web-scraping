package jpo.util.crawler.model;

import org.jongo.marshall.jackson.oid.MongoId;

public class JpoOpenDataIncrementalModel {
	
	@MongoId
	private String md5;
	private String fileName;
	private String title;
	private String publicDate;
	private String dataType;
	private String accumulatedTimes;
	private String downloadUrl;
	private String fileSize;
	
	private Boolean downloaded;
	private String updateDate;
	
	
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPublicDate() {
		return publicDate;
	}
	public void setPublicDate(String publicDate) {
		this.publicDate = publicDate;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getAccumulatedTimes() {
		return accumulatedTimes;
	}
	public void setAccumulatedTimes(String accumulatedTimes) {
		this.accumulatedTimes = accumulatedTimes;
	}
	public String getDownloadUrl() {
		return downloadUrl;
	}
	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public Boolean getDownloaded() {
		return downloaded;
	}
	public void setDownloaded(Boolean downloaded) {
		this.downloaded = downloaded;
	}

	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	
	
	

}
