package com.example.wifiapp.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wifiapp.R;

import java.util.ArrayList;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener{

    protected LayoutInflater mInflater;
    protected ViewGroup mContainer;
    protected ListView mFileListView;
    protected Context mContext;
    protected View mFragmentView;
    protected ArrayList<String> mFileNameList;
    protected FileListAdapter mFileListAdapter;
    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){

            if(!mIsDetached){
                onHandleMessage(msg);
            }

        }
    };
    protected Message mMessage = null;
    protected SharedPreferences mSharePreferences;
    protected boolean mIsDetached;


    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mSharePreferences = mContext.getSharedPreferences("config", mContext.MODE_PRIVATE);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mIsDetached = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mIsDetached = true;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        mContainer = container;

        mFragmentView = createFragmentView();
        ButterKnife.bind(this, mFragmentView);

        mFileListView = setFileListView();
        initFileNameList();

        init();

        return mFragmentView;
    }

    protected abstract void init();

    protected  void onHandleMessage(Message msg){


    }

    protected abstract ListView setFileListView();

    public void initFileNameList() {

        mFileNameList = new ArrayList<String>();
        mFileListAdapter = new FileListAdapter();
        mFileListView.setDivider(null);
        mFileListView.setAdapter(mFileListAdapter);

    }

    protected abstract View createFragmentView();

    /**
     * UpdateUI
     * @param what
     */
    public void updateUI(int what ){

        mMessage = mHandler.obtainMessage();

        mMessage.what = what;

        mHandler.sendMessage(mMessage);


    }


    public void updateUI(int what,Object obj){
        mMessage = mHandler.obtainMessage();
        mMessage.what = what;
        mMessage.obj = obj;
        mHandler.sendMessage(mMessage);
    }

    public void showMsg(String msg){

        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public Handler getHandler(){

        return mHandler;
    }

    @Override
    public void onClick(View view) {

    }

    class FileListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mFileNameList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            TextView textView = null;

            if (view != null){

                textView = (TextView)view;
            }
            else {

                textView = new TextView(mContext);
            }

            textView.setText(mFileNameList.get(i));
            textView.setTextSize(15);
            textView.setTextColor(getResources().getColor(R.color.blue));

            return textView;
        }
    }
}
