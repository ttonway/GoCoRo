package com.wcare.android.gocoro.http;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wcare.android.gocoro.BuildConfig;
import com.wcare.android.gocoro.Constants;
import com.wcare.android.gocoro.model.Cupping;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;

import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ttonway on 2017/2/23.
 */
public class ServiceFactory {

    private static WebService sWebService;

    public static synchronized WebService getWebService() {
        if (sWebService == null) {

                Gson gson = new GsonBuilder()
                        .setExclusionStrategies(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                return f.getDeclaringClass().equals(RealmObject.class);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        })
//                        .registerTypeAdapter(Class.forName("io.realm.CuppingRealmProxy"), new CuppingSerializer())
//                        .registerTypeAdapter(Class.forName("io.realm.RoastDataRealmProxy"), new RoastDataSerializer())
//                        .registerTypeAdapter(Class.forName("io.realm.RoastProfileRealmProxy"), new RoastProfileSerializer())
                        .registerTypeAdapter(Cupping.class, new CuppingSerializer())
                        .registerTypeAdapter(RoastData.class, new RoastDataSerializer())
                        .registerTypeAdapter(RoastProfile.class, new RoastProfileSerializer())
                        .create();


            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
            httpClientBuilder.writeTimeout(20, TimeUnit.SECONDS);
            httpClientBuilder.readTimeout(100, TimeUnit.SECONDS);
            if (BuildConfig.DEBUG) {
                // enable logging for debug builds
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClientBuilder.addInterceptor(loggingInterceptor);
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.WEB_HOST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .callFactory(httpClientBuilder.build())
                    .build();

            sWebService = retrofit.create(WebService.class);
        }
        return sWebService;
    }
}
