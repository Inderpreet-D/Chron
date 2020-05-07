package dragynslayr.bitbucket.chron;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) <= 3) {
            Log.d(MainActivity.APP_NAME, "Alarm Triggered");
            Toast.makeText(context, "Chron Alarm Triggered", Toast.LENGTH_LONG).show();

            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            StringBuilder msg = new StringBuilder();

            ArrayList<DateEvent> dates = FileHandler.loadDates(context);
            ArrayList<DateEvent> birthdays = new ArrayList<>();

            for (DateEvent d : dates) {
                if ((month + 1) == d.getMonth() && day == d.getDay()) {
                    birthdays.add(d);
                    msg.append(d.getName()).append("\n");
                }
            }

            if (msg.length() == 0) {
                msg = new StringBuilder("None");
            } else {
                msg = new StringBuilder(msg.toString().trim());
            }

            makeNotification(context, "Birthdays " + (month + 1) + "/" + day, msg.toString(), calendar.get(Calendar.YEAR) + (month * 10000) + (day * 1000000));
            sendMessages(birthdays, 0);
        }
    }

    private void makeNotification(Context context, String title, String msg, int id) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chron_notif)
                .setContentTitle(title)
                .setContentText(msg)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id, builder.build());
    }

    private void sendMessages(final ArrayList<DateEvent> birthdays, final int idx) {
        if (idx < birthdays.size()) {
            (new Handler()).postDelayed(() -> {
                DateEvent d = birthdays.get(idx);
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(d.getPhone(), null, "Happy Birthday, " + d.getName() + "!", null, null);
                sendMessages(birthdays, idx + 1);
            }, 5000);
        }
    }
}
