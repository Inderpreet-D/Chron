package dragynslayr.bitbucket.chron;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DateEventAdapter extends ArrayAdapter<DateEvent> {

    private ArrayList<DateEvent> dates;

    DateEventAdapter(Context context, ArrayList<DateEvent> dates) {
        super(context, 0, dates);
        this.dates = dates;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        DateEvent event = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.date_event, parent, false);
        }

        final TextView nameText = convertView.findViewById(R.id.eventName);
        TextView dateText = convertView.findViewById(R.id.eventDate);
        TextView phoneText = convertView.findViewById(R.id.eventPhone);

        nameText.setText(event.getName());
        dateText.setText(event.getDate());
        phoneText.setText(event.getPhone());

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Remove")
                        .setMessage("Do you want to remove " + nameText.getText().toString())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeDate(position);
                                Toast.makeText(getContext(), "Removed " + nameText.getText().toString(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });

        return convertView;
    }

    private void removeDate(int position) {
        dates.remove(position);
        notifyDataSetChanged();
        FileHandler.saveDates(dates, getContext().getApplicationContext());
    }
}
