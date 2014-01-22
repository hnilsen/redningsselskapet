package no.array.android.rs;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import no.array.android.rs.interfaces.RefreshLocationListener;

/**
 * Created by hakon on 21.01.14.
 */
public class MapLocation implements LocationListener,
                                    GooglePlayServicesClient.ConnectionCallbacks,
                                    GooglePlayServicesClient.OnConnectionFailedListener {
    double mLat = 0;
    double mLong = 0;
    double mAccuracy = 0;
    double mHeight = 0;
    static LocationManager mLocationManager;
    Context mContext;
    RefreshLocationListener refreshLocationListener;

    private static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    LocationRequest mLocationRequest;
    LocationClient mLocationClient;

    /**
     * Constructor for setting up map location
     * @param mContext Activity
     * @param callback Interface to communicate back with
     */
    public MapLocation(Context mContext, RefreshLocationListener callback) {
        this.mContext = mContext;
        refreshLocationListener = callback;

        setupPositionFetching();
    }

    public void startPositionFetching() {
        mLocationClient.connect();
    }

    public void stopPositionFetching() {
        mLocationClient.disconnect();
    }

    private void setupPositionFetching() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        mLocationClient = new LocationClient(mContext, this, this);
        startPositionFetching();
    }

    private void updatePosition() {
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
        return (int) loc;
    }

    public int getMinutes(double loc) {
        return (int) ((loc % 1) * 60);
    }

    public int getSeconds(double loc) {
        double minutes = (loc % 1) * 60;
        return (int) ((minutes % 1) * 60);
    }

    @Override
    public void onLocationChanged(Location location) {
        // update position values
        updatePosition();
        // Report to the activity that the location was updated
        refreshLocationListener.updateLocation();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        // update position values
        updatePosition();
        // Report to the activity that the location was updated
        refreshLocationListener.updateLocation();
    }

    @Override
    public void onDisconnected() {
        // Do something meaningful here?
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Do something meaningful here?
    }
}
