package no.array.android.rs;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import no.array.android.rs.interfaces.RefreshLocationListener;
import no.array.android.rs.kml.KMLParser;

/**
 * Created by hakon on 19.01.14.
 */

public class MainActivity extends Activity implements RefreshLocationListener {
    public static String TAG = "RS";
    public static Boolean DEBUG = true;
    static GoogleMap mMap;
    static MapLocation mLocation;
    private boolean mHasGottenFirstLocationUpdate = false;
    Marker mBoatMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocation = new MapLocation(this, this);
        setContentView(R.layout.rs_maps_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            // my position might not be necessary
            //mMap.setMyLocationEnabled(true);
            KMLParser.updateData(mMap, this);
            updatePositionFields();
        }
    }

    private void setBoatMarker() {
        MarkerOptions mo = new MarkerOptions();
        mo.position(mLocation.getLatLng());
        mo.title("Min båt");

        BitmapDescriptor bmd = BitmapDescriptorFactory.fromResource(R.drawable.ic_boat);
        mo.icon(bmd);

        if(mBoatMarker != null) {
            mBoatMarker.remove();
        }
        mBoatMarker = mMap.addMarker(mo);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mLocation.stopPositionFetching();
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
        mMap.animateCamera(mLocation.getLatLngZoom(10));
    }

    @Override
    public void updateLocation() {
        updatePositionFields();
        setBoatMarker();

        if(!mHasGottenFirstLocationUpdate) {
            setLocation();
            mHasGottenFirstLocationUpdate = true;
        }
    }
}
