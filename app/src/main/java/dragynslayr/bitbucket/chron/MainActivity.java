package dragynslayr.bitbucket.chron;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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

public class MainActivity extends Activity {

    public static final String APP_NAME = "Chron";
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

        dateInput = findViewById(R.id.dateInput);
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        calendar = Calendar.getInstance();
        dates = FileHandler.loadDates(this);

        ListView list = findViewById(R.id.listView);
        adapter = new DateEventAdapter(this, dates);
        list.setAdapter(adapter);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 1);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, PERM_REQ_ID);
        }
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

        if (requestCode == SELECT_PHONE_NUMBER && resultCode == RESULT_OK) {
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
}
