package no.array.android.rs.model;

/**
 * Created by hakon on 20.01.14.
 */
public class Placemark {
    String name;
    String snippet;
    String description;
    String styleUrl;
    Point Point;
    String style;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStyleUrl() {
        return styleUrl;
    }

    public void setStyleUrl(String styleUrl) {
        this.styleUrl = styleUrl;
    }

    public Point getPoint() {
        return Point;
    }

    public void setPoint(Point point) {
        Point = point;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getCoordinates() {
        return Point.getCoordinates();
    }

    public void setCoordinates(String coordinates) {
        Point.setCoordinates(coordinates);
    }
}

