package com.example.urchin.httpdownloader;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class HistoryActivity extends AppCompatActivity {
    public GridView recentImages;
    public HistoryImageAdapter gridImageAdapter;

    public int imageSize;
    public ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();
    public int loadedImageNum = 0;
    public int totalImageNum = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UIMessage.LOAD_IMAGE_FINISHED) {
                loadedImageNum++;
            }
            if (loadedImageNum == totalImageNum) {
                initHistoryGridView();
                startImageOnClickListener();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

//        startUboAnimation();
        loadImages();
    }

    public void startUboAnimation() {
        new UboController(this).start();
    }

    public void loadImages() {
        //calculate appropriate image width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenPadding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

        //api lower than 15 does not support, use user-defined values
        int gridViewHorizontalSpacing = 5;
        int gridViewColumnNum = 3;

        imageSize = (screenWidth - (screenPadding + gridViewHorizontalSpacing) * 2) / gridViewColumnNum;

        //load images from Download/
        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/");
        if (!folder.exists()) {
            //save file in application folder
            folder = new File(getApplicationContext().getFilesDir().toString() + "/Downloads/");
        }
        File files[] = folder.listFiles();

        //check files type
        String imageTypes[] = {".jpg", ".jpeg", ".png", ".bmp"};
        ArrayList<File> images = new ArrayList<File>();
        for (File file : files) {
            for (String type : imageTypes) {
                if (file.getPath().toLowerCase().endsWith(type)) {
                    images.add(file);
                    totalImageNum++;
                    break;
                }
            }
        }

        for (int i = 0; i < totalImageNum; i++) {
            bmps.add(null);
            new LoadTask(i, images.get(i).getPath()).start();
        }
    }

    public void initHistoryGridView() {
        //create adapter
        recentImages = (GridView)findViewById(R.id.images);
        gridImageAdapter = new HistoryImageAdapter(getApplicationContext(), imageSize, bmps);

        recentImages.setAdapter(gridImageAdapter);
    }

    public void startImageOnClickListener() {
        recentImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //jump to whole image view
                Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);

                //add image ID for viewPager
                Bundle b = new Bundle();
                b.putInt("imageID", position);
                intent.putExtras(b);

                startActivity(intent);
            }
        });
    }

    private class LoadTask extends Thread {
        public int id;
        public String filename;


        LoadTask(int position, String path) {
            id = position;
            filename = path;
        }

        public void run() {
            //get bitmap image
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filename, opt);

            opt.inJustDecodeBounds = false;
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inDither = true;
            opt.inSampleSize = calculateInSampleSize(opt);

            bmps.set(id, BitmapFactory.decodeFile(filename, opt));

            //send complete message
            Message msg = Message.obtain();
            msg.what = UIMessage.LOAD_IMAGE_FINISHED;
            handler.sendMessage(msg);

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
}
