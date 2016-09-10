package org.hamm.h1kemaps.app.view;

/**
 * Created by Konstantin Hamm on 25.02.2015.
 * The Classes and Activities in this Project were
 * developed with the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 */

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.positioner.SKCurrentPositionListener;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import org.hamm.h1kemaps.app.R;
import org.hamm.h1kemaps.app.application.H1keApplication;
import org.hamm.h1kemaps.app.util.Utils;

/**
 *  This Activity prepares the Device for the Skobbler rendering engine.
 */
public class AppStartActivity extends Activity implements SKPrepareMapTextureListener, SKMapUpdateListener, SKCurrentPositionListener {

    /**
     * Path to the MapResources directory
     */
    private String mMapResourcesDirPath = "";
    private H1keApplication mApp;

    /**
     * Creates activity and prepares app for usage.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_start);

        prepareDeviceForApp();
    }

    /**
     * Gets called if the map textures are ready for display.
     * @param prepared = true or false
     */
    @Override
    public void onMapTexturesPrepared(boolean prepared) {

        String[] styleArray = mApp.getMapStylesArray();
        String style = styleArray[mApp.getChosenMapStyle()];

        Utils.initAppLibrary( this, mApp.ismOfflineMaps(), style);

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(AppStartActivity.this, "Map resources were copied", Toast.LENGTH_SHORT).show();


                finish();
                startActivity(new Intent(AppStartActivity.this, MapActivity.class));
            }
        });
    }

    /**
     * This method is for preparing the device for running Skobbler Maps.
     * Several things happen here. A Directory is created for the map Resources
     * and some resources that are needed by the SDK like bitmaps etc. are copied
     * to a central place on the Android device.
     * The prepareDevice method has been created with the Help of the Skobbler Maps Demo
     * from this site: http://developer.skobbler.de/getting-started/android
     * The code from the Skobbler support sites is Open Source therefore
     * this code is Open Source also.
     */
    private void prepareDeviceForApp() {
        // Create a H1keApplication object so we can store the overall app state.
        mApp = (H1keApplication) getApplication();

        // Set SKLogging on
        SKLogging.enableLogs(true);

        File externalDir = getExternalFilesDir(null);

        // determine path where map resources should be copied on the device
        if (externalDir != null) {
            mMapResourcesDirPath = externalDir + "/" + "SKMaps/";
        } else {
            mMapResourcesDirPath = getFilesDir() + "/" + "SKMaps/";
        }

        mApp.setResourcePath(mMapResourcesDirPath);


        if (!new File(mMapResourcesDirPath).exists()) {

            // if map resources are not already present copy them to
            // mMapResourcesDirPath in the following thread
            new SKPrepareMapTextureThread(this, mMapResourcesDirPath, "SKMaps.zip", this).start();

            // copy some other resources needed
            copyOtherResources();
            prepareMapCreatorFile();
        } else {
            // map resources have already been copied - start the map activity
            Toast.makeText(AppStartActivity.this, "Map resources copied in a previous run", Toast.LENGTH_SHORT).show();
            prepareMapCreatorFile();

            mApp.retrievePreviousAppState();

            // Getting the previous chosen map Style
            String[] styleArray = mApp.getMapStylesArray();
            String style = styleArray[mApp.getChosenMapStyle()];

            // Initialize the App library with offline maps on or off and setting the Map Style.
            //
            Utils.initAppLibrary(this, mApp.ismOfflineMaps(), style);

            finish();

            // Start the Main Map activity.
            startActivity(new Intent(this, MapActivity.class));
        }
    }

    /**
     * Copy some additional resources from assets which are needed for the Rendering Engine.
     * The copyOtherResources Method has been created with the Help of the Skobbler Maps Demo
     * from this site: http://developer.skobbler.de/getting-started/android
     */
    private void copyOtherResources() {
        new Thread() {

            public void run() {
                try {
                    String tracksPath = mMapResourcesDirPath + "GPXTracks";
                    File tracksDir = new File(tracksPath);
                    if (!tracksDir.exists()) {
                        tracksDir.mkdirs();
                    }
                    Utils.copyAssetsToFolder(getAssets(), "GPXTracks", mMapResourcesDirPath + "GPXTracks");

                    String imagesPath = mMapResourcesDirPath + "images";
                    File imagesDir = new File(imagesPath);
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }
                    Utils.copyAssetsToFolder(getAssets(), "images", mMapResourcesDirPath + "images");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        }.start();
    }

    /**
     * Copies the map creator file from assets to a storage. The map creator is used for configuring
     * the map’s basic settings (internationalization, showing bicycle lanes, one way arrows, etc.)
     * through a configuration JSON file. After the JSON file is generated and saved it should be
     * placed in the application's assets folder
     * The prepareMapCreatorFile Method has been created with the Help of the Skobbler Maps Example
     * Code from this site: http://developer.skobbler.de/getting-started/android
     */
    private void prepareMapCreatorFile() {
        final H1keApplication app = (H1keApplication) getApplication();
        final Thread prepareGPXFileThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    final String mapCreatorFolderPath = mMapResourcesDirPath + "MapCreator";
                    final File mapCreatorFolder = new File(mapCreatorFolderPath);
                    // create the folder where you want to copy the json file
                    if (!mapCreatorFolder.exists()) {
                        mapCreatorFolder.mkdirs();
                    }
                    app.setMapCreatorPath(mapCreatorFolderPath + "/mapcreatorFile.json");
                    Utils.copyAsset(getAssets(), "MapCreator", mapCreatorFolderPath, "mapcreatorFile.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        prepareGPXFileThread.start();
    }


    @Override
    public void onMapVersionSet(int newVersion) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNewVersionDetected(int newVersion) {
        // TODO Auto-generated method stub
        Log.e("","new version "+newVersion);
    }

    @Override
    public void onNoNewVersionDetected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onVersionFileDownloadTimeout() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCurrentPositionUpdate(SKPosition skPosition) {

    }
}
