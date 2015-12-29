package com.example.wifiapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.wifiapp.R;

public class FirstActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
    }

    public void sendFiles(View v){

        Intent intent = new Intent(FirstActivity.this,SendActivity.class);
        intent.putExtra("isFromOtherApp",false);
        startActivity(intent);
    }

    public void receiveFiles(View v){

        Intent intent = new Intent(FirstActivity.this,ReceiveActivity.class);
        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            onDestroy();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        //销毁ReceiveActivity
        if(ReceiveActivity.instance != null){

            ReceiveActivity.instance.onDestroy();
        }

        //销毁SendActivity
        if(SendActivity.instance != null){

            SendActivity.instance.onDestroy();
        }

        super.onDestroy();

        finish();
    }
}
