package dragynslayr.bitbucket.chron;

import java.util.Comparator;

public class DateEventComparator implements Comparator<DateEvent> {
    @Override
    public int compare(DateEvent o1, DateEvent o2) {
        return o1.compare(o2);
    }
}
