package jpo.util.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.patent.utils.DateUtil;

import crawler.util.CrawlerUtil;
import jpo.util.crawler.model.DateInterval;
import jpo.util.crawler.model.JpoCount;

public class JpoOfficialWebCrawler {
    
    
    private static final String _BASEURL_COUNT_PATENTS = "https://www.xxxxxxx.xxxxx.xxxxx.xx/";
    
    protected WebDriver driver;
    protected String proxyIp = null;
    protected String proxyPort = null;
    protected Integer proxyPortInt = null;
    protected long timeout = 10000;
    
    protected static Logger logger = LoggerFactory.getLogger(JpoOfficialWebCrawler.class);
    
    private final String _PATENT_CODE_DESING = "DG";
    
    public JpoOfficialWebCrawler() {
    }
    
    public JpoOfficialWebCrawler(String proxyIp, String proxyPort) {
        logger.info("Set JpoOfficialWebCrawler");
        logger.info("proxyIp:"+proxyIp);
        logger.info("proxyPort:"+proxyPort);
        this.proxyIp = proxyIp;
        this.proxyPort = proxyPort;
        if (proxyPort != null)
            this.proxyPortInt = Integer.parseInt(this.proxyPort);
    }
    
    public void setUpHtmlUnitDriver() throws Exception {
	HtmlUnitDriver thisDriver = new HtmlUnitDriver(BrowserVersion.INTERNET_EXPLORER);
        thisDriver.setJavascriptEnabled(true);
        if (proxyIp != null && proxyPort != null) {
            System.setProperty("http.proxyHost", proxyIp);
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("https.proxyHost", proxyIp);
            System.setProperty("https.proxyPort", proxyPort);
            Proxy proxy = new Proxy();
            proxy.setHttpProxy(proxyIp +":" + proxyPort); 
            thisDriver.setProxySettings(proxy);
        }
        thisDriver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
        driver = thisDriver;
    }
    
    public void setUpPhantomJSDriver() throws Exception {
        
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
    }
    
