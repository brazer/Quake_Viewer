package by.org.cgm.quakeviewer.quake;import com.mapswithme.maps.api.MWMPoint;import java.util.ArrayList;import java.util.List;import by.org.cgm.jdbf.JdbfTask;/** * Helper class for providing sample content for user interfaces created by * Android template wizards. * <p> */public class QuakeContent {    /**     * An array of sample (quake) items.     */    public static List<QuakeItem> ITEMS = new ArrayList<QuakeItem>();    private static void addItem(QuakeItem item) {        ITEMS.add(item);    }    public static boolean init() {        try {            for (JdbfTask.QuakeRecord rec : JdbfTask.records) {                addItem(new QuakeItem(rec.N, Double.parseDouble(rec.Lon), Double.parseDouble(rec.Lat), rec.LocRus, rec.DateTime));            }        } catch (Exception ex) {            ex.printStackTrace();            return false;        }        return true;    }    /**     * A quake item representing a piece of content.     */    public static class QuakeItem {        public String id;        public String content;        public String datetime;        public double lat, lon;        public String name;        public QuakeItem(String id, double longitude, double latitude, String name) {            this.id = id;            lon = longitude;            lat = latitude;            this.name = name;        }        public QuakeItem(String id, double longitude, double latitude, String name, String content) {            this.id = id;            lon = longitude;            lat = latitude;            this.name = name;            this.content = content;        }        public MWMPoint toMWMPoint() {            return new MWMPoint(lat, lon, name, id);        }        @Override        public String toString() {            return name;        }    }}