package jpo.util.crawler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongo.service.JongoUtil;
import com.patent.utils.DateUtil;

import common.Constants;
import jpo.util.crawler.model.JpDesignFullPageCrawlerLog;
import jpo.website.jpo.model.download.image.DownloadedImage;
import patentdata.utils.INQRestTimeProcess;
import rdb.finder.PatDataFinder;

public class JpDesignPdfFullPageDownloadProcess {

//	final private static String BEGINE_DATE = "2018-02-18";
//	final private static String END_DATE = "2018-02-20";
//	final private static String DEST_FOLDER_PATH = "D:\\xxxxxx\\xxxxxx";
	private String proxyIp = null;
	private String proxyPort = null;

	public JpDesignPdfFullPageDownloadProcess() {
	}

	public JpDesignPdfFullPageDownloadProcess(String proxyIp, String proxyPort) {
		if (proxyIp != null) {
			this.proxyIp = proxyIp.trim();
		}
		if (proxyPort != null) {
			this.proxyPort = proxyPort.trim();
		}
	}

	final static String KIND_CODE = "S";

	private static Logger logger = LoggerFactory.getLogger(JpDesignPdfFullPageDownloadProcess.class);

	// mongo connection for log update
	private MongoCollection connectMongoVivaJpDesignFullPageLog() {
		MongoCollection mongoCol = JongoUtil.getMongoVivaJpDesignFullPageLog();
		return mongoCol;
	}

	private List<DownloadedImage> genDownloadedImageListFromPatData( // copied from DownloadTargetManager
			String beginDateStr, String endDateStr, String kindcode) {
		List<Date> gDateList = null;
		try {
			gDateList = PatDataFinder.getDistinctDateByGazattePubDateAndKindCode(beginDateStr, endDateStr, kindcode);
		} catch (Exception e) {
			e.printStackTrace();
		}

		final String dateKWord = Constants.PATDATA_FIELDNAME_GAZETTE_PUBLIC_DATE;

		int serialNumber = 0;
		List<DownloadedImage> dIList = new ArrayList<DownloadedImage>();
		for (Date gDate : gDateList) {
			String gDateString = DateUtil.getSQLDateString(gDate);
			logger.info("query and generate on this date:" + gDateString);
			List<String> patIdList = null;
			try {
				patIdList = PatDataFinder.queryPatIdByDateAndKindCode(dateKWord, gDateString, gDateString, kindcode);
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (String patId : patIdList) {
				DownloadedImage dI = new DownloadedImage();
				dI.setDate(DateUtil.getSQLDateString(gDate));
				dI.setId(serialNumber++);
				dI.setPatId(patId);
				dI.setIsDownloaded(false);
				dIList.add(dI);
			}
		}
		return dIList;
	}


	/** 
	 * @param beginDate
	 * @param endDate
	 * @param downloadPath the target folder to save the downloaded file
	 * @throws Exception
	 */
	private void processFlow(String beginDate, String endDate, String downloadPath) throws Exception {
		MongoCollection mongo4log = connectMongoVivaJpDesignFullPageLog();
		JpDesignPdfFullPageCrawler crawler = new JpDesignPdfFullPageCrawler(this.proxyIp, this.proxyPort);
		try {

			List<DownloadedImage> diList = genDownloadedImageListFromPatData(beginDate, endDate, KIND_CODE);
			logger.info("start processing, " + beginDate + "exit - " + endDate + " , total : " + diList.size());
			for (DownloadedImage di : diList) {
				JpDesignFullPageCrawlerLog logModel = new JpDesignFullPageCrawlerLog();
				logModel = crawler.initLogModel(di, logModel);
				mongo4log.save(logModel);
			}

			INQRestTimeProcess rtp = new INQRestTimeProcess(Long.parseLong(String.valueOf(diList.size())),
					"execute [" + beginDate + "-" + endDate + "]");

			for (DownloadedImage di : diList) {
				rtp.process();
				JpDesignFullPageCrawlerLog logModel = new JpDesignFullPageCrawlerLog();
				crawler.crawlerProcess(downloadPath, di, logModel);
				mongo4log.save(logModel);
//				break;
			}
			logger.info("finish processing " + beginDate + " - " + endDate + " , total : " + diList.size());

		} catch (Exception e) {
			logger.error("Exception:" + e);

		} finally {
		}

	}

	public static void main(String[] args) {
		// TODO: markdown before push
		Boolean firstDownload = false;
		args = new String[] { "-b", "2019-05-01", "-e", "2019-05-27", "-dl", "D:\\xxxxxx\\xxxxxxxx", "-p",
				"xx.xx.xx.xxx:xxx" };

		// args options

		Options options = new Options();
		options.addOption("h", "help", false, "Lists short help");
		options.addOption("b", "beginDate", true, "set begin time");
		options.addOption("e", "endDate", true, "set end time");
		options.addOption("dl", "downloadLocation", true, "download to specific location");
		options.addOption("p", "proxy:port", true, "proxy:port");

		CommandLineParser parser = new PosixParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		String proxyIp = null;
		String proxyPort = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			formatter.printHelp("input beginDate, endDate, downloadLocation, and port", options);
			System.exit(1);
		}
		String beginDate = cmd.getOptionValue("b");
		String endDate = cmd.getOptionValue("e");
		String downloadLoction = cmd.getOptionValue("dl");
		if (cmd.hasOption("p")) {
			String[] proxy = cmd.getOptionValue("p").split(":");
			proxyIp = proxy[0];// "xx.xx.xx.xxx"
			proxyPort = proxy[1];// "xxx"
			logger.info("Set proxy ip:" + proxyIp);
			logger.info("Set proxy port:" + proxyPort);
		}

		JpDesignPdfFullPageDownloadProcess downloadProcess = new JpDesignPdfFullPageDownloadProcess(proxyIp, proxyPort);
		try {
			downloadProcess.processFlow(beginDate, endDate, downloadLoction);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
