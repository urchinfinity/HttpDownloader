package com.example.urchin.httpdownloader;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Random;

/**
 * Created by urchin on 2016/7/15.
 */
public class UboController extends Thread  {

    public AppCompatActivity activity;

    private ImageView uboImages[];
    private int positions[][];

    private int boundLeft;
    private int boundRight;
    private int boundTop;
    private int boundBottom;

    private int gravityX = 0;
    private int gravityY = 0;
    private int duration = 0;

    UboController(AppCompatActivity a) {
        activity = a;
        uboImages = new ImageView[10];
        positions = new int[10][3];

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        boundLeft = -100;
        boundTop = -100;
        boundRight = displayMetrics.widthPixels + 100;
        boundBottom =  displayMetrics.heightPixels + 100;

        getViews();
        setInitialPositions();
    }

    public void run() {
        try {
            Thread.sleep(10);

            if (duration == 0) {
                gravityX = new Random().nextInt(18) - 9;
                gravityY = new Random().nextInt(18) - 9;
                duration = (new Random().nextInt(9) + 1) * 100;
            }

            for (int i = 0; i < uboImages.length; i++) {
                positions[i][0] += gravityX;
                positions[i][1] += gravityY;
                positions[i][2] += new Random().nextInt(30) - 10;

                positions[i][0] = positions[i][0] < boundLeft ? boundLeft : positions[i][0];
                positions[i][0] = positions[i][0] > boundRight ? boundRight : positions[i][0];
                positions[i][1] = positions[i][1] < boundTop ? boundTop : positions[i][1];
                positions[i][1] = positions[i][1] > boundBottom ? boundBottom : positions[i][1];
                positions[i][2] %= 360;

                setPosition(i);

                duration -= 100;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Toast.makeText(activity.getApplicationContext(), "x = " + event.values[0] + ", y = " + event.values[1] + ", z = " + event.values[2], Toast.LENGTH_SHORT).show();
    }

    private void getViews() {
        uboImages[0] = (ImageView)activity.findViewById(R.id.faller1);
        uboImages[1] = (ImageView)activity.findViewById(R.id.faller2);
        uboImages[2] = (ImageView)activity.findViewById(R.id.faller3);
        uboImages[3] = (ImageView)activity.findViewById(R.id.faller4);
        uboImages[4] = (ImageView)activity.findViewById(R.id.faller5);
        uboImages[5] = (ImageView)activity.findViewById(R.id.faller6);
        uboImages[6] = (ImageView)activity.findViewById(R.id.faller7);
        uboImages[7] = (ImageView)activity.findViewById(R.id.faller8);
        uboImages[8] = (ImageView)activity.findViewById(R.id.faller9);
        uboImages[9] = (ImageView)activity.findViewById(R.id.faller10);
    }

    private void setInitialPositions() {
        for (int i = 0; i < uboImages.length; i++) {
            positions[i][0] = new Random().nextInt(boundRight - boundLeft) + boundLeft;
            positions[i][1] = boundBottom / 2;
            positions[i][2] = new Random().nextInt(360);

            setPosition(i);
        }
    }

    private void setPosition(int index) {
        RelativeLayout.LayoutParams l = (RelativeLayout.LayoutParams)(uboImages[index].getLayoutParams());
        l.setMargins(positions[index][0], positions[index][1], 0, 0);

        uboImages[index].setLayoutParams(l);
        uboImages[index].setPivotX(20);
        uboImages[index].setPivotY(20);
        uboImages[index].setRotation(positions[index][2]);
    }
}
