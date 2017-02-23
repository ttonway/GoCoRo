package com.wcare.android.gocoro.http;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by ttonway on 2017/2/23.
 */

public interface WebService {

    @GET("knowledge/list")
    Call<List<KnowledgeMessage>> listKnowledgeMessages();
}
