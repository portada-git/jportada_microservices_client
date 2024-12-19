/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.elsquatrecaps.portada.jportadamicroservice.client.services;

/**
 *
 * @author josep
 */
public final class ProgressInfo {
    
    public static final int ERROR_INFO_TYPE = 1;
    public static final int PROGRESS_ERROR_TYPE = 201;
    public static final int PROGRESS_INFO_TYPE = 200;
    public static final int INFO_TYPE = 0;
    public static final int STATUS_INFO_TYPE = 100;
    //        public static final int ERROR=-1;
    //        public static final int PROCESS=0;
    //        public static final int MESSAGE=1;
    //        public static final int INFO=2;
    //        public static final int INFO_ERROR=3;
    //        public static final int STATUS_INFO=4;
    public static final String KEY_ALREADY_EXIST_STATUS = "KEY_ALREADY_EXIST";
    public static final String SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST_STATUS = "SUCCESS_FOR_PAPI_ACCESS_PERMISSION_REQUEST";
    public static final String OK_STATUS = "";
    private int type = 0;
    private String pre;
    private String name;
    private String process;
    private int progress;
    private int maxProgress;
    private String status;
    private int errorState = 0;

    //        public ProgressInfo(String message, String process, String status) {
    //            this(STATUS_INFO_TYPE, message, "", process, 1, 1, 0);
    //            this.status = status;
    //        }
    //
    ////        public ProgressInfo(String message, String process, boolean infoOnly) {
    ////            this(infoOnly?INFO:MESSAGE, message, "", process, 0, 100, 0);
    ////        }
    //
    ////        public ProgressInfo(String message, String process, int percent) {
    ////            this(MESSAGE, message, "", process, percent, 100, 0);
    ////        }
    //
    //        public ProgressInfo(String message, String process) {
    //            this(INFO_TYPE, message, "", process, 1, 1, 0);
    //        }
    //
    //        public ProgressInfo(String name, String process, int progress, int maxProgress) {
    //            this(PROGRESS_INFO_TYPE, "", name, process, progress, maxProgress, 0);
    //        }
    //
    //        public ProgressInfo(String preNameOrMessage, String name, String process, int progress, int maxProgress) {
    //            this(PROGRESS_INFO_TYPE, preNameOrMessage, name, process, progress, maxProgress, 0);
    //        }
    //
    //        public ProgressInfo(String name, String process, int progress, int maxProgress, int errorState) {
    //            this(PROGRESS_ERROR_TYPE, "", name, process, progress, maxProgress, errorState);
    //        }
    //
    //        public ProgressInfo(String preNameOrMessage, String name, String process, int progress, int maxProgress, int errorState) {
    //            this(PROGRESS_ERROR_TYPE, preNameOrMessage, name, process, progress, maxProgress, errorState);
    //        }
    public ProgressInfo(int type, String status, String preNameOrMessage, String name, String process, int progress, int maxProgress, int errorState) {
        this(type, preNameOrMessage, name, process, progress, maxProgress, errorState);
        this.status = status;
    }

    public ProgressInfo(int type, String preNameOrMessage, String name, String process, int progress, int maxProgress, int errorState) {
        this.type = type;
        this.pre = preNameOrMessage;
        this.name = name;
        this.process = process;
        this.progress = progress;
        this.maxProgress = maxProgress;
        this.errorState = errorState;
    }

    public String getStatus() {
        String ret = "";
        if (getType() == STATUS_INFO_TYPE) {
            ret = status;
        }
        return ret;
    }

    public String getMessage() {
        return this.pre.concat(this.name);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the process
     */
    public String getProcess() {
        return process;
    }

    /**
     * @return the progress
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @return the maxProgress
     */
    public int getMaxProgress() {
        return maxProgress;
    }

    public int getErrorState() {
        return errorState;
    }

    public int getType() {
        return type;
    }

    public void setName(String str) {
        this.name = str;
    }
    
}
