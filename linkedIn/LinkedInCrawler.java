package linkedIn;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongo.service.JongoUtil;

import jpo.util.crawler.JpoOfficialWebCrawler;
import jpo.util.crawler.model.JpoOpenDataIncrementalModel;
import patentdata.utils.INQRestTimeProcess;

public class LinkedInCrawler {

	protected WebDriver driver;
	protected String proxyIp = null;
	protected String proxyPort = null;
	protected Integer proxyPortInt = null;
	protected long timeout = 10000;

	protected static Logger logger = LoggerFactory.getLogger(JpoOfficialWebCrawler.class);

	public LinkedInCrawler() {
	}

	public LinkedInCrawler(String proxyIp, String proxyPort) {
		logger.info("Set JpoOfficialWebCrawler");
		logger.info("proxyIp:" + proxyIp);
		logger.info("proxyPort:" + proxyPort);
		this.proxyIp = proxyIp;
		this.proxyPort = proxyPort;
		if (proxyPort != null)
			this.proxyPortInt = Integer.parseInt(this.proxyPort);
	}

	public WebDriver setUpPhantomJSDriver() throws Exception {

		ArrayList<String> phantomArgs = null;
		// phantomArgs.add("--webdriver-loglevel=NONE");
		if (proxyIp != null && proxyPort != null) {
			phantomArgs = new ArrayList<String>();
			phantomArgs.add("--proxy=address:port" + proxyIp + ":" + proxyPort);
			// phantomArgs.add("--proxy-auth=username:password");
			phantomArgs.add("--proxy-type=http");
		}

		final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36";

		String OS = System.getProperty("os.name").toLowerCase();
		String location = null;
		if (OS.indexOf("win") >= 0) {
			location = "webdriver/win64/phantomjs/phantomjs.exe";
		} else if (OS.indexOf("linux") >= 0) {
			location = "webdriver/linux64/phantomjs/phantomjs";
		} else {
			throw new Exception("No defined phantomJS driver for this os.");
		}

		if (new File(location).exists()) {
			System.out.println("Loading driver on this location:" + location);
		} else {
			throw new Exception("No dirver on this location:" + location);
		}
		DesiredCapabilities caps = DesiredCapabilities.phantomjs();
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, location);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "userAgent", userAgent);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", false);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "javascriptEnabled", true);
		caps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, phantomArgs);
		caps.setBrowserName("chrome");
		driver = new PhantomJSDriver(caps);
		Capabilities cap1 = ((RemoteWebDriver) driver).getCapabilities();
		return driver;
	}

	public WebDriver setUpChormeDriver() throws Exception {

		if (proxyIp != null && proxyPort != null) {
			System.setProperty("http.proxyHost", proxyIp);
			System.setProperty("http.proxyPort", proxyPort);
			System.setProperty("https.proxyHost", proxyIp);
			System.setProperty("https.proxyPort", proxyPort);
		}
		// hardcode driver location and configruation
		String downloadFilepath = "/";
		String OS = System.getProperty("os.name").toLowerCase();
		String location = null;

		if (OS.indexOf("win") >= 0) {
			location = "webdriver/win64/chrome/chromedriver.exe";
			downloadFilepath = "D://";
		} else {
			throw new Exception("No defined chrome driver for this os.");
		}
		System.setProperty("webdriver.chrome.driver", location);

		HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
		chromePrefs.put("profile.default_content_settings.popups", 0);
		chromePrefs.put("download.default_directory", downloadFilepath);
		chromePrefs.put("pdfjs.disabled", true);

		ChromeOptions options = new ChromeOptions();
		options.setExperimentalOption("prefs", chromePrefs);
		// options.addArguments("--test-type");
		options.addArguments("start-maximized");
		options.addArguments("--disable-extensions");
//		options.setHeadless(true);

		// 好像一定要有window
		// options.addArguments("--no-startup-window");

		DesiredCapabilities dc = DesiredCapabilities.chrome();
		dc.setCapability(ChromeOptions.CAPABILITY, options);
		driver = new ChromeDriver(dc);
		driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
		return driver;
	}

	public void tearDown() throws Exception {
//        driver.close();
		driver.quit();// phantomjs need this!
	}

	private HashMap<String, String> genCurrentMap(MongoCollection mongo4LinkedIn) {
		MongoCursor<LinkedInConnectionModel> existingList = mongo4LinkedIn.find().as(LinkedInConnectionModel.class);
		HashMap<String, String> existingMap = new HashMap<>();
		while (existingList.hasNext()) {
			LinkedInConnectionModel existingModel = existingList.next();
			existingMap.put(existingModel.getId(), existingModel.getOccupation());
		}

		return existingMap;

	}

	private List<WebElement> batchcrawling(ArrayList<String> idList, List<WebElement> eleMemberList,
			MongoCollection mongo4LinkedIn, int currentProgress, HashMap<String, String> existingMap)
			throws InterruptedException {
		eleMemberList = driver.findElements(By.className("list-style-none"));
		System.out.println("eleMemberList.size() : " + eleMemberList.size());

//		for (WebElement ele : eleMemberList) {
		for (int i = 0 + currentProgress; i < eleMemberList.size(); i++) {
			WebElement ele = eleMemberList.get(i);
			WebElement targetEle;
			WebElement timeEle;
			targetEle = ele.findElement(By.className("mn-connection-card__details"));
			targetEle = targetEle.findElement(By.tagName("a"));
			String href = targetEle.getAttribute("href");
			System.out.println("href : " + href);
			String linkedInId = href.replace("https://www.linkedin.com/in/", "").replace("/", "");

			if (idList.contains(linkedInId)) {
//				System.out.println("already exist , skip   :" + linkedInId);
				continue;
			}

			LinkedInConnectionModel linkedInModel = new LinkedInConnectionModel();
			linkedInModel.setHref(href);
			linkedInModel.setId(linkedInId);
			idList.add(linkedInId);

			ArrayList<String> personInfo = new ArrayList<String>();
			List<WebElement> eleDetails = targetEle.findElements(By.tagName("span"));
			for (WebElement eleDetail : eleDetails) {
				if (!eleDetail.getAttribute("class").toString().equals("visually-hidden")) {
					personInfo.add(eleDetail.getText());
//					System.out.println("eleDetail.getText():" + eleDetail.getText());
				}
			}
			linkedInModel.setName(personInfo.get(0));
			linkedInModel.setOccupation(personInfo.get(1));

			timeEle = ele.findElement(By.tagName("time"));
			String connectedDate = timeUtil.genConnectedDate(timeEle.getText());
//			System.out.println(connectedDate);
			linkedInModel.setConnected(connectedDate);

			Thread.sleep(300);

			// save in mongo, no need to update if id already exist and no changes in
			// occupation

			if (existingMap.containsKey(linkedInModel.getId())) {
				if (existingMap.get(linkedInModel.getId()).equals(linkedInModel.getOccupation())) {
					continue;
				} else {
					mongo4LinkedIn.save(linkedInModel);
				}
			} else {
				mongo4LinkedIn.save(linkedInModel);
			}
		}
		return eleMemberList;
	}

	private void scrollDownAndSleep(List<WebElement> eleMemberList, JavascriptExecutor js) throws InterruptedException {
		WebElement lastEle = eleMemberList.get(eleMemberList.size() - 1);
		System.out.println(lastEle.getLocation());

		WebElement targetlast1 = lastEle.findElement(By.className("mn-connection-card__details"));
		targetlast1 = targetlast1.findElement(By.tagName("a"));
		String href1 = targetlast1.getAttribute("href");
//		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>last : " + href1);

		Thread.sleep(5000);
		js.executeScript("window.scrollTo" + lastEle.getLocation());
		System.out.println("go to sleep");
		Thread.sleep(10000);
	}

	private Boolean scrolledAll(List<WebElement> eleMemberList, String connectionCnt) {
		return eleMemberList.size() >= Integer.valueOf(connectionCnt);

	}

	private void processFlow(int totalCnt) {

		MongoCollection mongo4LinkedIn = JongoUtil.getMongoVivaLinkedIn();
		HashMap<String, String> existingMap = genCurrentMap(mongo4LinkedIn);
		try {

			JavascriptExecutor js;
			WebDriver driver = setUpChormeDriver();
			js = (JavascriptExecutor) driver;

			driver.get("https://www.linkedin.com/login?trk=guest_homepage-basic_nav-header-signin");
			Thread.sleep(500);
			driver.manage().window().setSize(new Dimension(1522, 810));
			Thread.sleep(500);

			driver.findElement(By.id("username")).click();
			Thread.sleep(500);

			driver.findElement(By.id("username")).clear();
			Thread.sleep(500);

			driver.findElement(By.id("username")).sendKeys("yp@inquartik.com");
			Thread.sleep(500);

			driver.findElement(By.cssSelector(".form__input--floating:nth-child(25) > .form__label--floating")).click();
			Thread.sleep(500);

			driver.findElement(By.id("password")).sendKeys("A1b2c3d4");
			Thread.sleep(500);

			driver.findElement(By.cssSelector(".btn__primary--large")).click();
			Thread.sleep(1000);

			driver.findElement(By.id("mynetwork-tab-icon")).click();
			Thread.sleep(500);

			driver.findElement(By.partialLinkText("Connections")).click();
			Thread.sleep(1000);

			js.executeScript("window.scrollTo(0,0)");

			String connectionCnt;
			connectionCnt = driver.findElement(By.className("mn-connections__header")).getText();
			connectionCnt = connectionCnt.replace(" Connections", "").replace(",", "");
			System.out.println(Integer.valueOf(connectionCnt));

			// ===========================get to connections page====================

			ArrayList<String> idList = new ArrayList<String>();
			List<WebElement> eleMemberList = new ArrayList<WebElement>();
			// ===start 1
			// batch===============================================================================

			INQRestTimeProcess rtp = new INQRestTimeProcess(Long.parseLong((String.valueOf(connectionCnt))) / 40,
					"execute ");
			int currentProgress = 0;
			while (!(scrolledAll(eleMemberList, connectionCnt)) && currentProgress <= totalCnt) {
				rtp.process();

				eleMemberList = batchcrawling(idList, eleMemberList, mongo4LinkedIn, currentProgress, existingMap);
				currentProgress = eleMemberList.size();
				scrollDownAndSleep(eleMemberList, js);
			}
			// ==================================================================================
		} catch (Exception e) {
			logger.error("Exception : " + e);
			e.printStackTrace();

		} finally {
			driver.quit();
		}

	}

	public static void main(String[] args) throws Exception {
		args = new String[] { "-c", "100" };

		Options options = new Options();
		options.addOption("h", "help", false, "Lists short help");
		options.addOption("c", "totalCnt", true, "enter how many connection added");

		CommandLineParser parser = new PosixParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			formatter.printHelp("updateList ?", options);
			System.exit(1);
		}
		int totalCnt = Integer.MAX_VALUE;

		if (Objects.nonNull(cmd.getOptionValue("c"))) {
			totalCnt = Integer.valueOf(cmd.getOptionValue("c"));
		}
		System.out.println("totalCnt : " + totalCnt);

		LinkedInCrawler crawler = new LinkedInCrawler();
		crawler.processFlow(totalCnt);


	}

}
