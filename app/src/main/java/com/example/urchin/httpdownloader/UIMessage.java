package com.example.urchin.httpdownloader;

/**
 * Created by urchin on 2016/7/5.
 */
public class UIMessage {
    //update UI layout
    static final int SHOW_DOWNLOADING_DIALOG = 0;
    static final int HIDE_DOWNLOADING_DIALOG  = 1;
    static final int CREATE_TASK                   = 2;
    static final int PAUSE_TASK                     = 3;
    static final int RESUME_TASK                   = 4;
    static final int CANCEL_TASK                   = 5;
    static final int DELETE_TASK                   = 6;
    static final int UPDATE_PROGRESSING_BAR    = 7;
    static final int UPDATE_RECENT                = 8;
    static final int SCREEN_ON                     = 9;
    static final int ANIMATION                     = 10;

    static final int CONNECTION_RECONNECT     = 20;

    static final int LOAD_IMAGE_FINISHED        = 30;

    //show user message
    static final int ERROR_INVALID_URL = 50;
    static final int ERROR_CONNECTION_FAILED = 51;
    static final int ERROR_CONNECTION_TIMEOUT = 52;
    static final int ERROR_SAVE_FILE = 53;
    static final int ERROR_DOWNLOAD_NOT_FOUND = 54;
    static final int ERROR_THREAD_NOT_FOUND = 55;

}
