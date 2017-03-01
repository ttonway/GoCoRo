package com.wcare.android.gocoro.http;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ttonway on 2017/2/23.
 */

public class ServiceFactory {

    private static WebService sWebService;

    public static synchronized WebService getWebService() {
        if (sWebService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://beta.wcare.cn:3003/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            sWebService = retrofit.create(WebService.class);
        }
        return sWebService;
    }
}
