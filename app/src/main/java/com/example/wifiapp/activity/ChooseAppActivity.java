package com.example.wifiapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.wifiapp.R;
import com.example.wifiapp.adapter.AppAdapter;
import com.example.wifiapp.model.AppInfo;
import com.example.wifiapp.utils.AppUtils;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ChooseAppActivity extends Activity {

    @Bind(R.id.lv_app) ListView mListView;
    private ArrayList<AppInfo> mAppList;
    private AppAdapter mAppAdapter;
    public static final int CHOOSE_APPCODE = 1<<0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_app);
        ButterKnife.bind(this);
        init();
    }

    public void init(){

        mAppList = AppUtils.getAppInfos();
        mAppAdapter = new AppAdapter(this);
        mAppAdapter.addList(mAppList);

        mListView.setAdapter(mAppAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mAppAdapter.setAppChecked(mAppList.get(i).getAppName());

                Intent intent = new Intent();

                intent.putExtra("name", mAppList.get(i).getAppName());
                intent.putExtra("package", mAppList.get(i).getAppPackage());

                ChooseAppActivity.this.setResult(CHOOSE_APPCODE, intent);
                ChooseAppActivity.this.finish();

            }
        });
    }

}
