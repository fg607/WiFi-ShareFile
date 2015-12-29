package com.example.wifiapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.wifiapp.R;
import com.example.wifiapp.callback.OnSendClickListener;
import com.example.wifiapp.fragment.SendFragment;
import com.example.wifiapp.model.FileInfo;
import com.example.wifiapp.utils.AppUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendActivity extends Activity {

    public static SendActivity instance = null;

    public static final  int PORT = 2008;
    public static final boolean SOCKET_AVAILABLE = true;

    private SendFragment mFragment;
    private FragmentManager mFragmentManager;
    private WifiManager mWifiManager;
    private String mServerIPAddress = null;
    private SharedPreferences mSharedPreferences;
    private Handler mHandler;
    private Message mMessage;
    private boolean mIsFromOtherApp = false;
    private String mFilePath;

    private ListView mFileManagerListView;
    private List<Map<String,Object>> mFolderList;
    private AlertDialog mFileDialog;
    private View mViewDialog;

    private List<FileInfo> mSendFileList;
    private String mSendFilePath = null;
    private String mFilePathTemp;
    private String mSendFileName = null;
    private String mFileNameTemp;
    private long mFileSize;
    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        instance = this;
        mSendFileList = new ArrayList<>();
        mSharedPreferences = getSharedPreferences("config",MODE_PRIVATE);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        initView();
        initIntent();
        connectServer();
    }

    private void initIntent() {

        Intent intent = getIntent();
        mIsFromOtherApp = intent.getBooleanExtra("isFromOtherApp", true);

        if(mIsFromOtherApp){
            handleIntent(intent);
        }
    }

    private void initView() {

        initFragment();

        initFileManager();
    }

    private void initFragment() {

        mFragment = new SendFragment();

        mFragment.setOnClickListener(new OnSendClickListener(){

            @Override
            public void onRefreshNetwork() {

                connectServer();

            }

            @Override
            public void onClickFolderButton() {

                showFileDialog();

            }

            @Override
            public void onClickAppButton() {

                Intent intent = new Intent();
                intent.setClass(SendActivity.this, ChooseAppActivity.class);
                startActivityForResult(intent,ChooseAppActivity.CHOOSE_APPCODE);

            }

            @Override
            public void onClickSendButton() {

                sendFiles();

            }
        });

        mHandler = mFragment.getHandler();
        mFragmentManager = getFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.framelayout,mFragment).commit();
    }

    public void initFileManager() {

        mFolderList = new ArrayList<Map<String,Object>>();
        mViewDialog = LayoutInflater.from(this).inflate(R.layout.filedialog, null);
        mFileManagerListView = (ListView)mViewDialog.findViewById(R.id.adapter_listView1);

        //文件管理器响应点击
        mFileManagerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {


                if ((Boolean) mFolderList.get(arg2).get("isDire")) {

                    updateFolderList((String) mFolderList.get(arg2).get("path"));

                } else {
                    mFilePathTemp = ((String) mFolderList.get(arg2).get("path"));

                    mSendFileList.add(new FileInfo(mFilePathTemp));

                    //显示文件名
                    updateUI(SendFragment.SHOW_FILE_NAME, mFilePathTemp);
                    mFileDialog.cancel();

                }


            }
        });
    }


    /**
     * 刷新文件管理器内容
     * @param currentFolder
     */
    public void updateFolderList(String currentFolder)
    {

        //清除旧的条目
        mFolderList.clear();
        SimpleAdapter adapter = new SimpleAdapter(this,mFolderList,R.layout.filedialog,
                new String[]{"name","image"},new int[]{R.id.adapter_filename,R.id.adapter_image});
        mFileManagerListView.setAdapter(adapter);


        File f = new File(currentFolder);
        File[] file = f.listFiles();

        if(!currentFolder.equals("/")){//如果不是根目录的话就在要显示的列表中加入此项
            Map<String,Object> map1=new HashMap<String,Object>();
            map1.put("name", "返回上一级目录");
            map1.put("image", R.drawable.folder_back);
            map1.put("path",f.getParent());
            map1.put("isDire", true);
            mFolderList.add(map1);
        }

        if(file != null){//必须判断 否则目录为空的时候会报错
            for(int i = 0; i < file.length; i++){
                Map<String,Object> map=new HashMap<String,Object>();
                map.put("name", file[i].getName());
                map.put("image", (file[i].isDirectory()) ? R.drawable.format_folder: R.drawable.file);
                map.put("path",file[i].getPath());
                map.put("isDire", file[i].isDirectory());
                mFolderList.add(map);
            }
        }

        mFileManagerListView.setAdapter(adapter);
    }

    private void connectServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                initSocket();
            }
        }).start();
    }


    public void showFileDialog(){

        updateFolderList("/");
        if(mFileDialog == null)
        {
            //不能多次调用setview(),否则会父视图已经包含子视图的异常。
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("选择传输文件").setView(mViewDialog).create();
            dialog.setCanceledOnTouchOutside(false);//使除了dialog以外的地方不能被点击
            mFileDialog = dialog;
        }

        mFileDialog.show();

    }


    private void sendFiles() {

        if (mSendFileList.size() < 1) {
            showMsg("请先选择要发送的文件或应用！");
            return;
        }

        updateUI(mFragment.BEFORE_TRANSLATE);

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (checkSocket() == SOCKET_AVAILABLE) {

                    notifyFilesNumber(mSendFileList.size());

                    for (FileInfo fileInfo : mSendFileList) {

                        if (fileInfo.exists()) {

                            mSendFilePath = fileInfo.getFilePath();
                            mSendFileName = fileInfo.getFileName();
                            mFileSize = fileInfo.getFileSize();

                            updateUI(mFragment.PREPARE_TRANSLATE,fileInfo);

                            startTranslate();
                        }

                    }

                    transmitFinished();


                } else {
                  updateUI(mFragment.NETWORK_ERROR);
                }

            }
        }).start();
    }


    public void startTranslate(){

        //通知服务器创建同名文件
        notifyCreateFile(mSendFileName + "$#$" + mFileSize);


        //获取服务器传来的就绪信号
        if(getBackSignal())
        {
            transmitFile(mSendFilePath);
        }

    }

    public void transmitFile(String path){

        InputStream ipstream = null;
        OutputStream opstream = null;

        File file = new File(path);

        try{
            ipstream = new FileInputStream(file);

            opstream = getOutputStream();

            byte[] buffer = new byte[1024*1024];

            int temp = 0;

            updateUI(mFragment.START_TRANSLATE_FILE,file.getName());

            while((temp = ipstream.read(buffer))!=-1)
            {

                opstream.write(buffer, 0, temp);

                updateUI(mFragment.TRANSLATE_FILE,temp);

            }
            opstream.flush();

            sleep(1000);


        }catch(IOException e){

           updateUI(mFragment.NETWORK_ERROR);

        }
        finally{

            try {

                ipstream.close();

            } catch (IOException e) {
                showMsg("网络异常！");
            }


        }
    }

    public InputStream getInputStream() throws IOException {

        InputStream ips = null;

        if(mSocket != null){

            ips = mSocket.getInputStream();
        }

        return ips;
    }

    private OutputStream getOutputStream() throws IOException {

        OutputStream ops = null;

        if(mSocket != null){

            ops = mSocket.getOutputStream();
        }

        return ops;
    }


    public void transmitFinished() {

        updateUI(mFragment.TRANSLATE_FINISHED);

        mSendFileList.clear();

        try {
            if(mSocket != null){
                mSocket.close();
                mSocket = null;
            }

        } catch (IOException e) {
            showMsg("网络异常！");
        }
    }


    /**
     *get the server ready signal
     */
    public boolean getBackSignal()
    {
        try {
            InputStream ipstream = getInputStream();
            byte[] buffer = new byte[1024];
            int temp = 0;
            temp = ipstream.read(buffer);
            String isready;
            if(temp != -1){

                isready = new String(buffer,0,temp);
                if(isready.equals("transmitready"))
                {
                    return true;
                }
            }

        } catch (IOException e) {

            updateUI(mFragment.NETWORK_ERROR);
        }
        return false;
    }

    /**
     * 	通知服务器创建同名文件
     */
    public void notifyCreateFile(String fileInfo)
    {
        OutputStream opstream = null;
        try {
            opstream = getOutputStream();

            opstream.write(fileInfo.getBytes("UTF-8"));
            opstream.flush();
        } catch (UnknownHostException e) {

           updateUI(mFragment.NETWORK_ERROR);

        } catch (IOException e) {
            updateUI(mFragment.NETWORK_ERROR);
        }

    }


    private void notifyFilesNumber(Integer number) {

        OutputStream opstream = null;
        try {
            opstream = getOutputStream();
            opstream.write(String.valueOf(number).getBytes());
            opstream.flush();

            sleep(500);

        } catch (IOException e) {
            showMsg("网络异常！");
        }


    }

    private boolean checkSocket() {

        if(mSocket == null){
            mSocket = getSocket();
        }

        if(mSocket == null){
            return false;
        }else {
            return true;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){

            mFileNameTemp = data.getStringExtra("name") + ".apk";
            updateUI(mFragment.SHOW_APP_NAME,mFileNameTemp);
            mFilePathTemp = AppUtils.getFilePath(data.getStringExtra("package"));

            FileInfo fileInfo = new FileInfo(mFilePathTemp);
            fileInfo.setFileName(mFileNameTemp);
            mSendFileList.add(fileInfo);

        }
    }


    /**
     * 开启传输服务
     */
    public void initSocket(){

        openWifi();

        if(mServerIPAddress == null){

            mServerIPAddress = scanPortOpendHost();
        }


        if(mServerIPAddress == null){
            return;
        }

        mSocket = getSocket();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }


    private void handleIntent(Intent intent) {

        String action = intent.getAction();
        if(action != null){
            if (action.equals(Intent.ACTION_SEND) ||
                    action.equals(Intent.ACTION_SEND_MULTIPLE)) {

                if (action.equals(Intent.ACTION_SEND)) {

                    handleActionSend(intent);

                } else if (action.equals(Intent.ACTION_SEND_MULTIPLE)){

                    handleActionSendMultiple(intent);

                }
            }
        }

    }


    private void handleActionSend(Intent intent) {

        Uri stream = (Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM);

        handleUri(stream);

        updateUI(mFragment.SHOW_FILE_SHARE,1);
    }

    private void handleUri(Uri stream) {
        if(stream != null && stream.toString().substring(0,6).contains("file")){

            handleUriFile(stream);
        }else{

            handleUriContent(stream);
        }
    }

    private void handleActionSendMultiple(Intent intent) {

        ArrayList<Uri> uris = new ArrayList<Uri>();
        String mimeType = intent.getType();
        uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        if (mimeType != null && uris != null) {


            for(Uri stream:uris){

                handleUri(stream);

            }
        }

        updateUI(mFragment.SHOW_FILE_SHARE, uris.size());

    }

    public void handleUriFile(Uri stream){

        mFilePath  = stream.getPath();

        if (mFilePath != null){

            mSendFileList.add(new FileInfo(mFilePath));
        }

    }

    public String resolveFileNameFromUri(Uri stream){

        String fileName = null;
        Cursor cursor = getContentResolver().query(stream, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(cursor
                        .getColumnIndexOrThrow("_data"));
                if (fileName == null) {
                    fileName = cursor.getString(cursor
                            .getColumnIndexOrThrow("hint"));
                }
                if (fileName == null) {
                    fileName = "Unknown file";
                }
            }
        }
        return fileName;
    }
    public void handleUriContent(Uri stream){

        mFilePath = resolveFileNameFromUri(stream);

        if (mFilePath != null) {

            mSendFileList.add(new FileInfo(mFilePath));
        }

    }

    public void openWifi(){

        if(!mWifiManager.isWifiEnabled()){

            mWifiManager.setWifiEnabled(true);

            updateUI(SendFragment.OPENING_WIFI);
            sleep(6000);
        }
    }


    public void sleep(long time){

        try {
            Thread.currentThread().sleep(time);
        } catch (InterruptedException e) {
            showMsg("程序异常！");
        }
    }


    /**
     * 获取传输连接
     */

    public Socket getSocket()
    {
        Socket socketTemp = null;
        try{
            socketTemp = new Socket(mServerIPAddress, PORT);

            updateUI(SendFragment.SERVER_CONNECTED);


        }
        catch(Exception e)
        {
            updateUI(SendFragment.SERVER_CONNECT_FAILED);

            //重置ip,需再次扫描ip
            mServerIPAddress = null;
        }


        return socketTemp;
    }




    /**
     * 扫描局域网指定ip范围端口开放
     */
    public String scanPortOpendHost()
    {
        //使用上一次ip连接服务器
        String lastServerIp = mSharedPreferences.getString("lastServerIp",null);

        if(lastServerIp != null && isPortOpened(lastServerIp)){

            return lastServerIp;
        }


        String localIp = null;

        localIp = getLocalHostIP();

        if(localIp == null){
           updateUI(SendFragment.NETWORK_ERROR);
            return null;
        }
        //获取主机所在网段
        int index = localIp.lastIndexOf(".");

        String ipPeroid = localIp.substring(0, index+1);
        String lastPeroid = localIp.substring(index+1);

        int i_lastPeroid = Integer.parseInt(lastPeroid);

        //扫描主机前后10位ip
        for(int i = 1;i<=10;i++)
        {
            int temp = i_lastPeroid + i;
            int temp1 = i_lastPeroid - i;

            if(isPortOpened(ipPeroid + temp))
            {
                //保存服务器ip
                mSharedPreferences.edit().putString("lastServerIp",ipPeroid + temp).commit();
                return ipPeroid + temp;
            }
            else if(temp1>1)
            {
                if(isPortOpened(ipPeroid + temp1))
                {
                    //保存服务器ip
                    mSharedPreferences.edit().putString("lastServerIp",ipPeroid + temp).commit();
                    return ipPeroid + temp1;
                }
            }
        }

        updateUI(SendFragment.SERVER_NOT_FOUND);

        showMsg("未发现服务主机，请稍后重试！");

        return null;
    }

    public String getLocalHostIP(){

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        int ipAddress = wifiInfo.getIpAddress();

        return intToIp(ipAddress);


    }

    /**
     * 将获取的ＩＰ转化为字符串
     * @param i
     * @return
     */
    public String intToIp(int i){

        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." +((i >> 16) & 0xFF) + "." +((i >> 24) & 0xFF);

    }

    /**
     * 检查端口是否开放
     */
    public boolean isPortOpened(final String ip)
    {

        Socket tempSocket = null;
        try {

            updateUI(SendFragment.SCANPORT,ip);

            tempSocket = new Socket(ip, PORT);

            return true;
        } catch (UnknownHostException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        finally{
            if(tempSocket!=null)
            {
                try {
                    tempSocket.close();
                } catch (IOException e) {
                    showMsg("网络异常！");
                }
            }
        }
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

    public void showMsg(String msg){

        getMainLooper().prepare();
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        closeSocket();
        finish();
    }

   public void closeSocket(){

        if(mSocket != null)
        {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                showMsg("网络异常！");
            }

        }

    }


    @Override
    public void onBackPressed() {

        if(mIsFromOtherApp){

            super.onBackPressed();
        }else {
            //返回键让程序进入后台运行
            moveTaskToBack(true);
        }


    }

}
