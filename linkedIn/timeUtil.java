package linkedIn;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class timeUtil {
	final static long HOUR = 60 * 60 * 1000;
	final static long DAY = HOUR * 24;
	final static long WEEK = DAY * 7;
	final static long MONTH = DAY * 30;
	final static long YEAR = DAY * 365;

	public static String genConnectedDate(String connectedtime) {
//		String connectedDate;
		long connectedTimeStamp;

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//		System.out.println(timestamp);
//		System.out.println(timestamp.getTime());

		String[] connectinfo = connectedtime.split(" ");
		long num = Integer.valueOf(connectinfo[1]);
//		System.out.println("num:  " + num);
		String unit = connectinfo[2];
//		System.out.println("unit: " + unit);

		switch (unit) {
		case "hour":
		case "hours":
			connectedTimeStamp = timestamp.getTime() - (num * HOUR);
//			System.out.println("connectedTimeStamp hour :" + connectedTimeStamp);
			break;

		case "day":
		case "days":
			connectedTimeStamp = timestamp.getTime() - (num * DAY);
//			System.out.println("connectedTimeStamp day :" + connectedTimeStamp);

			break;

		case "week":
		case "weeks":
			connectedTimeStamp = timestamp.getTime() - (num * WEEK);
//			System.out.println("connectedTimeStamp week :" + connectedTimeStamp);

			break;
		case "month":
		case "months":
			connectedTimeStamp = timestamp.getTime() - (num * MONTH);
//			System.out.println(timestamp.getTime());
//			System.out.println(num * MONTH);
//			System.out.println("connectedTimeStamp month :" + connectedTimeStamp);

			break;
		case "year":
		case "years":
			connectedTimeStamp = timestamp.getTime() - (num * YEAR);
//			System.out.println(timestamp.getTime());
//			System.out.println(num * YEAR);
//			System.out.println("connectedTimeStamp year :" + connectedTimeStamp);

			break;
		default:
			connectedTimeStamp = timestamp.getTime();
			break;

		}

		Date conDdate = new Date(connectedTimeStamp);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		// formatted value of current Date
//		System.out.println("Milliseconds to Date: " + df.format(conDdate));

		return df.format(conDdate).toString();

	}

	public static void main(String[] args) {
		Timestamp Currenttimestamp = new Timestamp(System.currentTimeMillis());
		System.out.println("Currenttimestamp:" + Currenttimestamp);
		System.out.println("Currenttimestamp.getTime(): " + Currenttimestamp.getTime());

		String connectedtime;
		String result;


		connectedtime = "Connected 3 djiw ago";
		result = timeUtil.genConnectedDate(connectedtime);
		System.out.println("Connected 3 djiw ago : " + result);

	}

}
