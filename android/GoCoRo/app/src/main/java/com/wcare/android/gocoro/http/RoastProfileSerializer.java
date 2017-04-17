package com.wcare.android.gocoro.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wcare.android.gocoro.model.RoastData;

import java.lang.reflect.Type;

/**
 * Created by ttonway on 2017/4/13.
 */
public class RoastProfileSerializer implements JsonSerializer<RoastData> {

    @Override
    public JsonElement serialize(RoastData src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("time", src.getTime());
        jsonObject.addProperty("fire", src.getFire());
        jsonObject.addProperty("temperature", src.getTemperature());
        jsonObject.addProperty("status", src.getStatus());
        jsonObject.addProperty("event", src.getEvent());
        jsonObject.addProperty("manualCool", src.isManualCool());
        return jsonObject;
    }
}
