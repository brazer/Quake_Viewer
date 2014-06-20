package by.org.cgm.quakeviewer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MWMResponse;
import com.mapswithme.maps.api.MapsWithMeApi;

import by.org.cgm.quakeviewer.quake.QuakeContent;

public class QuakeDetailActivity extends Activity {

    public static String EXTRA_FROM_MWM = "from-maps-with-me";
    private TextView mNumber, mContent, mLon, mLat, mLocation;
    private QuakeContent.QuakeItem mQuake;

    public static PendingIntent getPendingIntent(Context context)
    {
        final Intent intent = new Intent(context, QuakeDetailActivity.class);
        intent.putExtra(EXTRA_FROM_MWM, true);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_detail);

        mNumber = (TextView) findViewById(R.id.number);
        mContent = (TextView) findViewById(R.id.description);
        mLon = (TextView) findViewById(R.id.lon);
        mLat = (TextView) findViewById(R.id.lat);
        mLocation = (TextView) findViewById(R.id.location);

        findViewById(R.id.showOnMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsWithMeApi
                        .showPointOnMap(QuakeDetailActivity.this,
                                mQuake.lat, mQuake.lon, mQuake.content);
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_FROM_MWM, false)) {
            final MWMResponse response = MWMResponse.extractFromIntent(this, intent);
            MWMPoint point = response.getPoint();
            mQuake = QuakeContent.getItemFromPoint(point);
            if (mQuake!=null) {
                mNumber.setText(mQuake.id);
                mLat.setText(String.valueOf(mQuake.lat));
                mLon.setText(String.valueOf(mQuake.lon));
                mContent.setText(mQuake.content);
                mLocation.setText(mQuake.location);
            }
        }
    }

}
