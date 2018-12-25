package dragynslayr.bitbucket.chron;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

class FileHandler {

    private static final String SAVE_FILE_NAME = "dates.txt";

    static void saveDates(ArrayList<DateEvent> dates, Context context) {
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i < dates.size(); i++) {
            DateEvent d = dates.get(i);
            contents.append(d.toString());
            if (i != dates.size() - 1) {
                contents.append("\n");
            }
        }
        try {
            File file = new File(context.getFilesDir(), SAVE_FILE_NAME);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(contents.toString());
            writer.flush();
            writer.close();
        } catch (Exception e) {
            Log.e("Chron", "File write failed: " + e.toString());
        }
    }

    static ArrayList<DateEvent> loadDates(Context context) {
        ArrayList<DateEvent> read = new ArrayList<>();
        try {
            File file = new File(context.getApplicationContext().getFilesDir(), SAVE_FILE_NAME);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0) {
                        String[] split = line.trim().split(" ");
                        DateEvent d = new DateEvent(split[0], split[2], split[1]);
                        read.add(d);
                    }
                }
                reader.close();
            }
        } catch (Exception e) {
            Log.e("Chron", "File read failed: " + e.toString());
        }
        return read;
    }
}
