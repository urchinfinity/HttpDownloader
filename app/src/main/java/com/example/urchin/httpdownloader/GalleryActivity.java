package com.example.urchin.httpdownloader;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by urchin on 2016/7/7.
 */
public class GalleryActivity extends AppCompatActivity {

    private BoundedViewPager mPager;
    private GalleryPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        initScreen();
        showFullscreenImage(getSelectedImageID());
    }

    public void initScreen() {
        //set activity to fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public int getSelectedImageID() {
        //set current image to user selected image
        int id = 0;
        Bundle b = getIntent().getExtras();
        if (b != null)
            id = b.getInt("imageID");

        return id;
    }

    public void showFullscreenImage(int imageID) {
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (BoundedViewPager)findViewById(R.id.pager);
        mPagerAdapter = new GalleryPagerAdapter(getApplicationContext());

        //load images from Download/
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/");
        if (!folder.exists()) {
            //save file in application folder
            folder = new File(getApplicationContext().getFilesDir().toString() + "/Downloads/");
        }
        File ims[] = folder.listFiles();

        //check files type
        String imageTypes[] = {".jpg", ".jpeg", ".png", ".bmp"};
        for (File im : ims) {
            for (String type : imageTypes) {
                if (im.getPath().toLowerCase().endsWith(type)) {
                    mPagerAdapter.addImage(im.getPath());
                    break;
                }
            }
        }

        //set viewpager adapter
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(imageID + 1);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (mPager.getCurrentItem() == 0) {
                        try {
                            Thread.sleep(500);
                            mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else if (mPager.getCurrentItem() == mPagerAdapter.images.size() - 1) {
                        try {
                            Thread.sleep(500);
                            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}
