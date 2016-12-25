package com.example.urchin.httpdownloader;

/**
 * Created by urchin on 2016/7/5.
 */
public class TaskManager {
    static final int TASK_NUM = 10;
    static final int TASK_FIELD = 4;

    static final int ID                  = 0;
    static final int IS_IDLE            = 1;
    static final int IS_PAUSE           = 2;    // 0 for downloading, 1 for user pause, 2 for system pause
    static final int IS_CANCEL         = 3;

    static final int ID_NOT_FOUND = -1;

}
