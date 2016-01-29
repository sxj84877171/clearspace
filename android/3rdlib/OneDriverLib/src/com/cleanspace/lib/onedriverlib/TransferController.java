package com.cleanspace.lib.onedriverlib;


public class TransferController {

    private boolean isCancel = false;
    private int repeatCount=1;
    private String sessionKey;
    private boolean isReTransfer = false;

    private OneDriveProgressListener mOneDriveProgressListener;

    public TransferController(OneDriveProgressListener listener) {
        mOneDriveProgressListener = listener;
    }

    public void cancel() {
        isCancel = true;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public OneDriveProgressListener getOneDriveProgressListener() {
        return mOneDriveProgressListener;
    }

    public OneDriveListener getOneDriveListener(){
        if(!(mOneDriveProgressListener instanceof OneDriveListener)){
            return null;
        }
        return (OneDriveListener)mOneDriveProgressListener;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public boolean isReTransfer(){
        return isReTransfer;
    }

    public void setReTransfer(boolean isReTransfer){
        this.isReTransfer=isReTransfer;
    }

    public void setSessionKey(String sessionKey){
        this.sessionKey=sessionKey;
    }

    public String getSessionKey(){
        return sessionKey;
    }
}
