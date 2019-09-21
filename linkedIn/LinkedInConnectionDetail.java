package linkedIn;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongo.service.JongoUtil;

import patentdata.utils.INQRestTimeProcess;

public class LinkedInConnectionDetail {

	private static Logger logger = LoggerFactory.getLogger(LinkedInConnectionDetail.class);
	final static String LOGIN_EMAIL = "yp@inquartik.com";
	final static String LOGIN_PWD = "A1b2c3d4";
//	final static String MONGO_QUERY = "{email:null}";
//	final static String MONGO_QUERY = "{}";

	final static String EMAIL_TAG = "ci-email";
	final static int INDEX_NOT_FOUND = -1;

	private WebDriver linkedInLogin(LinkedInCrawler crawler, WebDriver driver, JavascriptExecutor js, String loginEmail,
			String loginPwd) throws Exception {
		logger.info("logining in with :" + loginEmail);

		driver.get("https://www.linkedin.com/login?trk=guest_homepage-basic_nav-header-signin");
		Thread.sleep(500);
		driver.manage().window().setSize(new Dimension(1522, 810));
		Thread.sleep(500);

		driver.findElement(By.id("username")).click();
		Thread.sleep(500);

		driver.findElement(By.id("username")).clear();
		Thread.sleep(570);

		driver.findElement(By.id("username")).sendKeys(loginEmail);
		Thread.sleep(500);

		driver.findElement(By.cssSelector(".form__input--floating:nth-child(25) > .form__label--floating")).click();
		Thread.sleep(500);

		driver.findElement(By.id("password")).sendKeys(loginPwd);
		Thread.sleep(590);

		driver.findElement(By.cssSelector(".btn__primary--large")).click();
		Thread.sleep(1000);
		Thread.sleep(600);

		return driver;

	}

	private Boolean checkEmailNull(String exceptionStr) {

		return !(exceptionStr.indexOf(EMAIL_TAG) == INDEX_NOT_FOUND);

	}
	
