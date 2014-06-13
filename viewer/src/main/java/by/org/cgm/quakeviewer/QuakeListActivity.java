package by.org.cgm.quakeviewer;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import by.org.cgm.jdbf.JdbfTask;
import by.org.cgm.quakeviewer.quake.QuakeContent;
import by.org.cgm.quakeviewer.quake.QuakeContent.QuakeItem;

public class QuakeListActivity extends ListActivity {

    QuakeAdapter mQuakeAdapter;
    public static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quake_list);

        progress = ProgressDialog.show(this, "Загрузка", "Загружаются данные", true, false);
        JdbfTask task = new JdbfTask();
        task.execute("http://www.brazer.url.ph/data.dbf");
        try {
            task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        QuakeContent.init();
        mQuakeAdapter = new QuakeAdapter(this, QuakeContent.ITEMS);
        setListAdapter(mQuakeAdapter);

        findViewById(R.id.btn_all).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuakes(QuakeContent.ITEMS);
            }
        });
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

        final String title = quakes.size() == 1 ? quakes.get(0).name : "Quakes of the World";
        MapsWithMeApi.showPointsOnMap(this, title, QuakeDetailActivity.getPendingIntent(this), points);
    }

    private static class QuakeAdapter extends ArrayAdapter<QuakeItem>
    {
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
