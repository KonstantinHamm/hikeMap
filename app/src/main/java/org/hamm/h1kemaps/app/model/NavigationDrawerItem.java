package org.hamm.h1kemaps.app.model;

/**
 * Created by Konstantin Hamm on 22.03.2015.
 * The Classes and Activities in this Project were
 * developed with the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and :
 * http://developer.skobbler.de/getting-started/android#sec000_
 */

/**
 * This class represents a Navigation Drawer Item.
 */
public class NavigationDrawerItem  {

    private String title;
    private int icon;
    private String count = "0";
    // boolean to set visiblity of the counter
    private boolean isCounterVisible = false;

    public NavigationDrawerItem(){}

    public NavigationDrawerItem(String title, int icon){
        this.title = title;
        this.icon = icon;
    }

    public NavigationDrawerItem(String title, int icon, boolean isCounterVisible, String count){
        this.title = title;
        this.icon = icon;
        this.isCounterVisible = isCounterVisible;
        this.count = count;
    }

    public String getTitle(){
        return this.title;
    }

    public int getIcon(){
        return this.icon;
    }

    public String getCount(){
        return this.count;
    }

    public boolean getCounterVisibility(){
        return this.isCounterVisible;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setIcon(int icon){
        this.icon = icon;
    }

    public void setCount(String count){
        this.count = count;
    }

    public void setCounterVisibility(boolean isCounterVisible){
        this.isCounterVisible = isCounterVisible;
    }

}
