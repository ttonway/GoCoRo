package com.wcare.android.gocoro.core;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.ui.ActivityMain;


/**
 * Created by ttonway on 2016/11/21.
 */
public class BackgroundService extends Service {
    private static final String TAG = BackgroundService.class.getSimpleName();

    private static final String ACTION_START = TAG + ".START";
    private static final String ACTION_STOP = TAG + ".STOP";

    public static void startService(Context context) {
        Intent intent = new Intent(context, BackgroundService.class);
        intent.setAction(ACTION_START);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, BackgroundService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }


    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (info.service.getClassName().equals(BackgroundService.class.getName())) {
                return true;
            }
        }
        return false;
    }

    Handler mHandler;
    NotificationManager mNotificationManager;

    GoCoRoDevice mDevice;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mHandler = new Handler();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mDevice = GoCoRoDevice.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand " + intent);


        if (intent == null) {
        } else if (intent.getAction().equals(ACTION_STOP)) {
            stopSelf();
        } else if (intent.getAction().equals(ACTION_START)) {
            startForeground(R.id.service_notification, getNotification());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        stopForeground(true);
    }

    Notification getNotification() {
        String text = getString(R.string.notification_service_running);
        Intent notificationIntent = new Intent(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setClass(this, ActivityMain.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pendingIntent);
        return builder.build();
    }

//    Dialog showAlertMessage(String message) {
//        Log.d(TAG, "showAlertMessage " + message);
//        final Dialog dialog = new Dialog(this, R.style.CustomDialog);
//        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom, null, false);
//        TextView textView = (TextView) view.findViewById(R.id.text1);
//        textView.setText(message);
//        view.findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_LAUNCHER);
//                intent.setClass(BackgroundService.this, MainActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//                startActivity(intent);
//            }
//        });
//        view.findViewById(R.id.btn2).setVisibility(View.GONE);
//
//        dialog.setContentView(view);
//        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable());
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        dialog.show();
//
//        return dialog;
//    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
