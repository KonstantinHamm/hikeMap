package org.hamm.h1kemaps.app.model;

/**
 * Created by Konstantin Hamm on 28.02.15.
 * The Classes and Activities in this Project were
 * developed with the Skobbler Maps SDK and its Support sites.
 * http://developer.skobbler.de/docs/android/2.4.0/index.html
 * and
 * http://developer.skobbler.de/getting-started/android#sec000_
 */

import java.util.ArrayList;
import java.util.List;

/**
 * This Class represents a MapPack wich can be downloaded by the user.
 */
public class MapPack {

    /**
     * Package code (e.g. RO - for Romania)
     */
    private String code;

    /**
     * Code of the parent package (e.g. EUR)
     */
    private String parentCode;

    /**
     * Package name (e.g. Romania, Bucharest, ...)
     */
    private String name;

    /**
     * The type of the package (continent, country, state, region, city)
     */
    private String type;

    /**
     * Size of the SKM file in the package
     */
    private long size;

    /**
     * Codes of the children pakages (e.g. ROCITY01)
     */
    private List<String> childrenCodes = new ArrayList<String>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<String> getChildrenCodes() {
        return childrenCodes;
    }

    public void setChildrenCodes(List<String> childrenCodes) {
        this.childrenCodes = childrenCodes;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    @Override
    public String toString() {
        return name;
    }
}
