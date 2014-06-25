package by.org.cgm.quakeviewer;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import by.org.cgm.jdbf.JdbfTask;
import by.org.cgm.quakeviewer.quake.QuakeContent;
import by.org.cgm.quakeviewer.quake.QuakeContent.QuakeItem;

public class QuakeListActivity extends ListActivity
        implements OnTaskCompleteListener, OpenFileDialog.OpenDialogListener {

    private QuakeAdapter mQuakeAdapter;
    public static boolean isLoaded;
    public static boolean isLoadedInternetDialog;
    public static InternetDialog internetDialog;
    public static boolean isLoadedFileDialog;
    public static OpenFileDialog fileDialog;
    public static OpenFileDialog.OpenDialogListener listener;

    {
        isLoaded = false;
        isLoadedInternetDialog = true;
        isLoadedFileDialog = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_list);

        if (savedInstanceState!=null) {
            isLoaded = savedInstanceState.getBoolean("isLoaded");
            isLoadedInternetDialog = savedInstanceState.getBoolean("isInternet");
            isLoadedFileDialog = savedInstanceState.getBoolean("isLocal");
            mQuakeAdapter = (QuakeAdapter) savedInstanceState.getSerializable("adapter");
            setListAdapter(mQuakeAdapter);
            setShowAllListener();
        }
        if (!isLoaded) createLoadDialog();
        internetDialog = new InternetDialog(this, this);
        if (!isLoadedInternetDialog & savedInstanceState!=null) {
            String url = savedInstanceState.getString("url");
            showInternetDialog(url);
        }
        fileDialog = new OpenFileDialog(this);
        fileDialog.setFolderIcon(getResources().getDrawable(R.drawable.abc_ic_go));
        listener = this;
        fileDialog.setOpenDialogListener(listener);
        if (!isLoadedFileDialog & savedInstanceState!=null) {
            String path = savedInstanceState.getString("path");
            showOpenFileDialog(path);
        }
    }

    private void setShowAllListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuakes(QuakeContent.QUAKES);
            }
        };
        findViewById(R.id.btn_all).setOnClickListener(listener);
        if (QuakeContent.QUAKES.size()>0)
            findViewById(R.id.btn_all).setEnabled(true);
        else findViewById(R.id.btn_all).setEnabled(false);
    }

    private void createLoadDialog() {
        LoadDialog start = new LoadDialog();
        start.setContext(this);
        start.show(getFragmentManager(), null);
    }

    private void showInternetDialog(String url) {
        internetDialog.setUrl(url);
        isLoadedInternetDialog = false;
        internetDialog.show(getFragmentManager(), null);
    }

    private void showOpenFileDialog(String path) {
        isLoadedFileDialog = false;
        fileDialog.setCurrentPath(path);
        fileDialog.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isLoaded", isLoaded);
        outState.putBoolean("isInternet", isLoadedInternetDialog);
        String url = internetDialog.getUrl();
        outState.putCharSequence("url", url);
        outState.putBoolean("isLocal", isLoadedFileDialog);
        String path = fileDialog.getCurrentPath();
        outState.putCharSequence("path", path);
        outState.putSerializable("adapter", mQuakeAdapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        List<QuakeItem> list = new ArrayList<QuakeItem>();
        list.add(mQuakeAdapter.getItem(position));
        showQuakes(list);
    }

    private void showQuakes(List<QuakeItem> quakes) {
        MWMPoint[] points = new MWMPoint[quakes.size()];
        for (int i = 0; i < quakes.size(); i++)
            points[i] = quakes.get(i).toMWMPoint();

        final String title = (quakes.size()==1) ? quakes.get(0).title : "Землетрясения";
        MapsWithMeApi.showPointsOnMap(this, title, QuakeDetailActivity.getPendingIntent(this), points);
    }

    @Override
    public void onTaskComplete(JdbfTask task) {
        if (task.isCancelled())
            Toast.makeText(this, R.string.task_cancelled, Toast.LENGTH_LONG).show();
        else {
            Boolean result = null;
            try {
                result = task.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            Toast.makeText(this,
                    getString(R.string.task_completed, (result!=null) ? result.toString() : "null"),
                    Toast.LENGTH_LONG).show();
        }

        if (!QuakeContent.init()) return;
        mQuakeAdapter = new QuakeAdapter(this, QuakeContent.QUAKES);
        setListAdapter(mQuakeAdapter);
        setShowAllListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.load) {
            isLoaded = false;
            createLoadDialog();
        }
        if (id==R.id.exit) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnSelectedFile(String fileName) {
        if (!fileName.contains("dbf")) {
            Toast.makeText(this, "Выберите dbf-файл", Toast.LENGTH_SHORT).show();
        }
        else {
            AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager(this, this);
            mAsyncTaskManager.setupTask(new JdbfTask(getResources()), fileName);
            isLoadedFileDialog = true;
        }
    }

    private static class QuakeAdapter extends ArrayAdapter<QuakeItem> implements Serializable {

        private final List<QuakeItem> data;

        public QuakeAdapter(Context context, List<QuakeItem> quakes)
        {
            super(context, android.R.layout.simple_list_item_2, android.R.id.text1, quakes);
            data = quakes;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            final View view = super.getView(position, convertView, parent);
            final TextView subText = (TextView) view.findViewById(android.R.id.text2);
            final QuakeItem quake = data.get(position);
            subText.setText(quake.content);
            return view;
        }
    }
}
