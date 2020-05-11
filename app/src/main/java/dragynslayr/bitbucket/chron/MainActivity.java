package dragynslayr.bitbucket.chron;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Constraints;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

public class MainActivity extends Activity {

    public static final String APP_NAME = "Chron";
    public static final String CHANNEL_ID = "ChronNotify";

    private final int SELECT_PHONE_NUMBER = 1, PERM_REQ_ID = 101;
    private Calendar calendar;
    private EditText nameInput, dateInput, phoneInput;
    private DateEventAdapter adapter;
    private ArrayList<DateEvent> dates;
    private DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat format = new SimpleDateFormat("MM/dd", Locale.CANADA);
            dateInput.setText(format.format(calendar.getTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createNotificationChannel();

        dateInput = findViewById(R.id.dateInput);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        calendar = Calendar.getInstance();
        dates = FileHandler.loadDates(this);

        ListView list = findViewById(R.id.listView);
        adapter = new DateEventAdapter(this, dates);
        list.setAdapter(adapter);

        scheduleAlarm();

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, PERM_REQ_ID);
        }
    }

    private void scheduleAlarm() {
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(APP_NAME);

        Constraints constraints = new Constraints.Builder().build();

        long currentTimeMillis = System.currentTimeMillis();
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(currentTimeMillis);
        Calendar later = Calendar.getInstance();
        later.setTimeInMillis(currentTimeMillis);
        later.set(Calendar.HOUR_OF_DAY, 0);
        later.set(Calendar.MINUTE, 1);
        later.set(Calendar.SECOND, 0);
        later.set(Calendar.MILLISECOND, 0);
        later.add(Calendar.HOUR_OF_DAY, 24);

        long initialDelay = Math.abs(later.getTimeInMillis() - now.getTimeInMillis());
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(AlarmWorker.class, 1, TimeUnit.DAYS, 2, TimeUnit.MINUTES).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS).setConstraints(constraints).addTag(APP_NAME).build();

        WorkManager.getInstance(getApplicationContext()).enqueue(request);

        Log.d(APP_NAME, "Work scheduled for " + (initialDelay / (1000.0 * 3600.0)) + " hours from now");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != PERM_REQ_ID) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("SMS Permission not granted")
                    .setMessage("This app will not text people without being able to send SMS messages")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.ok, null).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == adapter.SELECT_PHONE_NUMBER_2) {
            adapter.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER};

            if (contactUri != null) {
                Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                        String name = cursor.getString(nameIndex).split(" ")[0];
                        String number = cursor.getString(numberIndex).substring(2);

                        nameInput.setText(name);
                        phoneInput.setText(number);
                    }

                    cursor.close();
                }
            }
        }
    }

    public void onClickDate(View v) {
        new DatePickerDialog(this, date, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void onClickAdd(View v) {
        nameInput.clearFocus();
        phoneInput.clearFocus();
        dateInput.clearFocus();

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            View focus = getCurrentFocus();
            if (focus != null) {
                inputManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }

        String name = nameInput.getText().toString();
        String phone = phoneInput.getText().toString();
        String date = dateInput.getText().toString();

        if (name.length() > 0) {
            if (phone.length() == 10) {
                if (date.length() > 0) {
                    nameInput.setText("");
                    phoneInput.setText("");
                    dateInput.setText("");

                    addEvent(name, phone, date);
                    FileHandler.saveDates(dates, this);
                    makeToast("Added " + name);
                } else {
                    makeToast("Choose a date");
                }
            } else {
                makeToast("Phone number length must be 10 digits, entered " + phone.length());
            }
        } else {
            makeToast("Enter a name");
        }
    }

    public void onClickChoose(View v) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, SELECT_PHONE_NUMBER);
    }

    private void addEvent(String name, String phone, String date) {
        DateEvent event = new DateEvent(name, phone, date);
        adapter.add(event);
    }

    private void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
