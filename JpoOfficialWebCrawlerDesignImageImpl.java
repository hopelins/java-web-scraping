package jpo.util.crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;


import com.patentdata.util.WebElementUtil;

import crawler.util.CrawlerUtil;

public class JpoOfficialWebCrawlerDesignImageImpl
        extends JpoOfficialWebCrawler {
    
    public JpoOfficialWebCrawlerDesignImageImpl() {
    }
    
    public JpoOfficialWebCrawlerDesignImageImpl(String proxyIp,
            String proxyPort) {
        super(proxyIp, proxyPort);
//        System.setProperty("https.protocols", "TLSv1.1");
    }
    
    private String getTempFileToSave(int serialNumber) {
        String tempFolder = "temp";
        File f = new File(tempFolder);
        
        if (!f.exists()) {
            f.mkdirs();
        }
        return tempFolder + File.separator + serialNumber;
    }
    
    public void downLoadImageOld(String docNo, String destinationFileName)
            throws IOException, InterruptedException {
        String baseUrl = "https://www.xxxxxx.xxx.xx.xx/";
        driver.get(baseUrl + "/web/ishou/iskt/ISKT_GM201_Top.action");

        new Select(driver.findElement(
                By.id("searchForm_bTmFCOMDTO_fillingSearchConditionList_0__searchItemValue")))
                        .selectByVisibleText("登録番号");
        driver.findElement(
                By.id("searchForm_bTmFCOMDTO_fillingSearchConditionList_0__searchKeywordValue"))
                .clear();
        driver.findElement(
                By.id("searchForm_bTmFCOMDTO_fillingSearchConditionList_0__searchKeywordValue"))
                .sendKeys(docNo);
        driver.findElement(By.id("button_searchcount")).click();
        driver.findElement(By.linkText("1件")).click();
        //By.linkText("意匠登録" + docNo)
        driver.findElement(By.className("doc_no_link")).click();
        driver.findElement(By.linkText("PDF表示")).click();
        WebElement pageInfoEle = driver.findElement(By.xpath(
                "//*[@id=\"searchForm\"]/div/div/div[2]/div[1]/ul[2]/li"));
        int pageNumbers = JpoClientUtilRefactor
                .getPageNum(pageInfoEle.getText());
        
        ArrayList<InputStream> iSList = new ArrayList<InputStream>();
        for (int pageNumber = 0; pageNumber < pageNumbers; pageNumber++) {
            if (pageNumber != 0) {
                driver.findElement(By.linkText("次頁")).click();
            }
            
            WebElement downloadInfoEle = driver.findElement(By.xpath(
                    "//*[@id=\"searchForm\"]/div/div/div[2]/div[3]/embed"));
            String urlStr = downloadInfoEle.getAttribute("src");
            
            Set<Cookie> cookiesObj = driver.manage().getCookies();
            
            Map<String, String> cookiesMap = new HashMap<String, String>();
            for (Cookie cookie : cookiesObj) {
                cookiesMap.put(cookie.getName(), cookie.getValue());
            }
            URL url = new URL(urlStr);
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(this.proxyIp, this.proxyPortInt));
            
            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr)
                    .openConnection(proxy);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout((int) timeout);
            connection.setReadTimeout((int) timeout);
            connection.setRequestProperty("Content-type", "text/xml");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            connection.setRequestProperty("Accept",
                    "Accept:text/html,application/xhtml+xml,application/xml,application/pdf;q=0.9,image/webp,image/apng,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language",
                    "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            connection.setRequestProperty("Accept-Encoding",
                    "gzip, deflate, br");
            connection.setRequestProperty("Cookie",
            		WebElementUtil.getCookiesForHttpURLConnection(driver));
            connection.setRequestProperty("Host", "www.xxxxxx.xxx.xx.xx");
            connection.setRequestProperty("Origin",
                    "https://www.xxxxxx.xxx.xx.xx");
            connection.setRequestProperty("Referer", driver.getCurrentUrl());
            connection.setRequestMethod("GET");
            connection.connect();
            
            InputStream is = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buffer[] = new byte[512];
            
            int length = 0;
            while( (length = is.read(buffer)) != -1){
                baos.write(buffer, 0, length);
            }  
            iSList.add(new ByteArrayInputStream(baos.toByteArray()));
            Thread.sleep(CrawlerUtil.getRandomSleepTime(500));
        }
        
        PDFMergerUtility merger = new PDFMergerUtility();
        for (int i = 0; i < iSList.size(); i++) {
            merger.addSource(iSList.get(i));
        }
        
        
        File f = new File(destinationFileName);
        File fParent = f.getParentFile();
        if (!fParent.exists()) {
            fParent.mkdirs();
        }
        logger.info("Save merged file to "+destinationFileName);
        merger.setDestinationFileName(destinationFileName);
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }
    
    public void downLoadImage(String docNo, String destinationFileName)
            throws IOException, InterruptedException {
        String baseUrl = "https://www.xxxxxx.xxx.xx.xx/";
        driver.get(baseUrl + "/web/ishou/isbs/ISBS_GM101_Top.action");

        driver.findElement(By.id("bunkenBango1")).clear();
        driver.findElement(By.id("bunkenBango1")).sendKeys(docNo);
        driver.findElement(By.id("button_searchresult")).click();
        driver.findElement(By.className("detailedLink")).click();
        driver.findElement(By.linkText("PDF表示")).click();
        
        WebElement pageInfoEle = driver.findElement(By.xpath(
                "//*[@id=\"resultForm\"]/div[3]/div/div[2]/div[1]/ul[2]/li"));
        String pageNStr = pageInfoEle.getText();
        logger.info("The page info of the pdf:" + pageNStr);
        int pageNumbers = JpoClientUtilRefactor.getPageNum(pageNStr);
        logger.info("The page number of the pdf:" + pageNumbers);
        
        ArrayList<InputStream> iSList = new ArrayList<InputStream>();
        for (int pageNumber = 0; pageNumber < pageNumbers; pageNumber++) {
            logger.info("Take page " + pageNumber);
            if (pageNumber != 0) {
                driver.findElement(By.linkText("次頁")).click();
            }
            
            WebElement downloadInfoEle = driver.findElement(By.xpath(
                    "//*[@id=\"resultForm\"]/div/div/div[2]/div[3]/embed"));
            String urlStr = downloadInfoEle.getAttribute("src");
            
            Set<Cookie> cookiesObj = driver.manage().getCookies();
            
            Map<String, String> cookiesMap = new HashMap<String, String>();
            for (Cookie cookie : cookiesObj) {
                cookiesMap.put(cookie.getName(), cookie.getValue());
            }
            URL url = new URL(urlStr);
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(this.proxyIp, this.proxyPortInt));
            
            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr)
                    .openConnection(proxy);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout((int) timeout);
            connection.setReadTimeout((int) timeout);
            connection.setRequestProperty("Content-type", "text/xml");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            connection.setRequestProperty("Accept",
                    "Accept:text/html,application/xhtml+xml,application/xml,application/pdf;q=0.9,image/webp,image/apng,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language",
                    "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            connection.setRequestProperty("Accept-Encoding",
                    "gzip, deflate, br");
            connection.setRequestProperty("Cookie",
            		WebElementUtil.getCookiesForHttpURLConnection(driver));
            connection.setRequestProperty("Host", "www.xxxxxx.xxx.xx.xx");
            connection.setRequestProperty("Origin",
                    "https://www.xxxxxx.xxx.xx.xx");
            connection.setRequestProperty("Referer", driver.getCurrentUrl());
            connection.setRequestMethod("GET");
            connection.connect();
            
            InputStream is = connection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buffer[] = new byte[512];
            
            int length = 0;
            while( (length = is.read(buffer)) != -1){
                baos.write(buffer, 0, length);
            }  
            iSList.add(new ByteArrayInputStream(baos.toByteArray()));
            Thread.sleep(CrawlerUtil.getRandomSleepTime(500));
        }
        
        PDFMergerUtility merger = new PDFMergerUtility();
        for (int i = 0; i < iSList.size(); i++) {
            merger.addSource(iSList.get(i));
        }
        
        
        File f = new File(destinationFileName);
        File fParent = f.getParentFile();
        if (!fParent.exists()) {
            fParent.mkdirs();
        }
        logger.info("Save merged file to "+destinationFileName);
        merger.setDestinationFileName(destinationFileName);
        merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
    }
    
    public static void main(String[] args) throws Exception {
        String[] testStr = { "1234567","D:\\test.pdf" };
      
       args = testStr;
        
        String proxyIp = "xx.xx.xx.xxx";
        String proxyPort = "xxx";
        JpoOfficialWebCrawlerDesignImageImpl crawler = new JpoOfficialWebCrawlerDesignImageImpl(
                proxyIp, proxyPort);
        try {
            crawler.setUpChormeDriver();
//            crawler.setUpPhantomJSDriver();
            crawler.downLoadImage(args[0], args[1]);
            crawler.tearDown();
        } catch (Exception e) {
            crawler.tearDown();
            e.printStackTrace();
            throw new Exception("Fail to work correctly.");
        } finally {
            crawler.tearDown();
        }
    }
    
}
