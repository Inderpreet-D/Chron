package dragynslayr.bitbucket.chron;

import android.support.annotation.NonNull;

public class DateEvent {

    private final String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private String name, date, phone;
    private int day, month;

    DateEvent(String name, String phone, String date) {
        this.name = name;

        String[] dateParts = date.split("/");
        this.day = Integer.parseInt(dateParts[1]);
        this.month = Integer.parseInt(dateParts[0]);

        this.date = months[month - 1] + " " + day;
        this.phone = phone;
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
        if (month == other.month) {
            if (day == other.day) {
                return phone.compareTo(other.phone);
            } else {
                return day - other.day;
            }
        } else {
            return month - other.month;
        }
    }
}
