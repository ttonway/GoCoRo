package com.wcare.android.gocoro.model;

import com.wcare.android.gocoro.R;

import io.realm.RealmObject;

/**
 * Created by ttonway on 2016/12/19.
 */
public class RoastData extends RealmObject {
    public static final int STATUS_UNKNOWN = -1;
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_PREHEATING = 1;
    public static final int STATUS_ROASTING = 2;
    public static final int STATUS_COOLING = 3;

    public static final String EVENT_BURST1_START = "BURST1_START";//一爆開始
    public static final String EVENT_BURST1 = "BURST1";//一爆密集
    public static final String EVENT_BURST2_START = "BURST2_START";//二爆開始
    public static final String EVENT_BURST2 = "BURST2";//二爆密集

    int time;
    int fire;
    int temperature;
    int status;
    String event;

    boolean manualCool;//冷却设置
    boolean coolStatusComplete;//冷却停止

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getFire() {
        return fire;
    }

    public void setFire(int fire) {
        this.fire = fire;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public boolean isManualCool() {
        return manualCool;
    }

    public void setManualCool(boolean manualCool) {
        this.manualCool = manualCool;
    }

    public boolean isCoolStatusComplete() {
        return coolStatusComplete;
    }

    public void setCoolStatusComplete(boolean coolStatusComplete) {
        this.coolStatusComplete = coolStatusComplete;
    }

    public int getEventNameResId() {
        if (EVENT_BURST1_START.equals(event)) {
            return R.string.event_burst1_start;
        } else if (EVENT_BURST1.equals(event)) {
            return R.string.event_burst1;
        } else if (EVENT_BURST2_START.equals(event)) {
            return R.string.event_burst2_start;
        } else if (EVENT_BURST2.equals(event)) {
            return R.string.event_burst2;
        }
        return 0;
    }
}
