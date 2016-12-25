package com.example.urchin.httpdownloader;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by urchinwang on 7/13/16.
 */
public class BoundedViewPager extends ViewPager {

    public BoundedViewPager(Context context) {
        super(context);
    }

    public BoundedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (((GalleryPagerAdapter)getAdapter()).curScaleFactor == 1.f)
            return super.onInterceptTouchEvent(event);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (((GalleryPagerAdapter)getAdapter()).curScaleFactor == 1.f)
            return super.onTouchEvent(event);
        return false;
    }
}