package no.array.android.rs.model;

import android.util.Log;

import no.array.android.rs.MainActivity;

public class Point {
    String coordinates;

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public double getLong() {
        if(coordinates.split(",").length == 2 && coordinates.split(",")[0].length() > 0) {
            return Double.parseDouble(coordinates.split(",")[0]);
        } else {
            Log.d(MainActivity.TAG, coordinates);

            return 0;
        }
    }

    public double getLat() {
        if(coordinates.split(",").length == 2 && coordinates.split(",")[1].length() > 0) {
            return Double.parseDouble(coordinates.split(",")[1]);
        } else {
            return 0;
        }
    }
}
