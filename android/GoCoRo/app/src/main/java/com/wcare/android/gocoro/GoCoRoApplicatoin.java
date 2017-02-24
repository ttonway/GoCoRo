package com.wcare.android.gocoro;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.pgyersdk.crash.PgyCrashManager;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;
import com.wcare.android.gocoro.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by ttonway on 2016/12/19.
 */
public class GoCoRoApplicatoin extends Application {
    private static final String TAG = GoCoRoApplicatoin.class.getSimpleName();

    //各个平台的配置，建议放在全局Application或者程序入口
    {
        PlatformConfig.setWeixin("wxdc1e388c3822c80b", "3baf1193c85774b3fd9d18447d76cab0");
        //豆瓣RENREN平台目前只能在服务器端配置
        PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad","http://sns.whalecloud.com");
        PlatformConfig.setQQZone("1105931595", "EQV0iukAzQqWgyR3");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate at " + System.currentTimeMillis());

        if (!BuildConfig.DEBUG) {
            PgyCrashManager.register(this);
        }

        saveLogcatToFile(this);

        Log.i(TAG, "App Version: " + Utils.getAppVersion(this) + "(" + Utils.getAppVersionCode(this) + ")");
        Log.i(TAG, "BuildConfig: {applicationId=" + BuildConfig.APPLICATION_ID + ", buildType=" + BuildConfig.BUILD_TYPE + ", flavor=" + BuildConfig.FLAVOR
                + ", debug=" + BuildConfig.DEBUG + ", versionName=" + BuildConfig.VERSION_NAME + ", versionCode=" + BuildConfig.VERSION_CODE + "}");
        Log.i(TAG, "System: {Model=" + Build.MODEL + ", version.sdk=" + Build.VERSION.SDK + ", version.release=" + Build.VERSION.RELEASE + "}");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        Log.i(TAG, "DisplayMetrics: " + metrics);

        UMShareAPI.get(this);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        if (BuildConfig.DEBUG) {
            initMockData();
        }
    }

    public static void saveLogcatToFile(Context context) {
        String fileName = "logcat_" + System.currentTimeMillis() + ".txt";
        File outputFile = new File(context.getExternalCacheDir(), fileName);
        Log.i(TAG, "save logcat to " + outputFile.getAbsolutePath());
        try {
            @SuppressWarnings("unused")
            Process process = Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "saveLogcatToFile fail.", e);
        }
    }

    void initMockData() {
        Realm realm = Realm.getDefaultInstance();

        final RealmResults<RoastProfile> profiles = realm.where(RoastProfile.class).equalTo("uuid", "test").findAll();
        if (profiles.size() == 0) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RoastProfile profile = realm.createObject(RoastProfile.class, "test");
                    profile.setPeople("ppp");
                    profile.setBeanCountry("巴拿马");
                    profile.setBeanName("蓝山");
                    profile.setStartTime(System.currentTimeMillis());
                    profile.setEndTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30));
                    profile.setStartWeight(500);
                    profile.setEndWeight(50);
                    profile.setEnvTemperature(27);

                    profile.setStartFire(3);
                    profile.setStartDruation(30 * 60);

                    profile.setPreHeatTime(0);
                    profile.setRoastTime(3 * 60);
                    profile.setCoolTime(15 * 60);
                    for (int i = 0; i < 30 * 60; i++) {
                        RoastData data = realm.createObject(RoastData.class);
                        data.setTime(i);
                        if (i < 3 * 60) {
                            data.setStatus(RoastData.STATUS_PREHEATING);
                        } else if (i < 15 * 60) {
                            data.setStatus(RoastData.STATUS_ROASTING);
                            data.setFire(i < 10 * 60 ? 3 : 5);
                        } else {
                            data.setStatus(RoastData.STATUS_COOLING);
                        }

                        if (i == 5 * 60) {
                            data.setEvent(RoastData.EVENT_BURST1_START);
                        } else if (i == 10 * 60) {
                            data.setEvent(RoastData.EVENT_BURST1);
                        } else if (i == 15 * 60) {
                            data.setEvent(RoastData.EVENT_BURST2_START);
                        } else if (i == 20 * 60) {
                            data.setEvent(RoastData.EVENT_BURST2);
                        }
                        data.setCoolStatusComplete(i == 30 * 60 - 1);

                        data.setTemperature((int) (-230.f / 900 / 900 * i * i + 230.f * 2 / 900 * i + 0.5f));

                        profile.plotDatas.add(data);

                        realm.copyToRealmOrUpdate(profile);
                    }

                }
            });
        }
    }
}
