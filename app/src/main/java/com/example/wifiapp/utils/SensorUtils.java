package com.example.wifiapp.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.wifiapp.R;
import com.example.wifiapp.application.MyApplication;

/**
 * Created by fg607 on 15-12-20.
 */
public class SensorUtils {

    public static Context context = MyApplication.getContext();

    public static void notifyRing(String text){


        NotificationManager nm = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        notification.icon = R.drawable.ftp_pc;
        notification.tickerText = System.currentTimeMillis()+"";
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

       // notification.defaults |= Notification.DEFAULT_VIBRATE;
        //notification.defaults |= Notification.DEFAULT_LIGHTS;

        //让声音、振动无限循环，直到用户响应
        //notification.flags |= Notification.FLAG_INSISTENT;

        //通知被点击后，自动消失
        //notification.flags |= Notification.FLAG_AUTO_CANCEL;

        //点击'Clear'时，不清楚该通知(QQ的通知无法清除，就是用的这个)
       // notification.flags |= Notification.FLAG_NO_CLEAR;
        //第二个参数 ：下拉状态栏时显示的消息标题 expanded message title
        //第三个参数：下拉状态栏时显示的消息内容 expanded message text
        //第四个参数：点击该通知时执行页面跳转
        Intent appIntent = new Intent(context,context.getClass());
        PendingIntent pendingintent =PendingIntent.getActivity(context,0,appIntent,0);

        notification.setLatestEventInfo(context, "局域网文件传输", text, pendingintent);
        nm.notify(110, notification);



    }
}
