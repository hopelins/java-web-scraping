package jpo.util.crawler.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateInterval {
    
    public String begin;
    public String end;
    
    public DateInterval(Date beginDate, Date endDate) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyMMdd");
        this.begin = sdf.format(beginDate);
        this.end = sdf.format(endDate);
    }
    
    @Override
    public String toString() {
        return begin + ":" + end;
    }
    
    
}
