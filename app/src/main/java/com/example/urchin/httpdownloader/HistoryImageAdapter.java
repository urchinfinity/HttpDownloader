package com.example.urchin.httpdownloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by urchin on 2016/7/13.
 */
public class HistoryImageAdapter extends BaseAdapter {
    public Context context;
    public int imageSize;

    public ArrayList<Bitmap> images = new ArrayList<Bitmap>();

    HistoryImageAdapter(Context c, int w, ArrayList<Bitmap> bmps) {
        context = c;
        imageSize = w;
        images = bmps;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageV;
        if (convertView == null) {
            imageV = new ImageView(context);
            imageV.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageV.setLayoutParams(new GridView.LayoutParams(imageSize, imageSize));
            imageV.setBackgroundColor(Color.CYAN);
        } else {
            imageV = (ImageView) convertView;
        }

        imageV.setImageBitmap(images.get(position));
        return imageV;
    }
}

