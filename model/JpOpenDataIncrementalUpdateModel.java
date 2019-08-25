package jpo.util.crawler.model;


import org.jongo.marshall.jackson.oid.MongoId;

public class JpOpenDataIncrementalUpdateModel {
	
	@MongoId
	private String checkSum;
	private String fileName;
	private String title;
	private String publicDate;
	private String accumulatedTimes;
	private String downloadUrl;
	private String fileSize;
	private Boolean downloaded;
	private String downloadDate;
	private String downloadPath;
	private Boolean forFixError;
	private String fixStatus;
	private Boolean newestVersion;
	
	
	public String getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
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
	public Boolean getDownloaded() {
		return downloaded;
	}
	public void setDownloaded(Boolean downloaded) {
		this.downloaded = downloaded;
	}

	public String getDownloadDate() {
		return downloadDate;
	}
	public void setDownloadDate(String downloadDate) {
		this.downloadDate = downloadDate;
	}
	public Boolean getForFixError() {
		return forFixError;
	}
	public void setForFixError(Boolean forFixError) {
		this.forFixError = forFixError;
	}
	public String getFixStatus() {
		return fixStatus;
	}
	public void setFixStatus(String fixStatus) {
		this.fixStatus = fixStatus;
	}
	public Boolean getNewestVersion() {
		return newestVersion;
	}
	public void setNewestVersion(Boolean newestVersion) {
		this.newestVersion = newestVersion;
	}

	public String getDownloadPath() {
		return downloadPath;
	}
	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}


}
