package com.example.urchin.httpdownloader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class DownloadActivity extends AppCompatActivity {
    static final int MAX_LENGTH = 15;

    public String directory;
    public int downloadingTasksLUT[][];
    public ArrayList<String> namePool = new  ArrayList<String>();
    public ImageAdapter gridImageAdapter;

    public ScreenStateBroadcastReceiver screenStateReceiver = new ScreenStateBroadcastReceiver();

    //background animation images
    public int backgroundsID[];

    //UI handler that receives messages from other threads and renders corresponding UI animations

    public void showDownloadingDialog() {
        //check if downloading layout is hidden
        if (findViewById(R.id.layout_download).getVisibility() == View.GONE)
            findViewById(R.id.layout_download).setVisibility(View.VISIBLE);
    }

    public void hideDownloadingDialog() {
        //check if no other task exists
        boolean noTask = true;
        for (int i = 0; i < TaskManager.TASK_NUM; i++)
            if (downloadingTasksLUT[i][TaskManager.IS_IDLE] == 0)
                noTask = false;

        //check if downloading layout is hidden
        if (noTask)
            if (findViewById(R.id.layout_download).getVisibility() == View.VISIBLE)
                findViewById(R.id.layout_download).setVisibility(View.GONE);
    }

    private Handler handler = new Handler() {
      @Override
        public void handleMessage(Message msg) {
          switch (msg.what) {
              case UIMessage.SHOW_DOWNLOADING_DIALOG:
                  showDownloadingDialog();
                  break;
              case UIMessage.HIDE_DOWNLOADING_DIALOG:
                  hideDownloadingDialog();
                  break;
              case UIMessage.CREATE_TASK:
                  //clone new task from template layout
                  LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
                  LinearLayout newTask = (LinearLayout)layoutInflater.inflate(R.layout.download_task, null);

                  int taskID = downloadingTasksLUT[msg.arg1][TaskManager.ID];

                  //set unique id to new task and add to parent layout
                  newTask.setId(taskID);
                  ((TextView)newTask.findViewById(R.id.text_filename)).setText((String)msg.obj);
                  ((LinearLayout)findViewById(R.id.task_container)).addView(newTask);

                  //add button onClick listener for pause, resume, and cancel operations
                  ImageView btnPauseResume = (ImageView)((LinearLayout)findViewById(taskID)).findViewById(R.id.btn_pause);
                  btnPauseResume.setTag(msg.arg1);
                  btnPauseResume.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          //update table and change icon
                          int id = (int)(v.getTag());

                          if (downloadingTasksLUT[id][TaskManager.IS_PAUSE] >= 1) {
                              ((ImageView)v).setImageResource(R.drawable.pause);
                              updateDownloadingTable(id, 0, 0, 0);
                              changeBackground(UIMessage.RESUME_TASK);
                          } else {
                              ((ImageView)v).setImageResource(R.drawable.resume);
                              updateDownloadingTable(id, 0, 1, 0);
                              changeBackground(UIMessage.PAUSE_TASK);
                          }
                      }
                  });

                  ImageView btnCancel = (ImageView)((LinearLayout)findViewById(taskID)).findViewById(R.id.btn_cancel);
                  btnCancel.setTag(msg.arg1);
                  btnCancel.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          //update table
                          int id = (int)(v.getTag());
                          updateDownloadingTable(id, 0, 0, 1);
                          changeBackground(UIMessage.CANCEL_TASK);

                          //delete task
                          LinearLayout targetTask = (LinearLayout)findViewById(downloadingTasksLUT[id][TaskManager.ID]);
                          if (targetTask != null)
                              ((LinearLayout)findViewById(R.id.task_container)).removeView(targetTask);

                          //hide download
                          hideDownloadingDialog();
                      }
                  });

                  //change background
                  changeBackground(UIMessage.CREATE_TASK);

                  //scroll view to bottom
                  ScrollView scroller = (ScrollView)findViewById(R.id.scroller);
                  scroller.scrollTo(0, scroller.getBottom());

                  break;
              case UIMessage.UPDATE_PROGRESSING_BAR:
                  //find corresponding progressBar and ipdate new value
                  LinearLayout task = (LinearLayout)findViewById(downloadingTasksLUT[msg.arg1][TaskManager.ID]);
                  if (task != null)
                      ((ProgressBar)((LinearLayout)findViewById(downloadingTasksLUT[msg.arg1][TaskManager.ID])).findViewById(R.id.progressBar)).setProgress(msg.arg2);
                  break;
              case UIMessage.DELETE_TASK:
                  LinearLayout targetTask = (LinearLayout)findViewById(downloadingTasksLUT[msg.arg1][TaskManager.ID]);
                  if (targetTask != null)
                      ((LinearLayout)findViewById(R.id.task_container)).removeView(targetTask);
                  break;
              case UIMessage.UPDATE_RECENT:
                  gridImageAdapter.notifyDataSetChanged();
                  changeBackground(UIMessage.UPDATE_RECENT);
                  break;
              case UIMessage.SCREEN_ON:
                  changeBackground(UIMessage.SCREEN_ON);
                  break;

              case UIMessage.CONNECTION_RECONNECT:
                  Toast.makeText(getApplicationContext(), "Connection timeout. Try reconnecting...", Toast.LENGTH_SHORT).show();
                  break;


              //show message
              case UIMessage.ERROR_INVALID_URL:
                  Toast.makeText(getApplicationContext(), "invalid URL", Toast.LENGTH_SHORT).show();
                  break;
              case UIMessage.ERROR_CONNECTION_FAILED:
                  Toast.makeText(getApplicationContext(), "Unable to connect to URL host" + msg.arg1, Toast.LENGTH_SHORT).show();
                  break;
              case UIMessage.ERROR_SAVE_FILE:
                  Toast.makeText(getApplicationContext(), "Unable to save image", Toast.LENGTH_SHORT).show();
                  break;
              case UIMessage.ERROR_DOWNLOAD_NOT_FOUND:
                  Toast.makeText(getApplicationContext(), "Download/ folder not found.\nSave file in temp folder.", Toast.LENGTH_LONG).show();
                  break;
              case UIMessage.ERROR_CONNECTION_TIMEOUT:
                  Toast.makeText(getApplicationContext(), "Connection timeout", Toast.LENGTH_SHORT).show();
                  break;
              case UIMessage.ERROR_THREAD_NOT_FOUND:
                  Toast.makeText(getApplicationContext(), "Downloader is too busy!\nTry downloading the image later.", Toast.LENGTH_SHORT).show();
                  break;
          }
      }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        initDownloadingTable();
        initRecentGridView();
        initBackgrounds();
        startDownloadBtnListener();
        startHistoryBtnListener();
        startScreenStateListener();
        startUboAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(screenStateReceiver);
    }

    public void initDownloadingTable() {
        //get directory
        directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/";
        if (!new File(directory).exists()) {
            //save file in application folder
            directory = getApplicationContext().getFilesDir().toString() + "/Downloads/";
            if (!new File(directory).exists())
                new File(directory).mkdir();

            Message msg = Message.obtain();
            msg.what = UIMessage.ERROR_DOWNLOAD_NOT_FOUND;
            handler.sendMessage(msg);
        }

        downloadingTasksLUT = new int[TaskManager.TASK_NUM][TaskManager.TASK_FIELD];
        int taskIDsList[] = {R.id.task1, R.id.task2, R.id.task3, R.id.task4, R.id.task5, R.id.task6, R.id.task7, R.id.task8, R.id.task9, R.id.task10};

        for (int i = 0; i < TaskManager.TASK_NUM; i++) {
            downloadingTasksLUT[i][TaskManager.ID] = taskIDsList[i];
            updateDownloadingTable(i, 1, 0, 0);
        }
    }

    public void updateDownloadingTable(int id, int isIdle, int isPause, int isCancel) {
        downloadingTasksLUT[id][TaskManager.IS_IDLE] = isIdle;
        downloadingTasksLUT[id][TaskManager.IS_PAUSE] = isPause;
        downloadingTasksLUT[id][TaskManager.IS_CANCEL] = isCancel;
    }

    public void initRecentGridView() {
        //calculate appropriate image width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenPadding = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

        //api lower than 15 does not support, use user-defined values
        int gridViewHorizontalSpacing = 5;
        int gridViewColumnNum = 3;

        int imageWidth = (screenWidth - (screenPadding + gridViewHorizontalSpacing) * 2) / gridViewColumnNum;

        //create adapter
        GridView recentImages = (GridView)findViewById(R.id.images);
        gridImageAdapter = new ImageAdapter(getApplicationContext(), imageWidth);

        recentImages.setAdapter(gridImageAdapter);
    }

    public void initBackgrounds() {
        int bgs[] = {R.drawable.sad, R.drawable.laugh, R.drawable.downloading, R.drawable.screenoff, R.drawable.abreak, R.drawable.delete,
                     R.drawable.cute, R.drawable.cute2, R.drawable.lunch, R.drawable.notfunny,
                     R.drawable.wifi, R.drawable.sleepy, R.drawable.donttouch, R.drawable.hurt};
        backgroundsID = bgs;
        changeBackground(-1);
    }

    public void changeBackground(int type) {
        int imageID = backgroundsID[0];
        switch (type) {
            case UIMessage.CREATE_TASK:
                imageID = backgroundsID[2];
                break;
            case UIMessage.PAUSE_TASK:
                imageID = backgroundsID[4];
                break;
            case UIMessage.RESUME_TASK:
                imageID = backgroundsID[2];
                break;
            case UIMessage.CANCEL_TASK:
                imageID = backgroundsID[5];
                break;
            case UIMessage.UPDATE_RECENT:
                imageID = backgroundsID[1];
                break;
            case UIMessage.SCREEN_ON:
                imageID = backgroundsID[3];
                break;
            case UIMessage.ANIMATION:
                imageID = backgroundsID[(new Random().nextInt(backgroundsID.length - 6)) + 6];
                break;
        }
        ((ImageView)findViewById(R.id.animation)).setImageResource(imageID);
    }

    public void startDownloadBtnListener() {
        ((ImageView)findViewById(R.id.btn_download)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //hide keyboard
                ((InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                //check url validity
                String urlString = ((EditText)findViewById(R.id.text_url)).getText().toString();

                if (urlString.contentEquals("@m")) { //Multiple Download Command
                    String URLs[] = {"http://i.imgur.com/GEhgqOC.jpg",
                                     "http://img.ltn.com.tw/Upload/liveNews/BigPic/600_phpdnlnun.jpg",
                                     "http://cdn2.ettoday.net/images/1668/d1668910.jpg",
                                     "http://image.knowing.asia/1e411695-5148-4b51-b49d-9b5213ba9a26/42ba384849112792b33b05f4754071cc.png",
                                     "http://cdn2.ettoday.net/images/1232/d1232397.jpg",
                                     "http://2.bp.blogspot.com/-GyT6r-XPnME/Va-7-Mje0KI/AAAAAAAAF1k/q6HOxvboudg/s1600/IMG_1545.jpg",
                                     "http://attach.azureedge.net/newsimages/2016/04/24/507644-XXL.jpg",
                                     "http://photos.sacurrent.com/wp-content/uploads/2015/10/121121003_L.jpg",
                                     "http://jpninfo.com/wp-content/uploads/2015/08/Kumamon4.jpg",
                                     "https://scontent.cdninstagram.com/hphotos-xfa1/t51.2885-15/e15/11242493_1686098351618635_636711823_n.jpg"};

                    for (int i = 0; i < URLs.length; i++)
                        startDownloadTask(URLs[i]);

                } else if (urlString.contentEquals("@l")) { //Large File Download Command
                    String largeFileURL = "http://download.thinkbroadband.com/10MB.zip";
                    startDownloadTask(largeFileURL);
                } else if (!(urlString.contains("//") && urlString.contains("."))) {    //user input URL
                    Message msg = Message.obtain();
                    msg.what = UIMessage.ERROR_INVALID_URL;
                    handler.sendMessage(msg);
                }
                else
                    startDownloadTask(urlString);
            }
        });
    }

    public void startHistoryBtnListener() {
        ((ImageView)findViewById(R.id.btn_history)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //jump to history activity
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });

        ((GridView)findViewById(R.id.images)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //jump to history activity
                Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
            }
        });
    }

    public void startScreenStateListener() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenStateReceiver, intentFilter);
    }

    public void startUboAnimation() {
        ((RelativeLayout)findViewById(R.id.ubo_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBackground(UIMessage.ANIMATION);
            }
        });
    }

    public void startDownloadTask(String urlString) {
        DownloadTask task = new DownloadTask(urlString);
        if (task.id == TaskManager.ID_NOT_FOUND) {
            Message msg = Message.obtain();
            msg.what = UIMessage.ERROR_THREAD_NOT_FOUND;
            handler.sendMessage(msg);
        } else {
            task.start();
        }
    }

    class DownloadTask extends Thread {
        public int id;
        public String filename;
        public int progress;
        boolean downloadComplete = false;
        boolean downloadCancel = false;

        private URL url = null;
        private HttpURLConnection connection = null;

        DownloadTask(String urlString) {
            //set task id for future table & UI update
            for (id = 0; id < TaskManager.TASK_NUM; id++) {
                if (downloadingTasksLUT[id][TaskManager.IS_IDLE] == 1)
                    break;
            }

            if (id >= TaskManager.TASK_NUM) {   //reject download request
                id = TaskManager.ID_NOT_FOUND;
            } else {    //start download task
                updateDownloadingTable(id, 0, 0, 0);

                //get filename from urlString
                filename = urlString.substring(urlString.lastIndexOf('/') + 1);
                if (filename.length() > MAX_LENGTH)
                    filename = filename.substring(filename.length() - MAX_LENGTH);

                int version = 1;
                while (new File(directory + filename).exists() || namePool.contains(filename)) {
                    if (version == 1)
                        filename = filename.substring(0, filename.lastIndexOf('.')) + "(" + version + ")" + filename.substring(filename.lastIndexOf('.'));
                    else
                        filename = filename.substring(0, filename.lastIndexOf('(') + 1) + version + filename.substring(filename.lastIndexOf(')'));
                    version++;
                }
                namePool.add(filename);

                //get URL from urlString
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e) {
                    Message msgURL = Message.obtain();
                    msgURL.what = UIMessage.ERROR_INVALID_URL;
                    handler.sendMessage(msgURL);
                }
            }
        }

        public void run() {
            //generate downloading task UI
            renderDownloadUI();
            startDownloadTask();
        }

        private void renderDownloadUI() {
            //set downloading dialog to visible
            Message msgShow = Message.obtain();
            msgShow.what = UIMessage.SHOW_DOWNLOADING_DIALOG;
            handler.sendMessage(msgShow);

            //Create new downloading task
            Message msgCreate = Message.obtain();
            msgCreate.what = UIMessage.CREATE_TASK;
            msgCreate.arg1 = id;
            msgCreate.obj = filename;
            handler.sendMessage(msgCreate);
        }

        private void startDownloadTask() {
            try {
                if(connectURL(0)) {
                    //get content from connected URL
                    InputStream inputStream = connection.getInputStream();
                    OutputStream outputStream = new FileOutputStream(directory + filename);

                    byte content[] = new byte[1024];
                    int res = 0;
                    long cur = 0, fileLength = connection.getContentLength();

                    while (!(downloadComplete || downloadCancel)) {
                        if (downloadingTasksLUT[id][TaskManager.IS_PAUSE] >= 1) {
                            Thread.sleep(300);
                        } else {
                            try {
                                res = inputStream.read(content);
                            } catch (ProtocolException e) {
                                if (connectURL(cur)) {
                                    inputStream.close();
                                    inputStream = connection.getInputStream();

                                    res = inputStream.read(content);
                                }

                                Message msgReconnect = Message.obtain();
                                msgReconnect.what = UIMessage.CONNECTION_RECONNECT;
                                handler.sendMessage(msgReconnect);
                            }
                            if (res != -1) {
                                //write content to file
                                outputStream.write(content, 0, res);

                                //calculate progress
                                cur += res;
                                progress = (int)((cur * 100) / fileLength);

                                //update progress bar
                                Message msgPB = Message.obtain();
                                msgPB.what = UIMessage.UPDATE_PROGRESSING_BAR;
                                msgPB.arg1 = id;
                                msgPB.arg2 = progress;
                                handler.sendMessage(msgPB);
                            } else
                                downloadComplete = true;
                        }
                        downloadCancel = downloadingTasksLUT[id][TaskManager.IS_CANCEL] == 1;
                    }

                    if (downloadComplete) {
                        outputStream.flush();
                        gridImageAdapter.addImage(gridImageAdapter.getBitmapFromFilepath(directory + filename));

                        //send change background message
                        Message msgUpdate = Message.obtain();
                        msgUpdate.what = UIMessage.UPDATE_RECENT;
                        handler.sendMessage(msgUpdate);
                    }

                    if (downloadCancel) {
                        //delete temp files
                        File f = new File(directory + filename);
                        if (f.exists())
                            f.delete();
                    }

                    //close unused streams
                    inputStream.close();
                    outputStream.close();
                } else {
                    Message msg = Message.obtain();
                    msg.what = UIMessage.ERROR_CONNECTION_FAILED;
                    handler.sendMessage(msg);
                }
            } catch (ProtocolException e) {
                Message msgTimeout = Message.obtain();
                msgTimeout.what = UIMessage.ERROR_CONNECTION_TIMEOUT;
                handler.sendMessage(msgTimeout);
            }  catch (IOException e) {
                Message msgSave = Message.obtain();
                msgSave.what = UIMessage.ERROR_SAVE_FILE;
                handler.sendMessage(msgSave);
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //end connection
                if(connection != null) {
                    connection.disconnect();
                }

                //pop saved file name
                if (namePool.contains(filename))
                    namePool.remove(filename);

                //delete downloading task UI
                Message msgDel = Message.obtain();
                msgDel.what = UIMessage.DELETE_TASK;
                msgDel.arg1 = id;
                msgDel.arg2 = downloadComplete ? 1 : 0;
                handler.sendMessage(msgDel);

                //hide downloading dialog
                Message msgHide = Message.obtain();
                msgHide.what = UIMessage.HIDE_DOWNLOADING_DIALOG;
                handler.sendMessage(msgHide);

                //recycle task ID
                updateDownloadingTable(id, 1, 0, 0);

                //delete temp file
                if (!downloadComplete) {
                    File f = new File(directory + filename);
                    if (f.exists())
                        f.delete();
                }
            }
        }

        private boolean connectURL(long offset) {
            if(connection != null) {
                connection.disconnect();
            }

            //set connection configurations
            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(0);
                connection.setConnectTimeout(0);
//                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
                connection.setRequestProperty("Range", "bytes=" + offset + "-");
                connection.setRequestMethod("GET");
//                connection.setDoInput(true);
//                connection.setDoOutput(true);
//                connection.setInstanceFollowRedirects(true);
                connection.connect();

                return connection.getResponseCode() == HttpsURLConnection.HTTP_OK ||  connection.getResponseCode() == HttpsURLConnection.HTTP_PARTIAL;
            } catch (IOException e) {
                Message msg = Message.obtain();
                msg.what = UIMessage.ERROR_CONNECTION_FAILED;
                handler.sendMessage(msg);
                return false;
            }
        }
    }

    public class ScreenStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                //resume downloading tasks
                for (int i = 0; i < TaskManager.TASK_NUM; i++)
                    if (downloadingTasksLUT[i][TaskManager.IS_IDLE] == 0 && downloadingTasksLUT[i][TaskManager.IS_PAUSE] == 2)
                        updateDownloadingTable(i, 0, 0, 0);

                //send change background message
                Message msgUpdate = Message.obtain();
                msgUpdate.what = UIMessage.SCREEN_ON;
                handler.sendMessage(msgUpdate);

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                //pause downloading tasks
                for (int i = 0; i < TaskManager.TASK_NUM; i++)
                    if (downloadingTasksLUT[i][TaskManager.IS_IDLE] == 0 && downloadingTasksLUT[i][TaskManager.IS_PAUSE] == 0)
                        updateDownloadingTable(i, 0, 2, 0);
            }
        }
    }
}
