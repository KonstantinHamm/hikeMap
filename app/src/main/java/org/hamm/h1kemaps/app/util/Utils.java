package org.hamm.h1kemaps.app.util;

/**
 * Created by Konstantin Hamm on 25.02.2015.
 * The Classes and Activities in this Project were
 * developed with the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 */

import android.content.Context;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.common.io.ByteStreams;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import org.hamm.h1kemaps.app.application.H1keApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * A Utility class witch manages some very handy methods for initialisation of this app,
 * checking if the device has Gps and internet access etc.
 */
public class Utils {

    private static final String API_KEY = "ee77aa570c17ba124d1d36c2c62c50b44ecbc826589a6e58ced50f12218f4171";
    /**
     * This method checks if the Gps Module is available on the current Device.
     * @param con = current Context
     * @return True if the GPS is available False if not.
     */
    public static boolean hasGps( final Context con ) {

        // Get the Gps Service over the current context.
        final LocationManager locationManager = ( LocationManager ) con.getSystemService(Context.LOCATION_SERVICE);

        // Iterate over all available providers.
        for( final String provider : locationManager.getAllProviders() ) {

            // Return true if GPS Provider was found
            if ( provider.equals(LocationManager.GPS_PROVIDER )) {
                return true;
            }
        }
        return false;
    }

    /**
     * This Method checks if the Device has access to the internet
     * @param con = current Context.
     * @return True if Internet is accessible and False if not.
     */
    public static boolean hasInternet( Context con ) {
        // As in the hasGps method the get the system service 'Connectivity Service' over the current context.
        ConnectivityManager conMan = ( ConnectivityManager ) con.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details of current active data network
        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();

        if( networkInfo != null ) {
            // check whether the connection type is wifi or mobile
            if( networkInfo.getType() == ConnectivityManager.TYPE_WIFI ) {

                if( networkInfo.isConnected() ) {
                    return true;
                }

                if( networkInfo.getType() == ConnectivityManager.TYPE_MOBILE ) {

                    if( networkInfo.isConnected() ) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method uses Androids Asset Manager and Copy's the file to destination folder via java.io.
     * @param assetManager androids assetManager.
     * @param destinationFolder Destination folder.
     * @param assetsNames Name of the asset.
     */
    public static void copyAsset(AssetManager assetManager, String sourceFolder, String destinationFolder,
                                 String... assetsNames) throws IOException {

        for (String assetName : assetsNames) {
            OutputStream destinationStream = new FileOutputStream(new File(destinationFolder + "/" + assetName));
            String[] files = assetManager.list( sourceFolder + "/" + assetName);

            String temp = sourceFolder + "/" + assetName;

            if (files == null || files.length == 0) {

                InputStream asset = assetManager.open(temp);

                try {
                    ByteStreams.copy(asset, destinationStream);
                } finally {
                    asset.close();
                    destinationStream.close();
                }
            }
        }
    }

    /**
     * Copies files from assets to destination folder.
     * @param assetManager Asset Manager copys all assets which are needed.
     * @param sourceFolder The folder where the assests come from.
     * @param destinationFolder The folder where the assests will go.
     * @throws IOException
     */
    public static void copyAssetsToFolder(AssetManager assetManager, String sourceFolder, String destinationFolder)
            throws IOException {
        final String[] assets = assetManager.list(sourceFolder);

        final File destFolderFile = new File(destinationFolder);

        if (!destFolderFile.exists()) {
            destFolderFile.mkdirs();
        }
        copyAsset(assetManager, sourceFolder, destinationFolder, assets);
    }

    /**
     * Initializes the SKMaps framework
     * @param context = context.
     * @param offlineMode = initializes the skmaps sdk in offline mode.
     * @param mapStyle = initilizes skmaps sdk with specified map style.
     */
    public static void initAppLibrary(final Context context, boolean offlineMode, String mapStyle) {
        final H1keApplication app = (H1keApplication)context.getApplicationContext();
        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();
        // set path to map resources and initial map style

        initMapSettings.setMapResourcesPaths(app.getResourcePath(),
                new SKMapViewStyle(app.getResourcePath() + mapStyle +"/", mapStyle + ".json"));

        final SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setLanguage("en");
        advisorSettings.setAdvisorVoice("en");
        initMapSettings.setAdvisorSettings(advisorSettings);

        if(offlineMode) {
            initMapSettings.setPreinstalledMapsPath(app.getResourcePath()
                    + "/PreinstalledMaps");
            initMapSettings.setConnectivityMode(SKMaps.CONNECTIVITY_MODE_OFFLINE);

            initMapSettings.setMapDetailLevel(SKMapsInitSettings.SK_MAP_DETAIL_LIGHT);
        }

        SKMaps.getInstance().initializeSKMaps(context, initMapSettings, API_KEY);
    }

    /**
     * Initilizes the SKMaps Library in a default way. (Without changing to Offline mode or
     * another Map Style.)
     * For further information go here: http://developer.skobbler.de/docs/android/2.4.0/index.html
     * @param context The context.
     */
    public static void initAppLibrary(final Context context) {
        final H1keApplication app = (H1keApplication)context.getApplicationContext();

        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();

        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(app.getResourcePath(),
                new SKMapViewStyle(app.getResourcePath() + "daystyle/", "daystyle.json"));

        final SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setLanguage("en");
        advisorSettings.setAdvisorVoice("en");
        initMapSettings.setAdvisorSettings(advisorSettings);

        SKMaps.getInstance().initializeSKMaps(context, initMapSettings, API_KEY);
    }
}
