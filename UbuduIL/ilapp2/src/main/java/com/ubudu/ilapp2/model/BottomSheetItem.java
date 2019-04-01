package com.ubudu.ilapp2.model;

/**
 * Created by mgasztold on 13/07/16.
 */
public class BottomSheetItem {

    private int mDrawableRes;

    private int mId;

    private String mTitle;

    public BottomSheetItem(int id, int drawable, String title) {
        mId = id;
        mDrawableRes = drawable;
        mTitle = title;
    }

    public int getDrawableResource() {
        return mDrawableRes;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getId(){
        return mId;
    }

    protected void setTitle(String title){
        mTitle = title;
    }

    protected void setId(int id){
        mId = id;
    }

    protected void setDrawableRes(int drawable){
        mDrawableRes = drawable;
    }

}