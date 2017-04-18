package com.wcare.android.gocoro.http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.wcare.android.gocoro.model.RoastData;
import com.wcare.android.gocoro.model.RoastProfile;

import java.lang.reflect.Type;

/**
 * Created by ttonway on 2017/4/13.
 */
public class RoastProfileSerializer implements JsonSerializer<RoastProfile> {

    @Override
    public JsonElement serialize(RoastProfile src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uuid", src.getUuid());
        jsonObject.addProperty("deviceId", src.getDeviceId());
        jsonObject.addProperty("people", src.getPeople());
        jsonObject.addProperty("beanCountry", src.getBeanCountry());
        jsonObject.addProperty("beanName", src.getBeanName());
        jsonObject.addProperty("startTime", src.getStartTime());
        jsonObject.addProperty("endTime", src.getEndTime());
        jsonObject.addProperty("startWeight", src.getStartWeight());
        jsonObject.addProperty("endWeight", src.getEndWeight());
        jsonObject.addProperty("envTemperature", src.getEnvTemperature());
        jsonObject.addProperty("startFire", src.getStartFire());
        jsonObject.addProperty("startDruation", src.getStartDruation());
        jsonObject.addProperty("coolTemperature", src.getCoolTemperature());
        jsonObject.addProperty("preHeatTime", src.getPreHeatTime());
        jsonObject.addProperty("roastTime", src.getRoastTime());
        jsonObject.addProperty("coolTime", src.getCoolTime());
        jsonObject.addProperty("complete", src.isComplete());
//        jsonObject.add("plotDatas", context.serialize(src.getPlotDatas(), new TypeToken<List<RoastData>>(){}.getType()));
        // https://gist.github.com/cmelchior/ddac8efd018123a1e53a
        JsonArray plotDatas = new JsonArray();
        for (RoastData data : src.getPlotDatas()) {
            plotDatas.add(context.serialize(data, RoastData.class));
        }
        jsonObject.add("plotDatas", plotDatas);
        return jsonObject;
    }
}
