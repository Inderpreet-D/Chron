package dragynslayr.bitbucket.chron;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DateEventAdapter extends ArrayAdapter<DateEvent> {

    private ArrayList<DateEvent> dates;
    final int SELECT_PHONE_NUMBER_2 = 2;
    private EditText nameText2, phoneText2;

    DateEventAdapter(Context context, ArrayList<DateEvent> dates) {
        super(context, 0, dates);
        this.dates = dates;
        sortAndUpdate();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        DateEvent event = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.date_event, parent, false);
        }

        final TextView nameText = convertView.findViewById(R.id.eventName);
        TextView dateText = convertView.findViewById(R.id.eventDate);
        TextView phoneText = convertView.findViewById(R.id.eventPhone);

        if (event != null) {
            nameText.setText(event.getName());
            dateText.setText(event.getDate());
            phoneText.setText(event.getPhone());
        }

        convertView.setOnLongClickListener(v -> {
            final Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.remove_date);

            TextView description = dialog.findViewById(R.id.description);
            String text = "Do you want to delete " + nameText.getText().toString() + "?";
            description.setText(text);

            Button cancelButton = dialog.findViewById(R.id.cancelButton);
            cancelButton.setOnClickListener(view -> dialog.dismiss());

            Button confirmButton = dialog.findViewById(R.id.okButton);
            confirmButton.setOnClickListener(view -> {
                removeDate(position);
                makeToast("Removed " + nameText.getText().toString());
                dialog.dismiss();
            });

            dialog.show();
            return true;
        });

        convertView.setOnClickListener(v -> {
            final Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.edit_date);

            nameText2 = dialog.findViewById(R.id.nameInput);
            phoneText2 = dialog.findViewById(R.id.phoneInput);
            EditText dateText2 = dialog.findViewById(R.id.dateInput);

            if (event != null) {
                nameText2.setText(event.getName());
                phoneText2.setText(event.getPhone());
                String date = event.getMonth() + "/" + event.getDay();
                dateText2.setText(date);
            }

            dateText2.setOnClickListener(view2 -> {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat format = new SimpleDateFormat("MM/dd", Locale.CANADA);
                    dateText2.setText(format.format(calendar.getTime()));
                };
                new DatePickerDialog(getContext(), date, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            });

            Button editButton = dialog.findViewById(R.id.changeButton);
            editButton.setOnClickListener(view -> {
                Activity activity = (Activity) getContext();
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    View focus = activity.getCurrentFocus();
                    if (focus != null) {
                        inputManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                }

                String name = nameText2.getText().toString();
                String phone = phoneText2.getText().toString();
                String date = dateText2.getText().toString();

                if (name.length() > 0) {
                    if (phone.length() == 10) {
                        if (date.length() > 0) {
                            DateEvent newEvent = new DateEvent(name, phone, date);
                            removeDate(position);
                            add(newEvent);
                            FileHandler.saveDates(dates, getContext());
                            makeToast("Updated " + name);
                            dialog.dismiss();
                        } else {
                            makeToast("Choose a date");
                        }
                    } else {
                        makeToast("Phone number length must be 10 digits, entered " + phone.length());
                    }
                } else {
                    makeToast("Enter a name");
                }
            });

            Button chooseButton = dialog.findViewById(R.id.chooseButton);
            chooseButton.setOnClickListener(view -> {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                ((Activity) getContext()).startActivityForResult(i, SELECT_PHONE_NUMBER_2);
            });

            Button closeButton = dialog.findViewById(R.id.cancelButton);
            closeButton.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });

        return convertView;
    }

    @Override
    public void add(@Nullable DateEvent object) {
        super.add(object);
        sortAndUpdate();
    }

    private void sortAndUpdate() {
        DateEventComparator comparator = new DateEventComparator();
        dates.sort(comparator);
        notifyDataSetChanged();
    }

    private void removeDate(int position) {
        dates.remove(position);
        sortAndUpdate();
        FileHandler.saveDates(dates, getContext().getApplicationContext());
    }

    private void makeToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PHONE_NUMBER_2 && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER};

            if (contactUri != null) {
                Cursor cursor = getContext().getContentResolver().query(contactUri, projection, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                        String name = cursor.getString(nameIndex).split(" ")[0];
                        String number = cursor.getString(numberIndex).substring(2);

                        nameText2.setText(name);
                        phoneText2.setText(number);
                    }

                    cursor.close();
                }
            }
        }
    }
}
