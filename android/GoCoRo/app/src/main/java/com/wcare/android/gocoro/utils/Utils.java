package com.wcare.android.gocoro.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;


import com.github.mikephil.charting.charts.Chart;
import com.wcare.android.gocoro.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: ttonway
 * Date: 14/11/3
 * Time: 下午5:09
 */
public class Utils {
    private static final String TAG = "Utils";

    private Utils() {
    }

    @TargetApi(11)
    public static void enableStrictMode() {
        if (Utils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            if (Utils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
//                vmPolicyBuilder
//                        .setClassInstanceLimit(ActivityHome.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasIceCreamSandwich() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }


    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        Log.d(TAG, "External storage mounted? " + Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()));
        Log.d(TAG, "External storage removable? " + isExternalStorageRemovable());
        Log.d(TAG, "ExternalCacheDir: " + getExternalCacheDir(context));
        Log.d(TAG, "CacheDir: " + context.getCacheDir());

        final String cachePath =
                (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && getExternalCacheDir(context) != null) ||
                        (!isExternalStorageRemovable() && getExternalCacheDir(context) != null) ?
                        getExternalCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     * otherwise.
     */
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(8)
    public static File getExternalCacheDir(Context context) {
        if (Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * 检查WiFi开关是否打开
     *
     * @param context
     * @return
     */
    public static boolean isWifiEnabled(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return (wifi != null) && wifi.isWifiEnabled();
    }

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static String getAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getAppVersion fail.", e);
            return null;
        }
    }

    public static int getAppVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getAppVersionCode fail.", e);
            return 0;
        }
    }


    public static boolean isEmail(String email) {
        String str = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseInt fail.", e);
        }
        return 0;
    }

    public static float parseFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            Log.e(TAG, "parseFloat fail.", e);
        }
        return 0.f;
    }

    public static String formatTime(int seconds) {
        if (seconds == 0) {
            return "0";
        }

        int hour = seconds / 3600;
        int min = seconds / 60 - hour * 60;
        int sec = seconds - min * 60 - hour * 3600;

//        String str = "";
//        if (hour > 0) {
//            str += hour + "h";
//        }
//        if (min > 0) {
//            str += min + "m";
//        }
//        if (sec > 0) {
//            str += sec + "s";
//        }
        return String.format("%d:%02d", min, sec);
    }

    public static String formatTime2(int seconds) {
        if (seconds == 0) {
            return "0sec";
        }

        int hour = seconds / 3600;
        int min = seconds / 60 - hour * 60;
        int sec = seconds - min * 60 - hour * 3600;

        String str = "";
        if (hour > 0) {
            str += hour + "h";
        }
        if (min > 0) {
            str += min + "min";
        }
        if (sec > 0) {
            str += sec + "sec";
        }
        return str;
    }

    public static boolean saveImage(Bitmap bitmap, File file) {

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "saveImage fail.", e);
            return false;
        } finally {
            try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static Bitmap getChartBitmap(Chart chart) {
        Bitmap returnedBitmap = Bitmap.createBitmap(chart.getWidth(), chart.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(0xff2e2836);
        chart.draw(canvas);
        return returnedBitmap;
    }

    public static void shareContent(final Activity activity, String title, Bitmap bitmap, String url) {

        // NOTICE! bitmap maybe null

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
//        intent.setType("image/jpeg");
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, url);
//        intent.putExtra("sms_body", url);
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.dialog_title_share)));

        /*
        File cacheDir = activity.getExternalCacheDir();
        File file = new File(cacheDir, "chart.jpg");
        Log.i(TAG, "save image to " + file + " result " + saveImage(bitmap, file));

        OnekeyShare oks = new OnekeyShare();
        oks.setSilent(true);
        //关闭sso授权
        oks.disableSSOWhenAuthorize();
        // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
        oks.setTitle(title);
        // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
        oks.setTitleUrl(url);
        // text是分享文本，所有平台都需要这个字段
        oks.setText(activity.getString(R.string.share_content));
        //分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
        // oks.setImageUrl("http://f1.sharesdk.cn/imgs/2014/02/26/owWpLZo_638x960.jpg");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(file.getAbsolutePath());//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(url);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        // oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite("GoCoRo");
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(Constants.WEB_HOST);

        // 启动分享GUI
        oks.show(activity);
*/

//        ShareBoardConfig config = new ShareBoardConfig();
//        config.setShareboardPostion(ShareBoardConfig.SHAREBOARD_POSITION_CENTER);
//        config.setTitleVisibility(false);
//        config.setCancelButtonText(activity.getString(R.string.btn_cancel));
//
//        ShareAction action = new ShareAction(activity);
//        UMImage thumb = new UMImage(activity, bitmap);
//        thumb.compressStyle = UMImage.CompressStyle.SCALE;
//
//        UMWeb web = new UMWeb(url);
//        web.setTitle(activity.getString(R.string.activity_main));//标题
//        web.setThumb(thumb);  //缩略图
//        web.setDescription(activity.getString(R.string.about_content));//描述
//
//        action.withMedia(web);
//
//        action.withText(activity.getString(R.string.app_name))
//                .setDisplayList(SHARE_MEDIA.FACEBOOK, SHARE_MEDIA.SINA, SHARE_MEDIA.QQ, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.WEIXIN_FAVORITE)
////                .addButton("umeng_sharebutton_copy", "umeng_sharebutton_copy", "umeng_socialize_copy", "umeng_socialize_copy")
//                .setCallback(new UMShareListener() {
//                    @Override
//                    public void onStart(SHARE_MEDIA share_media) {
//                        Log.e(TAG, "shared start on " + share_media);
//                    }
//
//                    @Override
//                    public void onResult(SHARE_MEDIA share_media) {
//                        Log.e(TAG, "shared success on " + share_media);
//                    }
//
//                    @Override
//                    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
//                        Log.e(TAG, "shared fail on " + share_media, throwable);
//                        Toast.makeText(activity, activity.getString(R.string.toast_share_fail) + " " + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                    }
//
//                    @Override
//                    public void onCancel(SHARE_MEDIA share_media) {
//                        Log.e(TAG, "shared canceled on " + share_media);
//                    }
//                }).open(config);
    }
}
