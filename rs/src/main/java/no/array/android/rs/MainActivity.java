package no.array.android.rs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.array.android.rs.model.Kml;
import no.array.android.rs.model.Placemark;


import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;

/**
 * Created by hakon on 19.01.14.
 */

public class MainActivity extends Activity {
    public static String TAG = "RS";
    public static Boolean DEBUG = true;
    private static String sUserAgent = null;
    public static String KML_URL = "http://www.redningsselskapet.no/systemsider/kml?random=";
    public static final String PREFS = "kml_data";
    public static final String FILENAME = "kml_data.xml";
    static GoogleMap mMap;
    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rs_maps_fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        setMap();
        updateData(this);
    }

    private static void setMarkers(Kml kml) {
        for (Placemark placemark : kml.getDocument().getPlacemarks()) {
            double mLat = placemark.getPoint().getLat();
            double mLong = placemark.getPoint().getLong();

            if (mLat != 0 || mLong != 0) {
                int resource = mContext.getResources().getIdentifier(placemark.getStyle(), "drawable", mContext.getPackageName());
                BitmapDescriptor bmd = BitmapDescriptorFactory.fromResource(resource);

                MarkerOptions mo = new MarkerOptions();
                mo.position(new LatLng(mLat, mLong));
                mo.title(placemark.getName());

                if (placemark.getStyle().contains("rs_")) { // bare vise beskrivelse for båtene (telefonnummer)
                    mo.snippet(placemark.getDescription());
                }

                if (bmd != null) {
                    mo.icon(bmd);
                }

                mMap.addMarker(mo);
            }
        }
    }

    private void setMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

    private static void setLocation() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 10));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(60.29863, 5.24064), 10));
        }
    }

    public static Kml ParseDataLocally(Context context) {
        Kml kml = new Kml();
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();

            // Get XMLReader of the SAXParser
            XMLReader xr = sp.getXMLReader();

            // KML content handler
            Parser kmlHandler = new Parser();
            xr.setContentHandler(kmlHandler);

            // Get XML from URL
            InputSource source = new InputSource(new StringReader(getDataLocally(context)));
            xr.parse(source);

            kml = kmlHandler.getKml();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kml;
    }

    /**
     * Åpner webforbindelse og henter ut XML-teksten
     *
     * @param url URL til tjenesten/XML-objektet
     * @return Returnerer KML-data som streng
     */
    protected static String getDataFromServer(Context context, String url) {
        if (sUserAgent == null) {
            prepareUserAgent(context);
        }
        String response = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        try {
            if (sUserAgent != null) {
                httpGet.setHeader("User-Agent", sUserAgent);
            }
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();
            InputStreamReader contentReader = new InputStreamReader(content, "UTF-8");
            contentReader.getEncoding();

            BufferedReader buffer = new BufferedReader(contentReader);
            String s;
            while ((s = buffer.readLine()) != null) {
                response += s;
            }

            if (response.charAt(0) == 65279) {
                response = response.substring(1);
            }

        } catch (Exception e) {
            if (DEBUG)
                Log.d(TAG, "Error in http connection " + e.toString());
        }
        return response;
    }

    /**
     * Check if internet connection is up
     *
     * @param context The application context
     * @return True if it's up, false if it's not
     */
    static public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Shorthand-funksjon for å laste ned filen på nytt
     *
     * @param context Applikasjonskonteksten
     */
    public static void updateData(Context context) {
        if (isOnline(context)) {
            updateDataTask update = new updateDataTask(context);
            update.execute();
        }
    }

    /**
     * Oppdaterer KML-data, lagrer til internminne dersom de finnes.
     */
    private static class updateDataTask extends AsyncTask<Void, Void, Void> {
        private updateDataTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (isOnline(mContext)) {
                String fileUrl = KML_URL + Math.random() * 1000000;
                String response = getDataFromServer(mContext, fileUrl);
                if (response != null) {
                    storeDataLocally(mContext, response);
                }
            } else {
                Log.d(MainActivity.TAG, mContext.getResources().getString(R.string.update_not_online));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Kml kml = ParseDataLocally(mContext);
            if (kml != null) {
                setMarkers(kml);
            }
            setLocation();
        }
    }

    /**
     * Les ut XML-objektet som er lagret på internminnet
     *
     * @param context Applikasjonskontekst
     * @return Returnerer streng med XML-data
     */
    public static String getDataLocally(Context context) {
        String response = null;
        // prøver å åpne opp filen
        try {
            // open the file for reading
            InputStream instream = context.openFileInput(FILENAME);

            // forbered filen for lesing
            InputStreamReader inputreader = new InputStreamReader(instream);
            BufferedReader reader = new BufferedReader(inputreader);
            StringBuilder builder = new StringBuilder();

            // kjør gjennom filen linjevis (bare en linje, antakeligvis)
            for (String line; (line = reader.readLine()) != null; ) {
                builder.append(line).append("\n");
            }

            // lukk filen
            instream.close();

            // returner innholdet i filen som streng
            response = builder.toString();
        } catch (FileNotFoundException e) {
            if (DEBUG)
                Log.d(TAG, "Klarte ikke finne internt lagret XML-fil - prøver igjen: " + e.getLocalizedMessage());
        } catch (IOException e) {
            // det oppstod en feil når den prøver å lese den internt lagrede filen
            if (DEBUG)
                Log.d(TAG, "Klarte ikke åpne internt lagret XML-fil: " + e.getMessage());
        } catch (Exception e) {
            // det oppstod en annen feil
            if (DEBUG)
                Log.d(TAG, "Det oppstod en annen feil ved lesing av internt lagret XML-fil: " + e.getMessage());
        }

        return response;
    }


    /**
     * Lagre responsen (KML data) lokalt i internminne
     *
     * @param context  Applikasjonskontekst
     * @param response KML som tekst (nedlastet XML)
     */
    protected static void storeDataLocally(Context context, String response) {
        Resources res = context.getResources();
        SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = settings.edit();

        try {
            // Write the file with the content
            FileOutputStream fos;

            fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(response.getBytes());
            fos.close();

            // Update settings with new time stamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            String currentDateTimeString = sdf.format(new Date());

            edit.putString("updateTime", currentDateTimeString);
            edit.commit();
        } catch (FileNotFoundException e) {
            if (DEBUG)
                Log.d(TAG, "Klarte ikke åpne intern lagret KML-fil", e);
        } catch (IOException e) {
            if (DEBUG)
                Log.d(TAG, "Klarte ikke lese XML-format for lagring", e);
        }

        // Dersom det er satt et timestamp skal vi ikke oppdatere med "Ikke satt"
/*
        if(!settings.getString("updateTime", "").equals(res.getString(R.string.updateTime_not_set))) {
            edit.putString("updateTime", res.getString(R.string.updateTime_not_set));
            edit.commit();
        }
*/
    }

    /**
     * Prepare the internal User-Agent string for use. This requires a
     * {@link Context} to pull the package name and version number for this
     * application.
     *
     * @param context Application context
     */
    public static void prepareUserAgent(Context context) {
        try {
            // Read package name and version number from manifest
            PackageManager manager = context.getPackageManager();
            assert manager != null;
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            sUserAgent = String.format(context.getString(R.string.template_user_agent),
                    info.packageName, info.versionName);

        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG)
                Log.d(TAG, "Couldn't find package information in PackageManager", e);
        }
    }
}
