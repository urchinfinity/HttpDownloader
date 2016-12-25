package com.example.urchin.httpdownloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by urchin on 2016/7/6.
 */
public class ImageAdapter extends BaseAdapter {
    public Context context;
    public int imageSize;

    public ArrayList<Bitmap> images = new ArrayList<Bitmap>();

    ImageAdapter(Context c, int w) {
        context = c;
        imageSize = w;
    }

    void addImage(Bitmap bmp) {
        images.add(bmp);
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
            imageV.setBackgroundResource(R.drawable.file);
        } else {
            imageV = (ImageView) convertView;
        }

        imageV.setImageBitmap(images.get(position));

        return imageV;
    }

    public Bitmap getBitmapFromFilepath(String path) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opt);

        opt.inJustDecodeBounds = false;
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inDither = true;
        opt.inSampleSize = calculateInSampleSize(opt);

        return BitmapFactory.decodeFile(path, opt);
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        int inSampleSize = 1;

        if (imageWidth > imageSize || imageHeight > imageSize) {
            int halfWidth = imageWidth / 2;
            int halfHeight = imageHeight / 2;

            while ((halfWidth / inSampleSize) >= imageSize && (halfHeight / inSampleSize) >= imageSize)
                inSampleSize *= 2;
        }
        return inSampleSize;
    }
}
