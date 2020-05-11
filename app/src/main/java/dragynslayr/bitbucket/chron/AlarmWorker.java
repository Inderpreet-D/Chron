package dragynslayr.bitbucket.chron;

import android.content.Context;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AlarmWorker extends Worker {

    public AlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        handleAlarm(getApplicationContext());

        return Result.success();
    }

    private void handleAlarm(Context context) {
        Log.d(MainActivity.APP_NAME, "Alarm Triggered");
        Toast.makeText(context, "Chron Alarm Triggered", Toast.LENGTH_LONG).show();

        Calendar calendar = Calendar.getInstance();
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

        makeNotification(context, "WORKER: Birthdays " + (month + 1) + "/" + day, msg.toString(), calendar.get(Calendar.YEAR) + (month * 10000) + (day * 1000000));
        sendMessages(birthdays, 0);
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
