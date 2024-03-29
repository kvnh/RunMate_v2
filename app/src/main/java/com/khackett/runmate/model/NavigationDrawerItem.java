package com.khackett.runmate.model;

/**
 * Created by KHackett on 21/08/15.
 * POJO class to define each row in the navigation drawer menu.
 */
public class NavigationDrawerItem {

    private boolean showNotify;
    private String title;

    public NavigationDrawerItem() {
    }

    public NavigationDrawerItem(boolean showNotify, String title) {
        this.showNotify = showNotify;
        this.title = title;
    }

    public boolean isShowNotify() {
        return showNotify;
    }

    public void setShowNotify(boolean showNotify) {
        this.showNotify = showNotify;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
