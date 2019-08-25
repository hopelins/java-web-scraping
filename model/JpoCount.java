package jpo.util.crawler.model;

public class JpoCount {
    public int count;
    public DateInterval dateInterval;
    public String kindCode;
    public JpoCount() {
    }
    public JpoCount(int count, DateInterval dateInterval, String kindCode) {
        this.count = count;
        this.dateInterval = dateInterval;
        this.kindCode = kindCode;
    }
    
    @Override
    public String toString() {
        return String.format("[count:%s, dataInterval:%s, kindCode: %s]",
                this.count, this.dateInterval, this.kindCode);
    }
}
