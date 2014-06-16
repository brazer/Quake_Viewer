package by.org.cgm.jdbf;

import android.content.res.Resources;
import android.os.AsyncTask;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

import by.org.cgm.quakeviewer.IProgressTracker;
import by.org.cgm.quakeviewer.R;

public class JdbfTask extends AsyncTask<String, String, Boolean>
{

    public static ArrayList<QuakeRecord> records;
    private static DBFReader reader;
    protected final Resources mResources;
    private Boolean mResult;
    private String mProgressMessage;
    private IProgressTracker mProgressTracker;

    public JdbfTask(Resources resources) {
        mResources = resources;
        mProgressMessage = resources.getString(R.string.task_starting);
    }

    public void setProgressTracker(IProgressTracker progressTracker) {
        mProgressTracker = progressTracker;
        if (mProgressTracker != null) {
            mProgressTracker.onProgress(mProgressMessage);
            if (mResult != null) {
                mProgressTracker.onComplete();
            }
        }
    }

    /**
     * <p>Applications should preferably override {@link #onCancelled(Object)}.
     * This method is invoked by the default implementation of
     * {@link #onCancelled(Object)}.</p>
     * <p/>
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.</p>
     *
     * @see #onCancelled(Object)
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    @Override
    protected void onCancelled() {
        mProgressTracker = null;
    }

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     *
     * @param values The values indicating progress.
     * @see #publishProgress
     * @see #doInBackground
     */
    @Override
    protected void onProgressUpdate(String... values) {
        mProgressMessage = values[0];
        if (mProgressTracker!=null)
            mProgressTracker.onProgress(mProgressMessage);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        initReader(params[0]);
        boolean res = false;
        try {
            res = formRecords();
        } catch (JDBFException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return res;
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
    protected void onPostExecute(Boolean result) {
        mResult = result;
        if (mProgressTracker!=null)
            mProgressTracker.onComplete();
        mProgressTracker = null;
        //QuakeListActivity.progress.dismiss();
    }

    private boolean formRecords() throws JDBFException, UnsupportedEncodingException {
        if (reader==null) return false;
        int i;
        records = new ArrayList<QuakeRecord>();
        for (i=0; i<reader.getFieldCount(); i++) {
            System.out.print(reader.getField(i).getName()+" ");
        }
        System.out.println();
        boolean res;
        res = readDataAndAddRecords();
        for (i = 0; i < records.size()/2; i++)
        {
            QuakeRecord temp = records.get(i);
            records.set(i, records.get(records.size() - i - 1));
            records.set(records.size() - i - 1, temp);
        }
        return res;
    }

    private boolean readDataAndAddRecords() throws JDBFException {
        int i = 0;
        while (reader.hasNextRecord()) {
            if (isCancelled()) return false;
            String strings[] = reader.nextRecordStrings();
            QuakeRecord rec = new QuakeRecord();
            for (int j=0; j<strings.length; j++) {
                rec.setField(j, strings[j]);
                System.out.print(strings[j]+" ");
            }
            records.add(rec);
            System.out.println();
            publishProgress(mResources.getString(R.string.task_working, ++i));
        }
        return true;
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

