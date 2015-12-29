package com.example.wifiapp.fragment;


import android.app.Fragment;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.wifiapp.R;
import com.example.wifiapp.callback.OnSendClickListener;
import com.example.wifiapp.model.FileInfo;
import com.example.wifiapp.utils.SensorUtils;

import java.io.File;

import butterknife.Bind;

/**
 * A simple {@link Fragment} subclass.
 */
public class SendFragment extends BaseFragment{


    public static final int TRANSLATE_FILE = 2;
    public static final int START_TRANSLATE_FILE = 1 ;
    public static final int TRANSLATE_FINISHED = 3;
    public static final int SERVER_NOT_FOUND = 4;
    public static final int NETWORK_ERROR = 5;
    public static final int SERVER_CONNECTED = 6;
    public static final int SERVER_CONNECT_FAILED = 7;
    public static final int OPENING_WIFI = 8;
    public static final int CONNECTING_SERVER = 9;
    public static final int SCANPORT = 10;
    public static final int SHOW_FILE_NAME = 11;
    public static final int BEFORE_TRANSLATE = 12;
    public static final int SHOW_FILE_SHARE = 13;
    public static final int SHOW_APP_NAME = 14;
    public static final int PREPARE_TRANSLATE = 15;

    public static final  int PORT = 2008;

    @Bind(R.id.tv_filename) TextView mFileNameTextView;
    @Bind(R.id.tv_service_state) TextView mSocketStateTextView;
    @Bind(R.id.tv_transmit_state) TextView mTranslateStateTextView;
    @Bind(R.id.tv_appname) TextView mAppNameTextView;
    @Bind(R.id.layout_app) RelativeLayout mAppChooseRelativeLayout;
    @Bind(R.id.img_folder) ImageView mFolderImg;
    @Bind(R.id.progressbar) NumberProgressBar mProgressBar;
    @Bind(R.id.btn_send) Button mSendButton;
    @Bind(R.id.bt_refresh) Button mRefreshButton;

    private String mSendFileName = null;
    private long mFileSize;
    private float mProgress;

    private OnSendClickListener mClickListener;

    public SendFragment() {
        // Required empty public constructor
    }

    @Override
    protected View createFragmentView() {
        
        View view  = mInflater.inflate(R.layout.fragment_send, mContainer, false);
        
        return view;
    }

    @Override
    protected void init() {

        mFolderImg.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
        mRefreshButton.setOnClickListener(this);
        mAppChooseRelativeLayout.setOnClickListener(this);
        mSendButton.setEnabled(false);

    }

    @Override
    protected ListView setFileListView() {
        
        return (ListView) mFragmentView.findViewById(R.id.lv_files);
    }

    public void setOnClickListener(OnSendClickListener listener){

        mClickListener = listener;

    }

    @Override
    protected void onHandleMessage(Message msg) {

        switch (msg.what){
            case OPENING_WIFI:
                mSocketStateTextView.setTextColor(getResources().getColor(R.color.blue));
                mSocketStateTextView.setText("正在打开wifi.......");
                break;
            case NETWORK_ERROR:
                mSocketStateTextView.setTextColor(Color.RED);
                mSocketStateTextView.setText("网络连接失败，检查网络后重试！");
                mTranslateStateTextView.setTextColor(Color.RED);
                mTranslateStateTextView.setText("等待服务连接！");
                mRefreshButton.setVisibility(View.VISIBLE);
                showMsg("网络连接失败，请稍后重试！");
                break;
            case CONNECTING_SERVER:
                mSocketStateTextView.setTextColor(getResources().getColor(R.color.blue));
                mSocketStateTextView.setText("正在连接服务......");
                break;
            case SCANPORT:
                mSocketStateTextView.setTextColor(getResources().getColor(R.color.blue));
                mSocketStateTextView.setText("扫描 ip:" + (String)msg.obj + " 端口:" + PORT + "........");
                break;
            case SERVER_NOT_FOUND:
                mSocketStateTextView.setTextColor(Color.RED);
                mSocketStateTextView.setText("没有发现开启服务主机，检查后重试！");
                mTranslateStateTextView.setTextColor(Color.RED);
                mTranslateStateTextView.setText("等待服务连接！");
                mRefreshButton.setVisibility(View.VISIBLE);
                break;
            case SERVER_CONNECTED:
                showSocketState("发送服务运行中.....", "文件传输就绪....", true, View.INVISIBLE);
                break;
            case SERVER_CONNECT_FAILED:
                showSocketState("获取传输连接失败,检查后重试！", "等待发送服务开启！", false, View.VISIBLE);
                break;
            case SHOW_FILE_SHARE:
                mFileNameTextView.setText("共享" + (int) msg.obj + "个文件");
                break;
            case SHOW_FILE_NAME:
               showFileName((String) msg.obj);
                break;
            case SHOW_APP_NAME:
                showAppName((String)msg.obj);
                break;
            case BEFORE_TRANSLATE:
                beforeTranslate();
                break;
            case PREPARE_TRANSLATE:
                prepareTranslate((FileInfo)msg.obj);
                break;
            case START_TRANSLATE_FILE:
                mTranslateStateTextView.setTextColor(Color.argb(255, 19, 140, 19));
                mTranslateStateTextView.setText("正在传输" + (String) msg.obj +".........");
                break;
            case TRANSLATE_FILE:
                updateProgress(msg);
                break;
            case TRANSLATE_FINISHED:
                finishClear();
                break;
            default:
                break;
        }

    }

