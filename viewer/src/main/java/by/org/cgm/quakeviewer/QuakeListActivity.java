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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_list);

        boolean isLoaded = false;
        if (savedInstanceState!=null) isLoaded = savedInstanceState.getBoolean("isLoaded");
        if (!isLoaded) createStartDialog();
        else {
            mQuakeAdapter = (QuakeAdapter) savedInstanceState.getSerializable("adapter");
            setListAdapter(mQuakeAdapter);
            setShowAllListener();
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
    }

    private void createStartDialog() {
        StartDialog start = new StartDialog();
        start.setContext(this);
        start.setListener(this);
        start.show(getFragmentManager(), null);
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed
     * so that the state can be restored in {@link #onCreate} or
     * {@link #onRestoreInstanceState} (the {@link android.os.Bundle} populated by this method
     * will be passed to both).
     * <p/>
     * <p>This method is called before an activity may be killed so that when it
     * comes back some time in the future it can restore its state.  For example,
     * if activity B is launched in front of activity A, and at some point activity
     * A is killed to reclaim resources, activity A will have a chance to save the
     * current state of its user interface via this method so that when the user
     * returns to activity A, the state of the user interface can be restored
     * via {@link #onCreate} or {@link #onRestoreInstanceState}.
     * <p/>
     * <p>Do not confuse this method with activity lifecycle callbacks such as
     * {@link #onPause}, which is always called when an activity is being placed
     * in the background or on its way to destruction, or {@link #onStop} which
     * is called before destruction.  One example of when {@link #onPause} and
     * {@link #onStop} is called and not this method is when a user navigates back
     * from activity B to activity A: there is no need to call {@link #onSaveInstanceState}
     * on B because that particular instance will never be restored, so the
     * system avoids calling it.  An example when {@link #onPause} is called and
     * not {@link #onSaveInstanceState} is when activity B is launched in front of activity A:
     * the system may avoid calling {@link #onSaveInstanceState} on activity A if it isn't
     * killed during the lifetime of B since the state of the user interface of
     * A will stay intact.
     * <p/>
     * <p>The default implementation takes care of most of the UI per-instance
     * state for you by calling {@link android.view.View#onSaveInstanceState()} on each
     * view in the hierarchy that has an id, and by saving the id of the currently
     * focused view (all of which is restored by the default implementation of
     * {@link #onRestoreInstanceState}).  If you override this method to save additional
     * information not captured by each individual view, you will likely want to
     * call through to the default implementation, otherwise be prepared to save
     * all of the state of each view yourself.
     * <p/>
     * <p>If called, this method will occur before {@link #onStop}.  There are
     * no guarantees about whether it will occur before or after {@link #onPause}.
     *
     * @param outState Bundle in which to place your saved state.
     * @see #onCreate
     * @see #onRestoreInstanceState
     * @see #onPause
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isLoaded", true);
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
        if (id==R.id.load) createStartDialog();
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
