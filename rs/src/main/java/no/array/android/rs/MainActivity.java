package no.array.android.rs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

/**
 * Created by hakon on 19.01.14.
 */

public class MainActivity extends Activity {
    public static String TAG = "RS";
    public static Boolean DEBUG = true;
    static GoogleMap mMap;
    static MapLocation mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = new MapLocation(this);
        setContentView(R.layout.rs_maps_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            KMLParser.updateData(mMap, this);
            setLocation();
            updatePositionFields();
        }
    }

    private void updatePositionFields() {
        TextView lat = (TextView) findViewById(R.id.location_lat);
        TextView lng = (TextView) findViewById(R.id.location_long);
        TextView acc = (TextView) findViewById(R.id.location_accuracy);

        int latDeg = mLocation.getDegrees(mLocation.getLat());
        int latMin = mLocation.getMinutes(mLocation.getLat());
        int latSec = mLocation.getSeconds(mLocation.getLat());

        int longDeg = mLocation.getDegrees(mLocation.getLong());
        int longMin = mLocation.getMinutes(mLocation.getLong());
        int longSec = mLocation.getSeconds(mLocation.getLong());

        lat.setText("N " + latDeg + "° " + latMin + "' " + latSec + "\"");
        lng.setText("Ø " + longDeg + "° " + longMin + "' " + longSec + "\"");
        acc.setText(mLocation.getAccuracy() + " meter");
    }

    private void setLocation() {
        mMap.animateCamera(mLocation.getLatLngZoom(9));
    }
}
