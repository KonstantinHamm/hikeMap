package org.hamm.h1kemaps.app.util;

/**
 * Created by Konstantin Hamm on 22.03.2015.
 * The Classes and Activities in this Project were
 * developed with the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 */

import android.widget.BaseAdapter;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import org.hamm.h1kemaps.app.R;
import org.hamm.h1kemaps.app.model.NavigationDrawerItem;

import java.util.ArrayList;

/**
 * The Adapter for the Navigation Drawer List
 */
public class NavigationDrawerListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<NavigationDrawerItem> navDrawerItems;

    public NavigationDrawerListAdapter(Context context, ArrayList<NavigationDrawerItem> navDrawerItems){
        this.context = context;
        this.navDrawerItems = navDrawerItems;
    }

    @Override
    public int getCount() {
        return navDrawerItems.size();
    }

    @Override
    public Object getItem(int position) {
        return navDrawerItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.icon);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        TextView txtCount = (TextView) convertView.findViewById(R.id.counter);

        imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
        txtTitle.setText(navDrawerItems.get(position).getTitle());

        // displaying count
        // check whether it set visible or not
        if(navDrawerItems.get(position).getCounterVisibility()){
            txtCount.setText(navDrawerItems.get(position).getCount());
        } else {
            // hide the counter view
            txtCount.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return super.areAllItemsEnabled();
    }
}
