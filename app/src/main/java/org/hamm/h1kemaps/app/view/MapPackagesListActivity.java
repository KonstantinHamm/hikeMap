package org.hamm.h1kemaps.app.view;

/**
 * Created by Konstantin Hamm on 15.03.2015.
 * The Classes and Activities in this Project were
 * developed with help of the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 * The code from the Skobbler support sites is Open Source therefore
 * this code is Open Source also.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.skobbler.ngx.packages.SKPackageManager;

import org.hamm.h1kemaps.app.R;
import org.hamm.h1kemaps.app.application.H1keApplication;
import org.hamm.h1kemaps.app.model.MapPack;
import org.hamm.h1kemaps.app.util.MapDataParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Activity which displays map packages which are available
 * to download.
 */
public class MapPackagesListActivity extends Activity {
    
    private ListView listView;
    
    private H1keApplication app;
    
    /**
     * Packages currently shown in list
     */
    private List<MapPack> currentPackages;
    
    private MapPackageListAdapter adapter;

    /**
     * The onCreate Method checks first if the user has any map packages downloaded allready.
     * if not - a parser object is created to parse the Maps.xml file in an new thread.
     * After parsing the download packages are cached. After that the highest level
     * download packages(continents) are displayed.
     * @param savedInstanceState = bundle where to save necessary data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = (ListView) findViewById(R.id.list_view);
        app = (H1keApplication) getApplication();
        
        if (app.getMapPackages() != null) {
            // map packages are already available
            currentPackages = searchByParentCode(null);
            initializeList();
        } else {
            // map packages need to be obtained from parsing the Maps.xml file
            new Thread() {
                
                public void run() {
                    // get a parser object to parse the Maps.xml file
                    MapDataParser parser =
                            new MapDataParser(SKPackageManager.getInstance().getMapsXMLPathForCurrentVersion());
                    // do the parsing
                    parser.parse();
                    // after parsing Maps.xml cache the download packages
                    app.setMapPackages(parser.getPackMap());
                    // after parsing display the highest level download packages
                    currentPackages = searchByParentCode(null);
                    runOnUiThread(new Runnable() {
                        
                        @Override
                        public void run() {
                            initializeList();
                        }
                    });
                }
            }.start();
        }
    }
    
    /**
     * Populate list with current packages
     */
    private void initializeList() {
        findViewById(R.id.label_operation_in_progress).setVisibility(View.GONE);
        adapter = new MapPackageListAdapter();
        listView.setAdapter(adapter);
        listView.setVisibility(View.VISIBLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                List<MapPack> childPackages = searchByParentCode(currentPackages.get(position).getCode());
                if (childPackages.size() > 0) {
                    currentPackages = searchByParentCode(currentPackages.get(position).getCode());
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Common base class of common implementation for an Adapter that can be used in both ListView
     * (by implementing the specialized ListAdapter interface) and Spinner
     * (by implementing the specialized SpinnerAdapter interface).
     */
    private class MapPackageListAdapter extends BaseAdapter {
        
        @Override
        public int getCount() {
            return currentPackages.size();
        }
        
        @Override
        public Object getItem(int position) {
            return currentPackages.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Creates a View which allows the user to Download a map by starting a new
         * MapDownloadActivity
         *
         * @param position = The position of the item within the adapter's data set
         *                 of the item whose view we want.
         * @param convertView = The old view to reuse, if possible. Note: You should check that
         *                    this view is non-null and of an appropriate type before using. If it
         *                    is not possible to convert this view to display the correct data,
         *                    this method can create a new view. Heterogeneous lists can specify
         *                    their number of view types, so that this View is always of the right
         *                    type (see getViewTypeCount() and getItemViewType(int)).
         * @param parent = The parent that this view will eventually be attached to
         * @return
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_package_list_item, null);
            } else {
                view = convertView;
            }
            final MapPack currentPackage = currentPackages.get(position);
            Button downloadButton = (Button) view.findViewById(R.id.download_button);
            // countries and US states should be downloadable
            boolean downloadable =
                    (currentPackage.getType().equals("country") || currentPackage.getType().equals("state"))
                            && !currentPackage.getCode().equals("US");
            if (downloadable) {
                downloadButton.setVisibility(View.VISIBLE);
                view.findViewById(R.id.download_button).setOnClickListener(new View.OnClickListener() {
                    
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MapPackagesListActivity.this, MapDownloadActivity.class);
                        intent.putExtra("packageCode", currentPackage.getCode());
                        startActivity(intent);
                    }
                });
            } else {
                downloadButton.setVisibility(View.GONE);
            }
            TextView hasChildrenIndicator = (TextView) view.findViewById(R.id.indicator_children_available);
            if (currentPackage.getChildrenCodes().isEmpty()) {
                hasChildrenIndicator.setVisibility(View.INVISIBLE);
            } else {
                hasChildrenIndicator.setVisibility(View.VISIBLE);
            }
            ((TextView) view.findViewById(R.id.label_list_item)).setText(currentPackage.getName());
            return view;
        }
    }

    /**
     * If the Back button is Pressed. The Activity will show the higher level of the package hierarchy.
     */
    @Override
    public void onBackPressed() {
        boolean shouldClose = true;
        String grandparentCode = null;
        String parentCode = null;
        if(currentPackages == null){
            super.onBackPressed();
        }
        else if (!currentPackages.isEmpty()) {
            parentCode = currentPackages.get(0).getParentCode(); 
        }
        if (parentCode != null) {
            shouldClose = false;
            grandparentCode = app.getMapPackages().get(parentCode).getParentCode();
        }
        if (shouldClose) {
            super.onBackPressed();
        } else {
            // go one level higher in the map packages hierarchy
            currentPackages = searchByParentCode(grandparentCode);
            adapter.notifyDataSetChanged();
            MapPack parentPackage = app.getMapPackages().get(parentCode);
            listView.setSelection(currentPackages.indexOf(parentPackage));
        }
    }
    
    /**
     * Gets a list of download packages having the given parent code
     * @param parentCode code of the superior region. ( E.g DE for Germany)
     * @return List of MapPack's
     */
    private List<MapPack> searchByParentCode(String parentCode) {
        Collection<MapPack> packages = app.getMapPackages().values();
        List<MapPack> results = new ArrayList<MapPack>();
        for (MapPack pack : packages) {
            if (parentCode == null) {           
                if (pack.getParentCode() == null) {
                    results.add(pack);
                }
            } else if (parentCode.equals(pack.getParentCode())) {
                results.add(pack);
            }
        }
        return results;
    }
}
