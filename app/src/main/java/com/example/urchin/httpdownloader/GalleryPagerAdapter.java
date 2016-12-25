package com.example.urchin.httpdownloader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by urchin on 2016/7/7.
 */
public class GalleryPagerAdapter extends PagerAdapter {
    static final int MAX_WIDTH = 4000;
    static final int MAX_HEIGHT = 4000;

    public Context context;
    public ArrayList<String> images = new ArrayList<String>();
    public LayoutInflater inflater;

    static final String BOUND_LEFT = "boundLeft";
    static final String BOUND_RIGHT = "boundRight";

    public ScaleGestureDetector scaleDetector;
    public float preScaleFactor = 1.f;
    public float curScaleFactor = 1.f;
    private float maxScalingRatio;
    private boolean isScaling = false;
    private float focusX = 0.f;
    private float focusY = 0.f;

    private float preX = 0.f;
    private float preY = 0.f;

    private final int screenWidth;
    private final int screenHeight;

    public GalleryPagerAdapter(Context c) {
        context = c;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        images.add(BOUND_LEFT);
        images.add(BOUND_RIGHT);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;
        maxScalingRatio = MAX_HEIGHT / (float)screenHeight;
    }

    public void addImage(String path) {
        images.add(images.size() - 1, path);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //resize container for further scaling implementation
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(MAX_WIDTH, MAX_HEIGHT);
        container.setLayoutParams(fl);

        //create fragment
        View fragment = inflater.inflate(R.layout.fragment_gallery, null, false);
        final ImageView image = (ImageView)fragment.findViewById(R.id.image);
        final TextView filename = (TextView)fragment.findViewById(R.id.filename);

        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
        image.setLayoutParams(rl);

        if (position == 0) {
            image.setImageResource(R.drawable.boundleft);
        } else if (position == images.size() - 1) {
            image.setImageResource(R.drawable.boundright);
        } else {
            //set image bitmap
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(images.get(position), opt);

            opt.inJustDecodeBounds = false;
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inDither = true;
            opt.inSampleSize = calculateInSampleSize(opt);

            final Bitmap bmp = BitmapFactory.decodeFile(images.get(position), opt);

            image.setImageBitmap(bmp);
            filename.setText(images.get(position).substring(images.get(position).lastIndexOf('/') + 1));

            //add scaling listener
            scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    if (isScaling) {
                        curScaleFactor *= detector.getScaleFactor();
                        curScaleFactor = curScaleFactor < 1 ? 1 : curScaleFactor;
                        curScaleFactor = curScaleFactor > maxScalingRatio ? maxScalingRatio : curScaleFactor;
                    }

                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    isScaling = true;
                    focusX = detector.getFocusX() / curScaleFactor;
                    focusY = detector.getFocusY() / curScaleFactor;

                    return true;
                }
            });

            image.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        preX = event.getRawX();
                        preY = event.getRawY();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (isScaling)
                            isScaling = false;
                        else if (preScaleFactor == 1.f) {
                            if (filename.getVisibility() == View.GONE)
                                filename.setVisibility(View.VISIBLE);
                            else
                                filename.setVisibility(View.GONE);
                        }
                    }

                    if (!scaleDetector.onTouchEvent(event))
                        return true;

                    if (event.getAction() == MotionEvent.ACTION_MOVE && !isScaling) {
                        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams)(image.getLayoutParams());
                        int leftMargin = (int)(l.leftMargin - (preX - event.getRawX()));
                        int topMargin = (int)(l.topMargin - (preY - event.getRawY()));

                        leftMargin = leftMargin > 0 ? 0 : leftMargin;
                        leftMargin = leftMargin < screenWidth * (1 - preScaleFactor) ? (int)(screenWidth * (1 - preScaleFactor)) : leftMargin;

                        topMargin = topMargin > 0 ? 0 : topMargin;
                        topMargin = topMargin < screenHeight * (1 - preScaleFactor) ? (int)(screenHeight * (1 - preScaleFactor)) : topMargin;

                        l.setMargins(leftMargin, topMargin, 0, 0);
                        image.setLayoutParams(l);
                        image.setImageBitmap(bmp);

                        preX = event.getRawX();
                        preY = event.getRawY();

                    } else if (preScaleFactor != curScaleFactor) {
                        if (filename.getVisibility() == View.VISIBLE)
                            filename.setVisibility(View.GONE);

                        RelativeLayout.LayoutParams l;
                        if (curScaleFactor == 1.f) {
                            l = new RelativeLayout.LayoutParams(screenWidth, screenHeight);
                            l.setMargins(0, 0, 0, 0);
                        } else {
                            l = new RelativeLayout.LayoutParams((int) (screenWidth * curScaleFactor), (int) (screenHeight * curScaleFactor));
                            l.setMargins(-(int) (focusX * (curScaleFactor - 1)), -(int) (focusY * (curScaleFactor - 1)), 0, 0);
                        }

                        image.setLayoutParams(l);
                        image.setImageBitmap(bmp);
                        preScaleFactor = curScaleFactor;

                    }
                    return true;
                }
            });
        }

        container.addView(fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout)object);
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        int inSampleSize = 1;

        if (imageWidth > screenWidth || imageHeight > screenHeight) {
            int halfWidth = imageWidth / 2;
            int halfHeight = imageHeight / 2;

            while ((halfWidth / inSampleSize) >= screenWidth && (halfHeight / inSampleSize) >= screenHeight)
                inSampleSize *= 2;
        }
        return inSampleSize;
    }
}