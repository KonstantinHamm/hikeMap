package org.hamm.h1kemaps.app.util;

/**
 * Created by Konstantin Hamm on 25.02.15.
 * The Classes and Activities in this Project were
 * developed with the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 *
 * The code from the Skobbler support sites is Open Source therefore
 * this code is Open Source also.
 */

import org.hamm.h1kemaps.app.model.MapPack;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * SAX parser used for parsing the XML file that stores information about the
 * map packages available for download on the server.
 */
public class MapDataParser {

    /**
     * SAX parser to parse the XML
     */
    private SAXParser parser;

    /**
     * URL to the XML file
     */
    private String url;

    /**
     * Map storing download packages - populated when tha XML file is parsed
     */
    private Map<String, MapPack> packMap = new HashMap<String, MapPack>();



    /**
     * Constructor of the MapDataParser. Here's where the SAX parser is
     * initialized.
     * @param url = Url of the requested map Pack
     */
    public MapDataParser(String url) {

        this.url = url;

        try {
            // Create new SAX Parser
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch ( ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public Map<String, MapPack> getPackMap() {
        return packMap;
    }

    public void setPackMap(Map<String, MapPack> packMap) {
        this.packMap = packMap;
    }

    /**
     * This method gets the Requested XML data from the Server via Http and
     * Parses it in the next step.
     */
    public void parse() {
        HttpGet request = new HttpGet(url);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        InputStream reply;

        try {
            reply = httpClient.execute(request).getEntity().getContent();

            InputSource source = new InputSource(reply);

            source.setEncoding("UTF-8");

            ParserHandler ph = new ParserHandler();

            parser.parse(source, ph);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * This class is a convenience class for SAX applications.
     * It provides implementations for all of the callbacks in the four core SAX2 handler classes.
     */
    private class ParserHandler extends DefaultHandler {

        private static final String TAG_PACKAGES = "packages";

        private static final String TAG_WORLD = "world";

        private static final String TAG_TYPE = "type";

        private static final String TAG_SIZE = "size";

        private static final String TAG_ENGLISH_NAME = "en";

        private Stack<String> tagStack = new Stack<String>();

        private MapPack currentPackage;

        /**
         * Receive notification of the start of an element.
         * @param uri = The namespace URI, or the empty string if the element has no Namespace
         *            URI or if Namespace processing is not being performed.
         * @param localName = The local name (without prefix), or the empty string if Namespace
         *                  processing is not being performed.
         * @param qName = The qualified name (with prefix), or the empty string if qualified names
         *              are not available.
         * @throws SAXException = The attributes attached to the element. If there are no
         *              attributes, it shall be an empty Attributes object.
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (tagStack.contains(TAG_PACKAGES) && tagStack.peek().equals(TAG_PACKAGES)) {

                currentPackage = new MapPack();

                currentPackage.setCode(localName);
            }
            if (tagStack.contains(TAG_WORLD) && !tagStack.get(tagStack.size() - 1).equals(TAG_WORLD)) {

                String parentCode = tagStack.peek();

                packMap.get(localName).setParentCode(parentCode);

                packMap.get(parentCode).getChildrenCodes().add(localName);
            }
            tagStack.push(localName);
        }

        /**
         * Receive notification of the end of the element.
         * @param uri = The Namespace URI, or the empty string if the element has no Namespace
         *            URI or if Namespace processing is not being performed.
         * @param localName = The local name (without prefix), or the empty string if
         *             Namespace processing is not being performed.
         * @param qName = The qualified name (with prefix), or the empty string if qualified
         *             names are not available.
         * @throws SAXException = Any SAX exception, possibly wrapping another exception.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            tagStack.pop();

            if (tagStack.contains(TAG_PACKAGES) && tagStack.peek().equals(TAG_PACKAGES)) {

                packMap.put(currentPackage.getCode(), currentPackage);

            }
        }

        /**
         * Receives notification of character data inside an element.
         * @param ch
         * @param start
         * @param length
         * @throws SAXException
         */

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String content = new String(ch, start, length);
            if (tagStack.peek().equals(TAG_ENGLISH_NAME)) {

                currentPackage.setName(content);

            } else if (tagStack.peek().equals(TAG_TYPE)) {

                currentPackage.setType(content);

            } else if (tagStack.peek().equals(TAG_SIZE)

                    && tagStack.get(tagStack.size() - 2).equals(currentPackage.getCode())) {

                currentPackage.setSize(Integer.parseInt(content));
            }
        }
    }
}
