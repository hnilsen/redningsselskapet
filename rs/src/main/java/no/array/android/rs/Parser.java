package no.array.android.rs;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;

import no.array.android.rs.model.Document;
import no.array.android.rs.model.Icon;
import no.array.android.rs.model.IconStyle;
import no.array.android.rs.model.Kml;
import no.array.android.rs.model.Placemark;
import no.array.android.rs.model.Point;
import no.array.android.rs.model.Style;

/**
 * Created by hakon on 20.01.14.
 */

public class Parser extends DefaultHandler {
    private Placemark currentPlacemark = new Placemark();
    private Style currentStyle = new Style();

    private Kml kml;

    boolean inKml = false;

    boolean inDocument = false;
    boolean inDocumentName = false;
    boolean inDocumentDescription = false;

    boolean inStyle = false;
    boolean inIconStyle = false;
    boolean inIcon = false;
    boolean inHref = false;

    boolean inPlacemark = false;
    boolean inPlaceMarkName = false;
    boolean inPlaceMarkDescription = false;
    boolean inCoordinates = false;
    boolean inPoint = false;
    boolean inSnippet = false;
    boolean inStyleUrl = false;

    String currentString = "";

    @Override
    public void startDocument() throws SAXException {
        Log.d("XML","startDocument");
        kml = new Kml();
    }

    @Override
    public void endDocument() throws SAXException {
        Log.d("XML","endDocument");
    }
    /** Gets called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">*/
    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if(localName.equalsIgnoreCase("kml")) {
            this.inKml = true;
        } else if(localName.equalsIgnoreCase("document")) {
            this.inDocument = true;
            kml.setDocument(new Document());
        } else if (localName.equalsIgnoreCase("name") && !this.inPlacemark) {
            this.inDocumentName = true;
        } else if (localName.equalsIgnoreCase("description") && !this.inPlacemark) {
            this.inDocumentDescription = true;

        }else if(localName.equalsIgnoreCase("style")) {
            this.inStyle = true;
            currentStyle = new Style();
            currentStyle.setId(attributes.getValue("id"));
        } else if(localName.equalsIgnoreCase("iconstyle")) {
            this.inIconStyle = true;
        } else if(localName.equalsIgnoreCase("icon")) {
            this.inIcon = true;
        } else if(localName.equalsIgnoreCase("href")) {
            this.inHref = true;

        } else if(localName.equalsIgnoreCase("placemark")) {
            this.inPlacemark = true;
            currentPlacemark = new Placemark();
        } else if(localName.equalsIgnoreCase("name") && this.inPlacemark) {
            this.inPlaceMarkName = true;
        } else if(localName.equalsIgnoreCase("description") && this.inPlacemark) {
            this.inPlaceMarkDescription = true;
        } else if(localName.equalsIgnoreCase("snippet")) {
            this.inSnippet = true;
        } else if(localName.equalsIgnoreCase("point")) {
            this.inPoint = true;
        } else if(localName.equalsIgnoreCase("coordinates")) {
            this.inCoordinates = true;
        } else if(localName.equalsIgnoreCase("styleurl")) {
            this.inStyleUrl = true;
        }
    }

    /** Gets called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
            // KML AND DOCUMENT
        if(localName.equalsIgnoreCase("kml")) {
            this.inKml = false;
        } else if(localName.equalsIgnoreCase("document")) {
            this.inDocument = false;
        } else if (localName.equalsIgnoreCase("name") && !this.inPlacemark) {
            this.kml.getDocument().setName(currentString);
            this.inDocumentName = false;
        } else if (localName.equalsIgnoreCase("description") && !this.inPlacemark) {
            this.kml.getDocument().setDescription(currentString);
            this.inDocumentDescription = false;

            // STYLE
        } else if(localName.equalsIgnoreCase("style")) {
            this.inStyle = false;
            kml.getDocument().addStyle(currentStyle);
        } else if(localName.equalsIgnoreCase("iconstyle")) {
            // we will set the iconstyle in "href"
            this.inIconStyle = false;
        } else if(localName.equalsIgnoreCase("icon")) {
            // we will set the icon in "href"
            this.inIcon = false;
        } else if(localName.equalsIgnoreCase("href")) {
            IconStyle iconStyle = new IconStyle();
            Icon icon = new Icon();
            icon.setHref(currentString);

            iconStyle.setIcon(icon);
            currentStyle.setIconStyle(iconStyle);

            this.inHref = false;

            // PLACEMARK
        } else if(localName.equalsIgnoreCase("placemark")) {
            kml.getDocument().addPlacemark(currentPlacemark);
            this.inPlacemark = false;

        } else if(localName.equalsIgnoreCase("name") && this.inPlacemark) {
            currentPlacemark.setName(currentString);
            this.inPlaceMarkName = false;

        } else if(localName.equalsIgnoreCase("description") && this.inPlacemark) {
            currentPlacemark.setDescription(currentString);
            this.inPlaceMarkDescription = false;

        } else if(localName.equalsIgnoreCase("snippet")) {
            currentPlacemark.setSnippet(currentString);
            this.inSnippet = false;

        } else if(localName.equalsIgnoreCase("point")) {
            // we will set the point in "coordinates"
            this.inPoint = false;

        } else if(localName.equalsIgnoreCase("coordinates")) {
            Point point = new Point();
            point.setCoordinates(currentString);
            currentPlacemark.setPoint(point);
            this.inCoordinates = false;

        } else if(localName.equalsIgnoreCase("styleurl")) {
            currentPlacemark.setStyleUrl(currentString);
            this.inStyleUrl = false;
        }

        currentString = "";
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        String str = new String(ch, start, length);

        if(this.inDocumentName && !this.inPlacemark) {
            currentString += str;
        } else if(this.inDocumentDescription && !this.inPlacemark) {
            currentString += str;
        } else if(this.inHref) {
            currentString += str;
        } else if(this.inPlaceMarkName) {
            currentString += str;
        } else if(this.inPlaceMarkDescription) {
            currentString += str;
        } else if(this.inSnippet) {
            currentString += str;
        } else if(this.inStyleUrl) {
            currentString += str;
        } else if(this.inCoordinates) {
            currentString += str;
        }
    }

    public Kml getKml() {
        HashMap<String, String> styleMap = new HashMap<String, String>();
        for(Style style : kml.getDocument().getStyles()) {
            String id = style.getId();
            String href = style.getIconStyle().getIcon().getHref();

            String tempUrl = href.split("\\?")[0].toLowerCase();
            String imageName = tempUrl.split("/")[tempUrl.split("/").length-1];

            if(!styleMap.containsKey(id)) {
                if(imageName.contains("malteserkors")) {
                    styleMap.put(id, "malteserkors");
                } else {
                    String tempImageName = imageName.split("\\.")[0];
                    styleMap.put(id, "rs_" + tempImageName); // rs_9.png --> rs_9
                }
            }
        }

        ArrayList<Placemark> pms = new ArrayList<Placemark>();

        for(Placemark pm : kml.getDocument().getPlacemarks()) {
            String styleId = pm.getStyleUrl().replace("#", "");
            if(styleMap.containsKey(styleId)) {
                pm.setStyle(styleMap.get(styleId));
            }

            pms.add(pm);
        }

        kml.getDocument().setPlacemarks(pms);

        return kml;
    }
}