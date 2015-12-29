package com.example.wifiapp.fragment;


import android.app.Fragment;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.example.wifiapp.R;
import com.example.wifiapp.callback.OnReceiveClickListener;
import com.example.wifiapp.utils.SensorUtils;

import butterknife.Bind;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReceiveFragment extends BaseFragment {

    public static final int ERROR_DATA = -1;
    public static final int WIFI_CONNECT_FAILED = 0 ;
    public static final int TRANSLATE_STATE = 1 ;
    public static final int TRANSLATE_COMPLETED = 2;
    public static final int SERVICESTARTED = 3;
    public static final int WIFIDISCONNECTED = 4;
    public static final int TRANSLATE_FILE = 5;
    public static final int START_TRANSLATE_FILE = 6 ;
    public static final int TRANSLATE_FAILED = 7 ;
    public static final int SERVICE_START_FAILED = 8;
    public static final int OPEN_WIFI = 9;


    @Bind(R.id.tv_service_state)
    TextView mServiceStateTextView;
    @Bind(R.id.tv_transmit_state) TextView mTranslateStateTextView;
    @Bind(R.id.bt_retry)
    Button mRetryButton;
    @Bind(R.id.progressbar)
    NumberProgressBar mProgressBar;

    private float mProgress;
    private long mFileSize;
    private OnReceiveClickListener mListener;


    public ReceiveFragment() {

    }

    @Override
    protected View createFragmentView() {

        View view = mInflater.inflate(R.layout.fragment_receive, mContainer, false);

        return view;
    }

    @Override
    protected void init() {

        mRetryButton.setOnClickListener(this);

    }

    @Override
    protected ListView setFileListView() {

        return (ListView) mFragmentView.findViewById(R.id.lv_files);
    }

    public void setOnClickListener(OnReceiveClickListener listener){

        mListener = listener;
    }

    @Override
    protected void onHandleMessage(Message msg) {
        super.onHandleMessage(msg);

        if(msg.what == SERVICESTARTED){

            mServiceStateTextView.setTextColor(getResources().getColor(R.color.green));
            mTranslateStateTextView.setTextColor(getResources().getColor(R.color.green));

        }else {

            mServiceStateTextView.setTextColor(Color.RED);
            mTranslateStateTextView.setTextColor(Color.RED);
        }

        switch (msg.what){
            case OPEN_WIFI:
                mServiceStateTextView.setTextColor(getResources().getColor(R.color.blue));
                mServiceStateTextView.setText("正在打开wifi....");
                break;
            case WIFI_CONNECT_FAILED:
                mServiceStateTextView.setText("网络连接失败，请检查网络！");
                mTranslateStateTextView.setText("等待服务开启！");
                mRetryButton.setVisibility(View.VISIBLE);
                break;
            case WIFIDISCONNECTED:
                mServiceStateTextView.setText("网络断开连接，请检查网络！");
                mTranslateStateTextView.setText("等待服务开启！");
                mRetryButton.setVisibility(View.VISIBLE);
                break;
            case SERVICE_START_FAILED:
                mServiceStateTextView.setText("输服务开启失败，请检查网络！");
                mTranslateStateTextView.setText("等待服务开启！");
                mRetryButton.setVisibility(View.VISIBLE);
                break;
            case SERVICESTARTED:
                mServiceStateTextView.setText("网络连接成功,传输服务已开启");
                mTranslateStateTextView.setText("等待文件传输........");
                break;
            case ERROR_DATA:
                mTranslateStateTextView.setText("接收到错误数据，检查后重试！");
                mTranslateStateTextView.setText("等待服务开启！");
                break;
            case TRANSLATE_STATE:
                mTranslateStateTextView.setText( "正在接收文件" + (String)msg.obj + "...........");
                break;
            case TRANSLATE_COMPLETED:
                transmitFinished((String)msg.obj);
                break;
            case START_TRANSLATE_FILE:
                prepareTranslate(msg);
                break;
            case TRANSLATE_FILE:
                updateProgress(msg);
                break;
            case TRANSLATE_FAILED:
                mTranslateStateTextView.setText("网络出现异常，文件接收失败！");
                mProgressBar.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }


    public void updateProgress(Message msg) {
        mProgress += (float)(int)msg.obj * 100 / mFileSize;

        if(mProgress > 99.9){
            mProgressBar.setProgress(100);
            mProgressBar.setVisibility(View.GONE);
        }else {
            mProgressBar.setProgress((int)mProgress);
        }
    }

    public void prepareTranslate(Message msg) {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setMax(100);
        mFileSize = (long) msg.obj;
        mProgress = 0;
        mProgressBar.setProgress(0);
    }

    public void transmitFinished(String fileName) {
        mFileNameList.add(fileName);
        mFileListAdapter.notifyDataSetChanged();
        mTranslateStateTextView.setText(fileName + "接收完毕！");
        SensorUtils.notifyRing(fileName + "接收完毕");
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.bt_retry:
                mRetryButton.setVisibility(View.INVISIBLE);
                refreshNetwork();
                break;
            default:
                break;
        }
    }

    private void refreshNetwork() {

        if(mListener != null){

            mListener.onRefreshNetwork();
        }
    }
}
