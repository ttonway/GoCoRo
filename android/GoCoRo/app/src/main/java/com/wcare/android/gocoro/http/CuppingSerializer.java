package com.wcare.android.gocoro.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wcare.android.gocoro.model.Cupping;

import java.lang.reflect.Type;

/**
 * Created by ttonway on 2017/4/13.
 */
public class CuppingSerializer implements JsonSerializer<Cupping> {

    @Override
    public JsonElement serialize(Cupping src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uuid", src.getUuid());
        jsonObject.addProperty("name", src.getName());
        jsonObject.addProperty("comment", src.getComment());
        jsonObject.addProperty("time", src.getTime());
        if (src.getProfile() != null) {
            jsonObject.addProperty("profile", src.getProfile().getUuid());
        }
        jsonObject.addProperty("score1", src.getScore1());
        jsonObject.addProperty("score2", src.getScore2());
        jsonObject.addProperty("score3", src.getScore3());
        jsonObject.addProperty("score4", src.getScore4());
        jsonObject.addProperty("score5", src.getScore5());
        jsonObject.addProperty("score6", src.getScore6());
        jsonObject.addProperty("score7", src.getScore7());
        jsonObject.addProperty("score8", src.getScore8());
        jsonObject.addProperty("score9", src.getScore9());
        jsonObject.addProperty("score10", src.getScore10());
        return jsonObject;
    }
}
