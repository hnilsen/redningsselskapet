package no.array.android.rs.model;

import java.util.ArrayList;

/**
 * Created by hakon on 20.01.14.
 */
public class Document {
    String name;
    String description;
    ArrayList<Style> styles = new ArrayList<Style>();
    ArrayList<Placemark> placemarks = new ArrayList<Placemark>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<Style> getStyles() {
        return styles;
    }

    public void setStyles(ArrayList<Style> styles) {
        this.styles = styles;
    }

    public ArrayList<Placemark> getPlacemarks() {
        return placemarks;
    }

    public void setPlacemarks(ArrayList<Placemark> placemarks) {
        this.placemarks = placemarks;
    }

    public void addPlacemark(Placemark placemark) {
        this.placemarks.add(placemark);
    }

    public void addStyle(Style style) {
        this.styles.add(style);
    }
}