	private void processFlow(String monogoQuery) {
		LinkedInCrawler crawler = new LinkedInCrawler();
		WebDriver driver = null;
		try {
			driver = crawler.setUpChormeDriver();
//			driver = crawler.setUpPhantomJSDriver();

		} catch (Exception e1) {
			
			e1.printStackTrace();
		}

		try {

			LinkedInConnectionDetail detailCrawler = new LinkedInConnectionDetail();
			MongoCollection mongo4LinkedIn = JongoUtil.getMongoVivaLinkedIn();
			MongoCollection mongo4LinkedInLog = JongoUtil.getMongoVivaLinkedInDetailLog();

			JavascriptExecutor js = (JavascriptExecutor) driver;

			// query mongo to get the connection list
			MongoCursor<LinkedInConnectionModel> allConnection = mongo4LinkedIn.find(monogoQuery)  //MONGO_QUERY
					.as(LinkedInConnectionModel.class);
			int totalCnt = allConnection.count();
			logger.info("total count :" + totalCnt);

			// ================linkedInLogin
			driver = detailCrawler.linkedInLogin(crawler, driver, js, LOGIN_EMAIL, LOGIN_PWD);

			String href;
			LinkedInConnectionModel connectionInfo = null;

			INQRestTimeProcess rtp = new INQRestTimeProcess(Long.parseLong((String.valueOf(totalCnt))), "execute ");
			// ================================while start=================================
			while (allConnection.hasNext()) {

				rtp.process();
				try {

					connectionInfo = allConnection.next();
//					if (connectionInfo.getName().indexOf(begin) == 0 || connectionInfo.getName().indexOf(end) == 0) {
					href = connectionInfo.getHref();
					logger.info("processing : " + href);
					driver.get(href);

					int scrollCnt = 1;
					while (scrollCnt < 5) {
						try {
							driver.findElement(By.id("experience-section"));
							logger.info("experience-section obtained ");

							break;
						} catch (Exception e) {
							js.executeScript("window.scrollTo(100," + (300 * scrollCnt) + ")");
							scrollCnt++;
							logger.info("experience-section unable to access ");

						}
					}

					try { // try to get experience, position and company
							// company info
						Thread.sleep(500);

						WebElement experience = driver.findElement(By.id("experience-section"));
						logger.info("experience : " + experience.isEnabled());

						// set experience
						connectionInfo.setExperience(experience.getText());
//					logger.info(experience.getText());

						experience = experience.findElement(By.tagName("ul"));
						WebElement latestExperience = experience.findElement(By.tagName("div"));
						logger.info("latestExperience : " + latestExperience.isEnabled());

						latestExperience = latestExperience
								.findElement(By.cssSelector("[data-control-name='background_details_company']"));
						logger.info("latestExperience : " + latestExperience.isEnabled());

						// set position
						String position = latestExperience.findElement(By.tagName("h3")).getText();
//						System.out.println("position :" + position);
						connectionInfo.setPosition(position);

						// set company
						String companyName = latestExperience.findElement(By.className("pv-entity__secondary-title"))
								.getText();
//						System.out.println("companyName :" + companyName);
						connectionInfo.setCompany(companyName);
					} catch (Exception e) {
						connectionInfo.setCompany(null);
						connectionInfo.setPosition(null);

						LinkedInCrawlerLogModel logModel = new LinkedInCrawlerLogModel();
						logModel.setId(connectionInfo.getId());
						logModel.setExcepetion(e.toString());
						mongo4LinkedInLog.save(logModel);
					} // end try catch -- experience, position and company
					
					// == location===============================
					
					try {
						WebElement location = driver.findElement(By.cssSelector("[class='t-16 t-black t-normal inline-block']"));
						logger.info("location : "+location.isEnabled());
//						System.out.println(location.getText());
						connectionInfo.setLocation(location.getText());
					}catch(Exception e ) {
						e.printStackTrace();
						connectionInfo.setLocation("NA");
						LinkedInCrawlerLogModel logModel = new LinkedInCrawlerLogModel();
						logModel.setId(connectionInfo.getId());
						logModel.setExcepetion(e.toString());
						mongo4LinkedInLog.save(logModel);
					}

					// ==personal info=========================

					try { // try to get email
						driver.get(href + "detail/contact-info");
						Thread.sleep(5000);

						WebElement personalInfo = driver
								.findElement(By.cssSelector("[class='pv-contact-info__contact-type ci-email']"));
						logger.info("pv-contact-info__contact-type ci-email : " + personalInfo.isEnabled());

						personalInfo = personalInfo.findElement(By.className("pv-contact-info__ci-container"));
						logger.info("pv-contact-info__ci-container : " + personalInfo.isEnabled());

						String email = personalInfo.findElement(By.tagName("a")).getAttribute("href");
						email = email.replace("mailto:", "");

						// TODO: emailstr & email
						// email start with https .... will be save as email string

						// set email
						connectionInfo.setEmail(email);
//						logger.info("email :" + email);
					} catch (Exception e) {
						String exceptionStr = e.toString();
						if (detailCrawler.checkEmailNull(exceptionStr)) {
							connectionInfo.setEmail("NA");

						}
					} // end try catch -- email

//					} // end if
				} catch (Exception e) {
					LinkedInCrawlerLogModel logModel = new LinkedInCrawlerLogModel();
					logModel.setId(connectionInfo.getId());
					String exceptionStr = e.toString();
					logModel.setExcepetion(exceptionStr);
					mongo4LinkedInLog.save(logModel);
				} finally {
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
					LocalDate localDate = LocalDate.now();

					connectionInfo.setUpdateDate(dtf.format(localDate));
					mongo4LinkedIn.save(connectionInfo);
				} // end try catch in while
				
				//TODO: take out break before export

//				break;
			} // ==============================while end==============================

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			driver.quit();
		}
	}

	public static void main(String[] args) {
//		args = new String[] { "-q", "{location:null}"};
		//
				Options options = new Options();
				options.addOption("h", "help", false, "Lists short help");
				options.addOption("q", "mongoQuery", true, "mongo query e.g. {location:null}");

				CommandLineParser parser = new PosixParser();
				HelpFormatter formatter = new HelpFormatter();
				CommandLine cmd = null;

				try {
					cmd = parser.parse(options, args);
				} catch (ParseException e) {
					formatter.printHelp("updateList ?", options);
					System.exit(1);
				}
				String monogoQuery = cmd.getOptionValue("q");
				LinkedInConnectionDetail process = new LinkedInConnectionDetail();
				process.processFlow(monogoQuery);

	}

}