    private void showAppName(String obj) {

        mAppNameTextView.setText(obj);
    }

    private void beforeTranslate() {
        mSendButton.setClickable(false);
        mFolderImg.setClickable(false);
        mAppChooseRelativeLayout.setClickable(false);
    }

    public void transmitCompleted() {

        final String text = mSendFileName +"传输完成！";
        mFileNameList.add(mSendFileName);
        mFileListAdapter.notifyDataSetChanged();
        SensorUtils.notifyRing(mSendFileName + "传输完成");
        mTranslateStateTextView.setTextColor(Color.argb(255, 19, 140, 19));
        mTranslateStateTextView.setText(text);
        mFileNameTextView.setText("");
    }


    public void showSocketState(String text, String text2, boolean enabled, int visible) {
        if(enabled){

            mSocketStateTextView.setTextColor(Color.argb(255,19,140,19));
            mTranslateStateTextView.setTextColor(Color.argb(255,19,140,19));
        }
        else {
            mSocketStateTextView.setTextColor(Color.RED);
            mTranslateStateTextView.setTextColor(Color.RED);
        }
        mSocketStateTextView.setText(text);
        mTranslateStateTextView.setText(text2);
        mSendButton.setEnabled(enabled);
        mRefreshButton.setVisibility(visible);
    }


    /**
     * 显示文件名
     * @param filePath
     */
    public void showFileName(String filePath){

        File file = new File(filePath);
        if(file == null)
            return;

        if(file.exists()){

            mSendFileName = file.getName();
            mFileSize = file.length();
            mFileNameTextView.setText(mSendFileName);
        }
        else {

            if(mContext != null){

                showMsg("文件不存在");
            }

        }

    }


    public void prepareTranslate(FileInfo fileInfo) {
        mFileSize = fileInfo.getFileSize();
        mSendFileName = fileInfo.getFileName();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setMax(100);
        mProgress = 0;
        mProgressBar.setProgress(0);
    }

    public void updateProgress(Message msg) {
        mProgress += (float)(int)msg.obj * 100 / mFileSize;

        if(mProgress > 99.9){

            mProgressBar.setProgress(100);

            transmitCompleted();

            mProgressBar.setVisibility(View.GONE);

        }else {
            mProgressBar.setProgress((int)mProgress);
        }
    }

    private void finishClear() {

        mFileNameTextView.setText("");
        mAppNameTextView.setText("");

        mSendButton.setClickable(true);
        mFolderImg.setClickable(true);
        mAppChooseRelativeLayout.setClickable(true);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.img_folder:
                showFileDialog();
                break;
            case R.id.btn_send:
                sendFiles();
                break;
            case R.id.bt_refresh:
                mRefreshButton.setVisibility(View.INVISIBLE);
                refreshNetwork();
                break;
            case R.id.layout_app:
                chooseApp();
                break;
            default:
                break;
        }

    }

    private void chooseApp() {

        if(mClickListener != null){
            mClickListener.onClickAppButton();
        }
    }

    private void refreshNetwork() {

        if(mClickListener != null){
            mClickListener.onRefreshNetwork();
        }
    }

    private void sendFiles() {

        if(mClickListener != null){
            mClickListener.onClickSendButton();
        }
    }

    private void showFileDialog() {

        if(mClickListener != null){
            mClickListener.onClickFolderButton();
        }
    }

}
