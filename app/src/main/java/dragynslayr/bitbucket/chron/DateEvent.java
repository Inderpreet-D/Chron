package dragynslayr.bitbucket.chron;

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

    @Override
    public String toString() {
        return name + " " + month + "/" + day + " " + phone;
    }
}
