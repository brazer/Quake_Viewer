package by.org.cgm.jdbf;

import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

public class JdbfTask extends AsyncTask<String, Void, DBFReader>
{

    public static ArrayList<QuakeRecord> records;
    private static DBFReader reader;

    @Override
    protected DBFReader doInBackground(String... params) {
        try {
            URL url = new URL("http://www.brazer.url.ph/data.dbf");
            reader = new DBFReader(url.openStream());
        }
        catch (Exception e) {
            e.printStackTrace();
            reader = readFromFile();
        }
        try {
            onPostExecute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reader;
    }

    private DBFReader readFromFile() {
        try {
            return new DBFReader("/mnt/sdcard/data/data.dbf");
        } catch (JDBFException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void onPostExecute() throws JDBFException, UnsupportedEncodingException {
        if (reader==null) return;
        int i;
        records = new ArrayList<QuakeRecord>();
        for (i=0; i<reader.getFieldCount(); i++) {
            System.out.print(reader.getField(i).getName()+" ");
        }
        System.out.println();
        while (reader.hasNextRecord()) {
            String strings[] = reader.nextRecordStrings();
            QuakeRecord rec = new QuakeRecord();
            for (int j=0; j<strings.length; j++) {
                rec.setField(j, strings[j]);
                System.out.print(strings[j]+" ");
            }
            records.add(rec);
            System.out.println();
        }
        for (i = 0; i < records.size()/2; i++)
        {
            QuakeRecord temp = records.get(i);
            records.set(i, records.get(records.size() - i - 1));
            records.set(records.size() - i - 1, temp);
        }
    }

    public static class QuakeRecord
    {
        public String N, DateTime, Lon, Lat, Depth, MPSP, MPLP, MS, LocRus, Loc;
        public void setField(int nField, String value) {
            switch (nField) {
                case 0:
                    N = value;
                    break;
                case 1:
                    DateTime = value;
                    break;
                case 2:
                    Lon = value;
                    break;
                case 3:
                    Lat = value;
                    break;
                case 4:
                    Depth = value;
                    break;
                case 5:
                    MPSP = value;
                    break;
                case 6:
                    MPLP = value;
                    break;
                case 7:
                    MS = value;
                    break;
                case 8:
                    Loc = value;
                    break;
                case 9:
                    LocRus = value;
            }
        }
    }
}

