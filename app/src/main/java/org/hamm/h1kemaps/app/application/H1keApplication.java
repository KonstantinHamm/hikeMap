package org.hamm.h1kemaps.app.application;

/**
 * Created by Konstantin Hamm on 25.02.15.
 * The Classes and Activities in this Project were
 * developed with the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 */

import com.skobbler.ngx.map.SKAnnotation;
import org.hamm.h1kemaps.app.model.MapPack;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


/**
 * This Class represents the Applications states e.g. which
 * Map Style is currently applied and if the app runs in Offline
 * mode.
 */
public class H1keApplication extends android.app.Application {

    /**
     * Path to the Map resources which are needed by Skobblers Map Rendering engine
     */
    private String resourcePath;
    /**
    * Packages obtained from Map.XML
    */
    private Map<String, MapPack> mapPackages;

    /**
     * Path for the MapCreator
     */
    private String mapCreatorPath;

    /**
     * This String Array holds the different Map Rendering Styles.
     */
    private static final String mapStylesArray[] = {"daystyle", "nightstyle", "outdoorstyle", "grayscalestyle"};

    /**
     * This variable is the indicator which Map Style is currently chosen.
     */
    private int chosenMapStyle;

    /**
     * This variable holds the Offline- / Online maps State.
     */
    private boolean mOfflineMaps;

    /**
     * Here is where the Annotations ( or Pinns ) are saved.
     */
    private TreeMap<Integer, SKAnnotation> annotationList;

    /**
     *  This variable holds the compass State.
     */
    private boolean compassToggle;

    private boolean mNavigationInProgress;


    public boolean isCompassToggle() {
        return compassToggle;
    }

    public void setCompassToggle(boolean compassToggle) {
        this.compassToggle = compassToggle;
    }

    public H1keApplication () {
        annotationList = new TreeMap<Integer, SKAnnotation>();
    }

    public TreeMap<Integer, SKAnnotation> getAnnotationList() {
        return annotationList;
    }

    public void setAnnotationList(TreeMap<Integer, SKAnnotation> annotationList) {
        this.annotationList = annotationList;
    }

    public int getChosenMapStyle() {
        return chosenMapStyle;
    }

    public void setChosenMapStyle(int mapStyle) {
        this.chosenMapStyle = mapStyle;
    }

    public String getMapCreatorPath() {
        return mapCreatorPath;
    }

    public void setMapCreatorPath(String mapCreatorPath) {
        this.mapCreatorPath = mapCreatorPath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String mMapAssetsDirectory) {
        this.resourcePath = mMapAssetsDirectory;
    }

    public Map<String, MapPack> getMapPackages() {
        return mapPackages;
    }

    public void setMapPackages(Map<String, MapPack> mapPackages) {
        this.mapPackages = mapPackages;
    }

    public String[] getMapStylesArray() {
        return mapStylesArray;
    }

    public  boolean ismOfflineMaps() {
        return mOfflineMaps;
    }

    public  void setmOfflineMaps(boolean mOfflineMaps) {
        this.mOfflineMaps = mOfflineMaps;
    }

    public boolean ismNavigationInProgress() {
        return mNavigationInProgress;
    }

    public void setmNavigationInProgress(boolean mNavigationInProgress) {
        this.mNavigationInProgress = mNavigationInProgress;
    }

    /**
     * This method saves some AppStates for use while the Application is
     * Closing. This is done with Gson.
     */
    public  void saveCurrentAppState () {

        AppState appState = new AppState(mOfflineMaps, chosenMapStyle);

        Gson gson = new Gson();

        String jsonStr = gson.toJson(appState);

        try {
            if( mapCreatorPath != null && !mapCreatorPath.isEmpty()) {
                FileWriter fileWriter = new FileWriter(resourcePath + "appState.json");
                fileWriter.write(jsonStr);
                fileWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * This method retrieves the previous App State.
     * E.g. was the App in Offline or in Online mode and witch Map Style was chosen ?
     *
     */
    public void retrievePreviousAppState() {
        Gson gson = new Gson();

        try {
            FileReader fileReader = new FileReader(resourcePath + "appState.json");

            BufferedReader buffered = new BufferedReader(fileReader);

            AppState appState = gson.fromJson(buffered, AppState.class);

            mOfflineMaps = appState.isOfflineMapsPreferred();
            chosenMapStyle = appState.getMapStyle();

        } catch ( IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  The class AppState holds the App's condition. The AppState Object is
     *  later written and read from the Device's memory.
     */
    private class AppState {

        boolean offlineMapsPreferred;
        int mapStyle;

        /**
         * Constructs an AppState object wich is written to memory.
         * @param connectionType = Off- / Online connection.
         * @param style = MapStyle that is currently chosen.
         */
        public AppState (boolean connectionType, int style) {
            offlineMapsPreferred = connectionType;
            mapStyle = style;
        }

        public boolean isOfflineMapsPreferred() {
            return offlineMapsPreferred;
        }

        public void setOfflineMapsPreferred(boolean offlineMapsPreferred) {
            this.offlineMapsPreferred = offlineMapsPreferred;
        }

        public int getMapStyle() {
            return mapStyle;
        }

        public void setMapStyle(int mapStyle) {
            this.mapStyle = mapStyle;
        }
    }
}
