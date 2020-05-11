package dragynslayr.bitbucket.chron;

import java.util.Calendar;

import androidx.annotation.NonNull;

public class DateEvent {

    private static final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private String name, date, phone;
    private int day, month, monthOffset;

    DateEvent(String name, String phone, String date) {
        this.name = name;

        String[] dateParts = date.split("/");
        this.day = Integer.parseInt(dateParts[1]);
        this.month = Integer.parseInt(dateParts[0]);

        this.date = months[month - 1] + " " + day;
        this.phone = phone;

        int current = Calendar.getInstance().get(Calendar.MONTH) + 1;
        monthOffset = month - current;
        if (month < current) {
            monthOffset += 12;
        }
    }

    String getName() {
        return name;
    }

    String getDate() {
        return date;
    }

    String getPhone() {
        return phone;
    }

    int getDay() {
        return day;
    }

    int getMonth() {
        return month;
    }

    @NonNull
    @Override
    public String toString() {
        return name + " " + month + "/" + day + " " + phone;
    }

    int compare(DateEvent other) {
        if (monthOffset == other.monthOffset) {
            if (day == other.day) {
                return phone.compareTo(other.phone);
            } else {
                return day - other.day;
            }
        } else {
            return monthOffset - other.monthOffset;
        }
    }
}
