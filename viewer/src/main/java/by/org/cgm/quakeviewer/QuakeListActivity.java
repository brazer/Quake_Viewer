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

public class QuakeListActivity extends ListActivity implements OnTaskCompleteListener {

    private QuakeAdapter mQuakeAdapter;
    public static boolean isLoaded = false;
    public static boolean isLoadedInternetDialog = true;
    private InternetDialog internetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_list);

        if (savedInstanceState!=null) {
            isLoaded = savedInstanceState.getBoolean("isLoaded");
            isLoadedInternetDialog = savedInstanceState.getBoolean("isInternet");
            mQuakeAdapter = (QuakeAdapter) savedInstanceState.getSerializable("adapter");
            setListAdapter(mQuakeAdapter);
            setShowAllListener();
        }
        if (!isLoaded) createLoadDialog();
        internetDialog = new InternetDialog(this, this);
        if (!isLoadedInternetDialog) {
            String url = savedInstanceState.getString("url");
            showInternetDialog(url);
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
        start.setListener(this);
        start.show(getFragmentManager(), null);
    }

    private void showInternetDialog(String url) {
        internetDialog.setUrl(url);
        isLoadedInternetDialog = false;
        internetDialog.show(getFragmentManager(), null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isLoaded", isLoaded);
        outState.putBoolean("isInternet", isLoadedInternetDialog);
        String url = internetDialog.getUrl();
        outState.putCharSequence("url", url);
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

        final String title = quakes.size() == 1 ? quakes.get(0).name : "Землетрясения";
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

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p/>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p/>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link android.view.Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p/>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p/>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.</p>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.load) {
            isLoaded = false;
            createLoadDialog();
        }
        if (id==R.id.exit) System.exit(0);
        return super.onOptionsItemSelected(item);
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
