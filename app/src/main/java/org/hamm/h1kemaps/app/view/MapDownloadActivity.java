package org.hamm.h1kemaps.app.view;

/**
 * Created by Konstantin Hamm on 15.03.2015.
 * The Classes and Activities in this Project were
 * developed with the Help of the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 * The code from the Skobbler support sites is Open Source therefore
 * this code is Open Source also.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.skobbler.ngx.packages.SKPackageManager;
import com.skobbler.ngx.packages.SKPackageURLInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.hamm.h1kemaps.app.R;
import org.hamm.h1kemaps.app.application.H1keApplication;
import org.hamm.h1kemaps.app.model.MapPack;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MapDownloadActivity extends Activity {

    private static final int NO_BYTES_INTO_ONE_MB = 1048576;

    /**
     * Path at which download packages are temporarily stored
     */
    private static String packagesPath;

    private H1keApplication app;

    private ProgressBar progressBar;

    private Button startDownloadButton;

    private TextView downloadPercentage;

    /**
     * Selected map package to be downloaded
     */
    private MapPack dowloadPackage;

    /**
     * Index of the current download resource
     */
    private int downloadResourceIndex;

    /**
     * URLs to download resources
     */
    private List<String> downloadResourceUrls;

    /**
     * Download resources extensions
     */
    private List<String> downloadResourceExtensions;

    /**
     * Layout of Activity is initilized and paths for map packages are set in this method.
     * @param savedInstanceState = this is a bundle where things like the state of the app is saved
     *                             in the event of an interruption ( phone call, user pushing home
     *                             button etc.)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (H1keApplication) getApplication();
        setContentView(R.layout.activity_download);
        packagesPath = app.getResourcePath() + "/Maps/downloads/";
        progressBar = (ProgressBar) findViewById(R.id.download_progress_bar);
        startDownloadButton = (Button) findViewById(R.id.download_button);
        downloadPercentage = (TextView) findViewById(R.id.download_percentage_text);
        dowloadPackage = app.getMapPackages().get(getIntent().getStringExtra("packageCode"));
        startDownloadButton.setText(getResources().getString(R.string.label_download) + " " + dowloadPackage.getName());
        prepareDownloadResources();
    }

    /**
     * Prepares a list of download resources for the selected package to be
     * downloaded
     */
    private void prepareDownloadResources() {
        downloadResourceUrls = new ArrayList<String>();
        downloadResourceExtensions = new ArrayList<String>();
        downloadResourceIndex = 0;


        // the resources to be downloaded for the selected package will be:
        // - the .skm file (the map)
        // - the textures file (.txg)
        // - the name-browser files (.ngi, .ngi.dat) necessary for offline
        // searches

        SKPackageURLInfo info = SKPackageManager.getInstance().getURLInfoForPackageWithCode(dowloadPackage.getCode());
        String mapURL = info.getMapURL();
        String texturesURL = info.getTexturesURL();
        String nbFilesZipUrl = info.getNameBrowserFilesURL();

        downloadResourceUrls.add(mapURL);
        downloadResourceExtensions.add(".skm");

        downloadResourceUrls.add(texturesURL);
        downloadResourceExtensions.add(".txg");

        downloadResourceUrls.add(nbFilesZipUrl.replaceFirst("\\.zip", ".ngi"));
        downloadResourceExtensions.add(".ngi");

        downloadResourceUrls.add(nbFilesZipUrl.replaceFirst("\\.zip", ".ngi.dat"));
        downloadResourceExtensions.add(".ngi.dat");
    }

    /**
     * Starts the Download of a Map Pack.
     * @param v = event origin
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.download_button:
                startDownloadButton.setEnabled(false);
                downloadResource(downloadResourceUrls.get(0), downloadResourceExtensions.get(0));
                Toast.makeText(MapDownloadActivity.this.getApplicationContext(),
                        R.string.download_in_background, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    /**
     * Downloads a resource from the server.
     * this Method has been created with the Help of the Skobbler Maps Demo
     * from this site: http://developer.skobbler.de/getting-started/android
     * The code from the Skobbler support sites is Open Source therefore
     * this code is Open Source also.
     * @param url URL to the download resource
     * @param extension extension of the resource
     */
    private void downloadResource(final String url, final String extension) {
        // thread download a remote resource
        Thread downloadThread = new Thread() {

            private long lastProgressUpdateTime = System.currentTimeMillis();

            @Override
            public void run() {
                super.run();
                // get the request used to download the resource at the URL
                HttpGet request = new HttpGet(url);
                DefaultHttpClient httpClient = new DefaultHttpClient();
                try {
                    // execute the request
                    HttpResponse response = httpClient.execute(request);
                    InputStream responseStream = response.getEntity().getContent();
                    if (!new File(packagesPath).exists()) {
                        new File(packagesPath).mkdirs();
                    }
                    // local file at temporary path where the resource is
                    // downloaded
                    RandomAccessFile localFile =
                            new RandomAccessFile(packagesPath + dowloadPackage.getCode() + extension, "rw");

                    // download the resource to the temporary path
                    long bytesRead = localFile.length();
                    localFile.seek(bytesRead);
                    byte[] data = new byte[NO_BYTES_INTO_ONE_MB];

                    while (true) {
                        final int actual = responseStream != null ? responseStream.read(data, 0, data.length) : 0;
                        if (actual > 0) {

                            bytesRead += actual;
                            localFile.write(data, 0, actual);

                            if (downloadResourceExtensions.get(downloadResourceIndex).equals(".skm")) {
                                // notify the UI about progress (in case of the
                                // SKM download resource)
                                long currentTime = System.currentTimeMillis();
                                if (currentTime - lastProgressUpdateTime > 100) {

                                    updateDownloadProgress(bytesRead, dowloadPackage.getSize());
                                    lastProgressUpdateTime = currentTime;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    localFile.close();

                    // notify that the download was finished
                    updateOnFinishDownload();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        // start downloading in the thread
        downloadThread.start();
    }

    /**
     * Update the progress bar to show the progress of the SKM resource download
     * @param downloadedSize size downloaded so far
     * @param totalSize total size of the resource
     */
    private void updateDownloadProgress(final long downloadedSize, final long totalSize) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                int progress = (int) (progressBar.getMax() * ((float) downloadedSize / totalSize));
                progressBar.setProgress(progress);
                downloadPercentage.setText(((float) progress / 10) + "%");
        }
        });
    }

    /**
     * Update when a resource download was completed
     */
    private void updateOnFinishDownload() {



        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progressBar.setProgress(progressBar.getMax());
                if (downloadResourceExtensions.get(downloadResourceIndex).equals(".skm")) {
                    downloadPercentage.setText("100%");
                }
                if (downloadResourceIndex >= downloadResourceUrls.size() - 1) {
                    // if the download of the last resource was completed -
                    // install the package
                    SKPackageManager.getInstance().addOfflinePackage(packagesPath, dowloadPackage.getCode());
                    // at this point the downloaded package should be available
                    // offline
                    Toast.makeText(MapDownloadActivity.this.getApplicationContext(),
                            "Map of " + dowloadPackage + " is now available offline", Toast.LENGTH_SHORT).show();
                } else {
                    // start downloading the next queued resource from the
                    // package
                    downloadResourceIndex++;
                    downloadResource(downloadResourceUrls.get(downloadResourceIndex),
                            downloadResourceExtensions.get(downloadResourceIndex));
                }
            }
        });
    }
}
