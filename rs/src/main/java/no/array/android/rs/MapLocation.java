package no.array.android.rs;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by hakon on 21.01.14.
 */
public class MapLocation {
    double mLat = 0;
    double mLong = 0;
    double mAccuracy = 0;
    double mHeight = 0;
    static LocationManager mLocationManager;
    Context mContext;

    public MapLocation(Context mContext) {
        this.mContext = mContext;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        updatePosition();
    }

    public void updatePosition() {
        Criteria criteria = new Criteria();

        Location location = mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(criteria, false));
        mLat = location.getLatitude();
        mLong = location.getLongitude();
        mAccuracy = location.getAccuracy();
        mHeight = location.getAltitude();
    }

    public LatLng getLatLng() {
        return new LatLng(getLat(), getLong());
    }

    public CameraUpdate getLatLngZoom(int zoom) {
        return CameraUpdateFactory.newLatLngZoom(getLatLng(), zoom);
    }

    public double getLat() {
        return mLat;
    }

    public double getLong() {
        return mLong;
    }

    public int getAccuracy() {
        return (int) mAccuracy;
    }

    public double getHeight() {
        return mHeight;
    }

    public int getDegrees(double loc) {
        return (int)loc;
    }

    public int getMinutes(double loc) {
        return (int)((loc % 1) * 60);
    }

    public int getSeconds(double loc) {
        double minutes = (loc % 1) * 60;
        return (int)((minutes % 1) * 60);
    }
}
