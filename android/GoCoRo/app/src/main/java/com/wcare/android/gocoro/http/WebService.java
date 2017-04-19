package com.wcare.android.gocoro.http;

import com.wcare.android.gocoro.model.Cupping;
import com.wcare.android.gocoro.model.RoastProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by ttonway on 2017/2/23.
 */

public interface WebService {

    @GET("android_version.json")
    Call<AppVersion> queryAndroidVerion();

    @GET("knowledge/list")
    Call<List<KnowledgeMessage>> listKnowledgeMessages();

    @POST("profile/upload")
    Call<RemoteModel> uploadProfile(@Body RoastProfile profile);

    @POST("cupping/upload")
    Call<RemoteModel> uploadCupping(@Body Cupping cupping);
}
