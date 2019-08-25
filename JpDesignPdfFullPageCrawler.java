package jpo.util.crawler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jpo.util.crawler.model.JpDesignFullPageCrawlerLog;
import jpo.website.jpo.model.download.image.DownloadedImage;

public class JpDesignPdfFullPageCrawler extends JpoOfficialWebCrawler {

	public JpDesignPdfFullPageCrawler() {
	}

	public JpDesignPdfFullPageCrawler(String proxyIp, String proxyPort) {
		super(proxyIp, proxyPort);
	}

	private static Logger logger = LoggerFactory.getLogger(JpDesignPdfFullPageCrawler.class);

	final private static String BASE_URL = "https://xxxxxxx.xx.xx"; //the targeted url 
	final private static int TABS_COUNT = 2;
	final private static int RETRY_COUNT = 3;


	/**
	 * check if new tab is opened, if the second tabs is opened successfully: 1).
	 * the tab count should be 2 2). the tabsSet.size() should be the same as
	 * tabsArray.size()
	 * 
	 * @param tabsSet
	 * @param tabsArray
	 * @return if the second tabs is opened correctly
	 */
	private Boolean tabsCountCheck(Set<String> tabsSet, ArrayList<String> tabsArray) {
		return (tabsSet.size() == TABS_COUNT || tabsSet.size() == tabsArray.size());
	}

	private Boolean noSecondTab(Set<String> tabsSet, ArrayList<String> tabsArray) {
		return !tabsCountCheck(tabsSet, tabsArray);
	}

	private String genRequestURL(String urlStr) {
		String[] urlStrSplits = urlStr.split(".pdf#");
		StringBuilder sb = new StringBuilder();
		String requestURL = sb.append(urlStrSplits[0]).append(".pdf").toString();
		logger.info("got requestURL : " + requestURL);
		return requestURL;
	}

	private String genFullPageFilePath(String folderPath, String diDate, String docNo) {
		String[] diDateSplits = diDate.split("-");
		String year = diDateSplits[0];
		String month = diDateSplits[1];
		String day = diDateSplits[2];
		String destinationFileName =String.format("%s/%s/%s/%s/%s/fullPage.pdf", folderPath, year, month, day, docNo);


		File f = new File(destinationFileName);
		File fParent = f.getParentFile();
		if (!fParent.exists()) {
			fParent.mkdirs();

		}
		logger.info("downloaded file will be save as : " + destinationFileName);
		return destinationFileName; // "D:\\xxxx\\xxxxx\\1234567.pdf"
	}

