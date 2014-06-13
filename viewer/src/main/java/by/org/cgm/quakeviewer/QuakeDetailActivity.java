package by.org.cgm.quakeviewer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class QuakeDetailActivity extends Activity {

    public static String EXTRA_FROM_MWM = "from-maps-with-me";

    public static PendingIntent getPendingIntent(Context context)
    {
        final Intent i = new Intent(context, QuakeDetailActivity.class);
        i.putExtra(EXTRA_FROM_MWM, true);
        return PendingIntent.getActivity(context, 0, i, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_detail);




        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, QuakeListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
