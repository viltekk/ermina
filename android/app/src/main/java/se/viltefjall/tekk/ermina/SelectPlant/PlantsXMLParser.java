package se.viltefjall.tekk.ermina.SelectPlant;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class PlantsXMLParser {
    @SuppressWarnings("unused")
    private static final String ID  = "PlantsXMLParser";

    private String mPlantsURL;

    PlantsXMLParser(String plantsURL) {
        mPlantsURL = plantsURL;
    }

    Plants getPlants() throws XmlPullParserException, IOException {
        return parse(downloadUrl(mPlantsURL));
    }

    private Plants parse(InputStream in) throws XmlPullParserException, IOException {
        Plants plants = new Plants();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, null, "plants");
            while(parser.next() != XmlPullParser.END_TAG) {
                if(parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                if(name.equals("plant")) {
                    plants.addPlant(readPlant(parser));
                } else {
                    skip(parser);
                }
            }
        } finally {
            in.close();
        }
        return plants;
    }

    private Plant readPlant(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, null, "plant");
        String name  = null;
        String latin = null;
        int hi       = 0;
        int lo       = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String tagName = parser.getName();
            switch (tagName) {
                case "name":
                    name = readString(parser, "name");
                    break;
                case "latin":
                    latin = readString(parser, "latin");
                    break;
                case "hi":
                    hi = Integer.parseInt(readString(parser, "hi"));
                    break;
                case "lo":
                    lo = Integer.parseInt(readString(parser, "lo"));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return new Plant(name, latin, hi, lo);
    }

    private String readString(XmlPullParser parser, String tag)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String t = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return t;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }
}
