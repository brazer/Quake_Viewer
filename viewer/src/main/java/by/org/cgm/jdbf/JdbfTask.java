package by.org.cgm.jdbf;

import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import by.org.cgm.quakeviewer.QuakeListActivity;

public class JdbfTask extends AsyncTask<String, Integer, Integer>
{

    public static ArrayList<QuakeRecord> records;
    private static DBFReader reader;

    @Override
    protected Integer doInBackground(String... params) {
        initReader(params[0]);
        try {
            formRecords();
        } catch (JDBFException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return records.size();
    }

    private void initReader(String strUrl) {
        try {
            URL url = new URL(strUrl);
            reader = new DBFReader(url.openStream());
        }
        catch (Exception e) {
            e.printStackTrace();
            reader = readFromFile();
        }
    }

    private DBFReader readFromFile() {
        try {
            return new DBFReader("/mnt/sdcard/Download/data.dbf");
        } catch (JDBFException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p/>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param result The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(Integer result) {
        QuakeListActivity.progress.dismiss();
    }

    private void formRecords() throws JDBFException, UnsupportedEncodingException {
        if (reader==null) return;
        int i;
        records = new ArrayList<QuakeRecord>();
        for (i=0; i<reader.getFieldCount(); i++) {
            System.out.print(reader.getField(i).getName()+" ");
        }
        System.out.println();
        readDataAndAddRecords();
        for (i = 0; i < records.size()/2; i++)
        {
            QuakeRecord temp = records.get(i);
            records.set(i, records.get(records.size() - i - 1));
            records.set(records.size() - i - 1, temp);
        }
    }

    private void readDataAndAddRecords() throws JDBFException {
        while (reader.hasNextRecord()) {
            String strings[] = reader.nextRecordStrings();
            QuakeRecord rec = new QuakeRecord();
            for (int j=0; j<strings.length; j++) {
                rec.setField(j, strings[j]);
                System.out.print(strings[j]+" ");
            }
            records.add(rec);
            System.out.println();
            publishProgress(records.size());
            if (isCancelled()) break;
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