    public void setUpChormeDriver() throws Exception {
        
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
        //options.addArguments("--test-type");
        options.addArguments("start-maximized");
        options.addArguments("--disable-extensions");
        // options.addArguments("--headless");
        // options.addArguments("--no-startup-window");
        
        DesiredCapabilities dc = DesiredCapabilities.chrome();
        dc.setCapability(ChromeOptions.CAPABILITY, options);
        driver = new ChromeDriver(dc);
        driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.MILLISECONDS);
    }
    
    public void tearDown() throws Exception {
//        driver.close();
        driver.quit();// phantomjs need this!
    }
    
    /**
     * 
     * @param kindCode:
     *            the type defined officially raw-file: A,T,S,B9,U9
     * @throws Exception
     */
    public void setClickBoxByPatType(String kindCode) throws Exception {
        switch (kindCode.trim()) {
            case "S":
            case "A":
            case "T":
                driver.findElement(By.id("bTmFCOMDTO.officialInfoList[0]"))
                        .click();
                break;
            case "B9":
                driver.findElement(By.id("bTmFCOMDTO.officialInfoList[0]"))
                        .click();
                break;
            case "U9":
                driver.findElement(By.id("bTmFCOMDTO.officialInfoList[2]"))
                        .click();
                break;
            default:
                throw new Exception(
                        "No corresponding kindCode definition for " + kindCode);
        }
    }
    
    public void setDateFormByPatType(String kindCode) throws Exception {
        kindCode = kindCode.trim();
        String id = null;
        if (kindCode.equals(_PATENT_CODE_DESING)) {
            id = "searchForm_bTmFCOMDTO_fillingSearchConditionList_0__searchItemValue";
        } else {
            id = "searchForm_bTmFCOMDTO_searchItemList_0_";
        }
        
        Select selectObj = new Select(driver.findElement(By.id(id)));
        switch (kindCode) {
            case "S":// AAAAAA
                selectObj.selectByVisibleText("AAAAAA");
                break;
            case "A":// BBBBB
                selectObj.selectByVisibleText("BBBBB");
                break;
            case "T":// CCCC
                selectObj.selectByVisibleText("CCCC");
                break;
            case "B9":// DDDD
                selectObj.selectByVisibleText("DDDD");
                break;
            case "U9":// EEEEEE
                selectObj.selectByVisibleText("EEEEEE");
                break;
            case _PATENT_CODE_DESING:// FF
                selectObj.selectByVisibleText("FF");
                break;
            default:
                throw new Exception(
                        "No corresponding kindCode definition for " + kindCode);
        }
    }
    
    private List<JpoCount> getDesignPatentCount(DateInterval dI)
            throws Exception {
        String kindCode = "DG";
        List<JpoCount> jpoCountList = new ArrayList<JpoCount>();
        
        String queryKey = dI.toString();
        
        try {
//            setUpChormeDriver();
            setUpPhantomJSDriver();
            driver.get(
                    "https://xxxx.xxxxxx.xxxx.xx/web/all/top/BTmTopPage");
            driver.get(
                    "https://xxxx.xxxxxx.xxxx.xx/web/ishou/iskt/ISKT_GM201_Top.action");
            
            
            // set specific date type;
            setDateFormByPatType(kindCode);
            
            driver.findElement(
                    By.id("searchForm_bTmFCOMDTO_fillingSearchConditionList_0__searchKeywordValue"))
                    .clear();
            driver.findElement(
                    By.id("searchForm_bTmFCOMDTO_fillingSearchConditionList_0__searchKeywordValue"))
                    .sendKeys(dI.toString());
            driver.findElement(By.id("button_searchcount")).click();
            
            String countText = driver
                    .findElement(By.className("searchbox-result-count"))
                    .getText();
            String countStr = countText.replaceAll("件", "");
            int count = Integer.parseInt(countStr);
            
            JpoCount jpoCount = new JpoCount(count, dI, kindCode);
            jpoCountList.add(jpoCount);
        } catch (Exception e) {
            tearDown();
            e.printStackTrace();
            throw new Exception("Fail to work correctly.");
        } finally {
            tearDown();
        }
        return jpoCountList;
    }
    
    private List<JpoCount> getNonDesignPatentCount(DateInterval dI,
            List<String> kindCodes) throws Exception {
        
        List<JpoCount> jpoCountList = new ArrayList<JpoCount>();
        
        String queryKey = dI.toString();
        
        try {
//        	setUpChormeDriver();
            setUpPhantomJSDriver();
            driver.get(
                    "https://xxxx.xxxxxx.xxxx.xx/web/all/top/BTmTopPage");
            for (String kindCode : kindCodes) {
                Thread.sleep(CrawlerUtil.getRandomSleepTime());
                driver.get(
                        "https://xxxx.xxxxxx.xxxx.xx/tjk/tokujitsu/tjkt/TJKT_GM201_Top.action");
                driver.findElement(By.id("bTmFCOMDTO.officialInfoList[0]"))
                        .click();
                // set patent type;
                setClickBoxByPatType(kindCode);
                // set specific date type;
                setDateFormByPatType(kindCode);
                driver.findElement(By.id("bunkenBango0")).clear();
                driver.findElement(By.id("bunkenBango0"))
                        .sendKeys(dI.toString());
                driver.findElement(By.id("button_searchcount")).click();
                
                String countText = driver
                        .findElement(By.className("searchbox-result-count"))
                        .getText();
                String countStr = countText.replaceAll("件", "");
                int count = Integer.parseInt(countStr);
                
                JpoCount jpoCount = new JpoCount(count, dI, kindCode);
                jpoCountList.add(jpoCount);
                
                // clear patent type check-box;
                setClickBoxByPatType(kindCode);
            }
        } catch (Exception e) {
            tearDown();
            e.printStackTrace();
            throw new Exception("Fail to work correctly.");
        } finally {
            tearDown();
        }
        return jpoCountList;
    }
    
    public List<JpoCount> getPatentCount(DateInterval dI,
            List<String> kindCodes) throws Exception {
        
        List<JpoCount> jpoCountList = new ArrayList<JpoCount>();
        
        String queryKey = dI.toString();
        
        List<String> queryNonDesign = new ArrayList<String>();
        List<String> queryDesign = new ArrayList<String>();
        
        for (String kindCode:kindCodes) {
            if (kindCode.trim().equals(_PATENT_CODE_DESING)) {
                queryDesign.add(_PATENT_CODE_DESING);
            } else {
                queryNonDesign.add(kindCode);
            }
        }
        
        if (queryDesign.size() > 0) {
            jpoCountList.addAll(getDesignPatentCount(dI));
        }
        
        if (queryNonDesign.size() > 0) {
            jpoCountList.addAll(getNonDesignPatentCount(dI, queryNonDesign));
        }
        
        return jpoCountList;
    }
    
    public static void main(String[] args) throws Exception {
        // test
        Date d1 = DateUtil.parseDate("20170921");
        Date d2 = DateUtil.parseDate("20170930");
        DateInterval dI = new DateInterval(d1, d2);
        JpoOfficialWebCrawler crawler = new JpoOfficialWebCrawler();
        // end of test
        
        String[] kindCodeArray = { "A" };
        List<String> kindCodeList = new ArrayList<String>();
        for (String kindCode : kindCodeArray) {
            kindCodeList.add(kindCode);
        }
        
        List<JpoCount> jpoCountList = new ArrayList<JpoCount>();
        // jpoCountList.addAll(crawler.getNonDesignPatentCount(dI,kindCodeList));
        jpoCountList.addAll(crawler.getDesignPatentCount(dI));
        for (JpoCount jpoCount : jpoCountList) {
            System.out.println(jpoCount.toString());
        }
        
    }
    
}
