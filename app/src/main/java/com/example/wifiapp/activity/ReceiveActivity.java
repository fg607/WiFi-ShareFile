package com.example.wifiapp.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.example.wifiapp.R;
import com.example.wifiapp.callback.OnReceiveClickListener;
import com.example.wifiapp.fragment.ReceiveFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveActivity extends Activity {

    public static ReceiveActivity instance = null;

    private ConnectivityManager mConnectivityManager;
    private NetworkInfo mNetworkInfo;
    private WifiManager mWifiManager = null;
    public boolean mIsWifiConnected = false;
    private Socket mConnectSocket = null;
    private ServerSocket mServerSocket = null;
    private boolean mIsStopServer = false;
    private ReceiveFragment mFragment;
    private Handler mHandler;
    private Message mMessage;
    private FragmentManager mFragmentManager;

    private String mSdcardPath;
    private long mFileSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_receive);

        instance = this;

        mSdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        initFragment();

        initNewWorkService();

        initSocket();

        loopCheckWifiState();
    }

    private void initFragment() {

        mFragment = new ReceiveFragment();

        mFragment.setOnClickListener(new OnReceiveClickListener() {
            @Override
            public void onRefreshNetwork() {

                initSocket();
            }
        });

        mHandler = mFragment.getHandler();
        mFragmentManager = getFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.framelayout,mFragment).commit();
    }

    private void initNewWorkService() {

        mWifiManager = (WifiManager) ReceiveActivity.this.getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    public void initSocket(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                openWifi();

                if(waitWifiConnect()){

                    mIsWifiConnected = true;

                    startSocketServer();

                }else {
                   updateUI(ReceiveFragment.WIFI_CONNECT_FAILED);
                }
            }
        }).start();

    }

    public void loopCheckWifiState(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!mIsStopServer){

                    //连接上wifi后开始监控连接状态
                    if(mIsWifiConnected){

                        if(!checkWifiConnected()){

                            mIsWifiConnected = false;
                            updateUI(ReceiveFragment.WIFIDISCONNECTED);
                        }

                    }

                }
            }
        }).start();
    }

    /**
     * openWifi
     */
    public void openWifi(){

        final boolean isWifiEnabled = mWifiManager.isWifiEnabled();

        if(!isWifiEnabled){

            updateUI(mFragment.OPEN_WIFI);
            mWifiManager.setWifiEnabled(true);
        }

    }

    public boolean waitWifiConnect() {
        int waitCount = 0;
        while (!checkWifiConnected() && waitCount < 8){

            sleep(1000);

            waitCount++;
        }

        if(waitCount == 8){
            return false;
        }else {
            return true;
        }
    }

    /**
     * start socket server on port 2008
     */
    public void startSocketServer(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    if(mServerSocket == null){
                        mServerSocket = new ServerSocket(2008);
                    }

                   updateUI(ReceiveFragment.SERVICESTARTED);

                    while(mIsWifiConnected && !mIsStopServer) {

                        mConnectSocket = mServerSocket.accept();
                        new SocketThread(mConnectSocket).start();
                    }

                } catch (IOException e) {
                    updateUI(ReceiveFragment.SERVICE_START_FAILED);
                }
            }
        }).start();


    }

    /**
     * check wifi is connected
     * @return
     */
    public boolean checkWifiConnected(){

        mNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mNetworkInfo.isConnected();
    }

    class SocketThread extends Thread{

        private Socket socket = null;
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        public SocketThread(Socket socket){

            this.socket = socket;

        }

        public InputStream getInputStream(){

            if(inputStream == null){

                try {
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return inputStream;

        }

        public OutputStream getOutputStream(){

            if(outputStream == null){

                try {
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return outputStream;

        }

        public void run(){


            InputStream ips = getInputStream();
            OutputStream ops = getOutputStream();

            if(ips == null || ops == null){

                updateUI(ReceiveFragment.ERROR_DATA);
                return;
            }

            int fileNumber = receiveFileNumber(ips);

            if(fileNumber == -1 || fileNumber < 0){

                updateUI(ReceiveFragment.ERROR_DATA);
                return;
            }

            for(int i =0;i< fileNumber;i++){

                //obtain transmit filename and create the same file

                File file = createFile(ips,mSdcardPath);

                if(file == null){

                    updateUI(ReceiveFragment.ERROR_DATA);
                    return;
                }


                //notify custom to transmit file

                boolean isSuccess = noticeReady(ops,"transmitready");

                if(!isSuccess){

                    updateUI(ReceiveFragment.ERROR_DATA);
                    return;
                }

                //start translating
                receiveFileData(ips,file);

            }


            try {
                if(socket != null){

                    socket.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private int receiveFileNumber(InputStream ipstream) {

        if(ipstream == null){
            return  -1;
        }

        try {
            byte[] buffer = new byte[1024];
            int temp = 0;

            temp = ipstream.read(buffer);

            String receiveData;

            if(temp!=-1){
                receiveData = new String(buffer,0,temp);
                return Integer.parseInt(receiveData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;

    }

    /**
     * receive filename and create the same
     * @param inputStream
     * @param sdPath
     * @return
     */
    public File createFile(InputStream inputStream,String sdPath){

        File file = null;

        try {
            byte[] buffer = new byte[1024];
            int temp = 0;

            if(inputStream != null){
                temp = inputStream.read(buffer);
            }else {
                return file;
            }

            String receiveData;

            if(temp != -1){
                receiveData = new String(buffer,0,temp);
            } else {

                return file;
            }


            int flag = receiveData.indexOf("$#$",0);

            String filename = receiveData.substring(0, flag);

            mFileSize = Long.decode(receiveData.substring(flag+3));

            String filepath = sdPath+"/WifiSharedFolder/"+filename;


            File folder = new File(sdPath+"/WifiSharedFolder");

            if(!folder.exists())
                folder.mkdir();


            file = new File(filepath);
            file.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;

    }



    /**
     * notify custom to transmit file
     * @param opstream
     * @param msg
     */
    public boolean noticeReady(OutputStream opstream,String msg){



        try {

            if(opstream != null){

                opstream.write(msg.getBytes());

                opstream.flush();
            }else {
                return false;
            }



        } catch (IOException e) {
            return false;
        }

        return true;

    }


    /**
     * receive fileData
     * @param ipstream
     * @param file
     */

    public void receiveFileData(InputStream ipstream,File file){

        if(ipstream == null || file == null){

           updateUI(ReceiveFragment.ERROR_DATA);
            return;
        }

        String fileName = file.getName();
        FileOutputStream fileopstream = null;

        try {

            fileopstream = new FileOutputStream(file);
            byte[] buffer = new byte[1024*1024];
            int temp = 0;
            int receiveSize = 0;

            updateUI(ReceiveFragment.TRANSLATE_STATE,fileName);
            updateUI(ReceiveFragment.START_TRANSLATE_FILE,mFileSize);

            while((temp= ipstream.read(buffer))!=-1){

                fileopstream.write(buffer, 0, temp);
                updateUI(ReceiveFragment.TRANSLATE_FILE, temp);

                receiveSize += temp;

                if(receiveSize == mFileSize){

                    break;
                }

            }

            if(receiveSize != mFileSize){

                deleteFile(file);
                updateUI(ReceiveFragment.TRANSLATE_FAILED);
                return;
            }

            fileopstream.flush();

        } catch (IOException e) {

            deleteFile(file);
            String str = "网络出现异常，文件接收失败！";
            updateUI(ReceiveFragment.TRANSLATE_STATE, str);
            return;
        } finally {

            if(fileopstream != null){

                try {

                    fileopstream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        updateUI(ReceiveFragment.TRANSLATE_COMPLETED,fileName);
    }

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



    public void sleep(long time){

        try {
            Thread.currentThread().sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(File file){

        if(file.exists()){
            file.delete();
        }


    }

    @Override
    protected void onDestroy() {


        //停止所有循环线程
        mIsStopServer = true;

        releaseResource();


        //等待所有线程退出，关闭进程
        new Thread(new Runnable() {
            @Override
            public void run() {

                sleep(100);

            }
        }).start();

        super.onDestroy();
    }


    public void releaseResource(){

        try {
            if(mConnectSocket != null){
                mConnectSocket.close();
                mConnectSocket = null;
            }
            if (mServerSocket != null) {
                mServerSocket.close();
                mServerSocket = null;
            }
        }
        catch (IOException e){
            e.printStackTrace();

        }
    }

    @Override
    public void onBackPressed() {

        //返回键让程序进入后台运行
        moveTaskToBack(true);

    }
}