	private void downloadPdf(String requestURL, String folderPath, String diDate, String docNo) throws IOException {
		try {
			String filePath = genFullPageFilePath(folderPath, diDate, docNo);
			logger.info("downloaded will be save to "+filePath);
			URL pdfUrl = new URL(requestURL);
			logger.info("URL to download set succesfully");
			try (InputStream in = pdfUrl.openStream()) {
				Files.copy(in, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
				logger.info("file downloaded and saved : " + docNo);

			} catch (IOException ioException) {
				logger.error("not ok : fialed to get the input stream or copy the file from the requestURL");
				throw ioException;
			}

		} catch (IOException e) {
			logger.error("not ok : fialed to initial the pdfUrl by requestURL");
			throw e;

		}
	}

	private WebElement checkEnableAndGetEle(By by, WebDriver driver, String toPrint) {
		WebElement ele = driver.findElement(by);
		logger.info(toPrint + ":" + ele.isEnabled());
		return ele;
	}

	private String crawlerToGetUrlStr(WebDriver driver, int retryLimit, String docNo) throws Exception {

		ArrayList<String> tabsArray = new ArrayList<String>();
		WebElement downloadInfoEle = null;
		WebElement embedEle = null;

		Boolean tabNotOpened = true;
		String urlStr = null;
		int tabOpenRetryCnt = 0;
		int loadPdfRetry = 0;

		while (tabNotOpened && tabOpenRetryCnt <= retryLimit) {

			driver.get(BASE_URL);
			driver.manage().window().setSize(new Dimension(1522, 808));
			Thread.sleep(500);
			By by = null;
			String keyword = null;

			keyword = "d00_srchCondtn_numInput_txtNum0";
			by = By.id(keyword);
			checkEnableAndGetEle(by, driver, keyword).click();
			Thread.sleep(300);

			keyword = "d00_srchCondtn_numInput_txtNum0";
			by = By.id(keyword);
			checkEnableAndGetEle(by, driver, keyword).sendKeys(docNo);
			Thread.sleep(300);

			keyword = "#d00_srchBtn_btnInqry > span";
			by = By.cssSelector(keyword);
			checkEnableAndGetEle(by, driver, keyword).click();
			Thread.sleep(1000);

			{
				keyword = "#d00_srchBtn_btnInqry > span";
				by = By.cssSelector(keyword);
				WebElement element = checkEnableAndGetEle(by, driver, keyword);
				Actions builder = new Actions(driver);
				builder.moveToElement(element).perform();
			}
			{
				keyword = "body";
				by = By.tagName(keyword);
				WebElement element = checkEnableAndGetEle(by, driver, keyword);
				Actions builder = new Actions(driver);
				builder.moveToElement(element, 0, 0).perform();
			}
			keyword = "d0003_srchRsltLst_numonly_regNum0";
			by = By.id(keyword);
			checkEnableAndGetEle(by, driver, keyword).click();

			Thread.sleep(5000); // waiting the pdf to load into the page

			Set<String> tabsSet = driver.getWindowHandles();
			tabsArray = new ArrayList<String>(tabsSet);


			tabNotOpened = noSecondTab(tabsSet, tabsArray); // check if the second tab opened
			tabOpenRetryCnt++;

		} // end while (tabNotOpened && tabOpenRetryCnt < TAB_OPEN_RETRY) loop

		if (tabNotOpened) {
			logger.error("tabNotOpened, retry times: " + tabOpenRetryCnt);
			throw new Exception("tabNotOpened");
		}

		logger.info("new tab with embeded pdf file opened");

		while (Objects.isNull(downloadInfoEle) && loadPdfRetry <= retryLimit) {

			for (String tab : tabsArray) {
				try {
//					System.out.println(tab + "trying to get pdf src");
					driver.switchTo().window(tab);
					driver.manage().window().setSize(new Dimension(1600, 1200));

//					System.out.println("Switched to tab:"+ tab);
					Thread.sleep(2000);
					String keyword = "pdfArea";
					By by = By.id(keyword);
					downloadInfoEle = checkEnableAndGetEle(by, driver, keyword);
					logger.info("=======got text=======" + downloadInfoEle.getText());

//					System.out.println(
//							">>>>>>>>>>>>>>>>>>>>>>>downloadInfoEle.isEnable() : " + downloadInfoEle.isEnabled());
//					System.out.println("get text=============================" + downloadInfoEle.getText());

					keyword = "*";
					by = By.tagName(keyword);
					embedEle = downloadInfoEle.findElement(by);
//					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>embedEle：　" + embedEle);
					urlStr = embedEle.getAttribute("src");
					logger.info("=======urlStr obtained======= : "+urlStr);
//
//					System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>urlStr"+urlStr);
//
//					System.out.println(tab + "pdf src got");
					break;
				} catch (Exception e) {
//					e.printStackTrace();
//					System.out.println(tab + " pdf src is not in this tab");

				}
			}
			Thread.sleep(10000);

			loadPdfRetry++;
		} // end while(Objects.isNull(downloadInfoEle) && loadPdfRetry <=LOAD_PDF_RETRY)

		if (Objects.isNull(downloadInfoEle)) {
			logger.error("downloadInfoEle is null, couldn't find downloadInfoEle, retry times: " + loadPdfRetry);
			throw new Exception("downloadInfoEle is null, couldn't find the pdf");
		}

		// check if totalPage ==1, processFlow modify needed when totalPage >1

		String totalPage = driver.findElement(By.id("cfc003_main_lblTotalPageCount")).getText();
		if (Integer.valueOf(totalPage) != 1) {
			logger.error("expected total page is 1, but more or less than 1 is detected");
			throw new Exception("expected total page is 1, but more or less than 1 is detected");
		}

		logger.info("pdf src --urlStr obtained, urlStr" + urlStr);

		return urlStr;

	}

	public String genDocNoFromPatIdInJP(String patId) throws Exception { // copied from DownloadTargetManager
		final Pattern pat = Pattern.compile("^JP\\d{4}(\\d{7}).*");
		Matcher mat = pat.matcher(patId);
		if (mat.find()) {
			return mat.group(1);
		}

		final Pattern pat2 = Pattern.compile("^JP\\d{4}(\\d{7}_\\d{3}).*");
		Matcher mat2 = pat2.matcher(patId);
		if (mat2.find()) {
			return mat2.group(1);
		} else {
			throw new Exception("Can not recognize the pat-id:" + patId);
		}

	}

	public JpDesignFullPageCrawlerLog initLogModel(DownloadedImage di, JpDesignFullPageCrawlerLog logModel)
			throws Exception {
		logModel.setPatId(di.getPatId());
		logModel.setDocNo(genDocNoFromPatIdInJP(di.getPatId()));
		logModel.setGazette_public_date(di.getDate());
		logModel.setDownloaded(false);
		logModel.setRequestURL("NA");
		Date date = new Date();
		logModel.setDownloadDate(date);

		return logModel;
	}

	public JpDesignFullPageCrawlerLog crawlerProcess(String destFolderPath, DownloadedImage di,
			JpDesignFullPageCrawlerLog logModel) {

		// WebDriver driver = driverSetUp();
		try {
			setUpPhantomJSDriver();
			logger.info("setUpPhantomJSDrive success");
//			setUpChormeDriver();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			String docNo = genDocNoFromPatIdInJP(di.getPatId());
			String diDate = di.getDate();
			logModel = initLogModel(di, logModel);
			logger.info("start crawling for docNo : " + docNo);
			String urlStr = crawlerToGetUrlStr(driver, RETRY_COUNT, docNo);
			String requestURL = genRequestURL(urlStr);
			logModel.setRequestURL(requestURL);
			downloadPdf(requestURL, destFolderPath, diDate, docNo);
			logModel.setDownloaded(true);
			logger.info("end crawling for docNo : " + docNo + "success");

		} catch (InterruptedException interruptedException) {
			logger.error("InterruptedException : " + interruptedException);
//			throw interruptedException;

		} catch (IOException ioe) {
			logger.error("IOException : " + ioe);
//			throw ioe;
		} catch (Exception e) {
			logger.error("Exception : " + e);
//			throw e;
		} finally {
			driver.quit();
			return logModel;
		}
	}
	
	
	public JpDesignFullPageCrawlerLog crawlerProcess(String destFolderPath,
			JpDesignFullPageCrawlerLog logModel) {

		// WebDriver driver = driverSetUp();
		try {
			setUpPhantomJSDriver();
			logger.info("setUpPhantomJSDrive success");
//			setUpChormeDriver();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			String docNo = genDocNoFromPatIdInJP(logModel.getPatId());
			String date = logModel.getGazette_public_date();
//			logModel = initLogModel(log, logModel);
			logger.info("start crawling for docNo : " + docNo);
			String urlStr = crawlerToGetUrlStr(driver, RETRY_COUNT, docNo);
			String requestURL = genRequestURL(urlStr);
			logModel.setRequestURL(requestURL);
			downloadPdf(requestURL, destFolderPath, date, docNo);
			logModel.setDownloaded(true);
			logger.info("end crawling for docNo : " + docNo + "success");

		} catch (InterruptedException interruptedException) {
			logger.error("InterruptedException : " + interruptedException);
//			throw interruptedException;

		} catch (IOException ioe) {
			logger.error("IOException : " + ioe);
//			throw ioe;
		} catch (Exception e) {
			logger.error("Exception : " + e);
//			throw e;
		} finally {
			driver.quit();
			return logModel;
		}
	}

	public static void main(String[] args) {

	}

}
