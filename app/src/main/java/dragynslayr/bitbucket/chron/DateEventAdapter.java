package dragynslayr.bitbucket.chron;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
            new AlertDialog.Builder(getContext())
                    .setTitle("Remove")
                    .setMessage("Do you want to delete " + nameText.getText().toString() + "?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        removeDate(position);
                        Toast.makeText(getContext(), "Removed " + nameText.getText().toString(), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            return true;
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
}
