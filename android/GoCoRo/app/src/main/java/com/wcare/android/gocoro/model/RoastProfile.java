package com.wcare.android.gocoro.model;

import android.text.TextUtils;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ttonway on 2016/12/19.
 */
public class RoastProfile extends RealmObject {

    @PrimaryKey
    String uuid;

    String people;
    String beanCountry;
    String beanName;
    long startTime;
    long endTime;
    int startWeight;
    int endWeight;
    int envTemperature;

    int startFire;
    int startDruation;// in seconds
    boolean complete;// if roast process completed

    public RealmList<RoastData> plotDatas;

    public String getUuid() {
        return uuid;
    }

    public String getPeople() {
        return people;
    }

    public void setPeople(String people) {
        this.people = people;
    }

    public String getBeanCountry() {
        return beanCountry;
    }

    public void setBeanCountry(String beanCountry) {
        this.beanCountry = beanCountry;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getStartWeight() {
        return startWeight;
    }

    public void setStartWeight(int startWeight) {
        this.startWeight = startWeight;
    }

    public int getEndWeight() {
        return endWeight;
    }

    public void setEndWeight(int endWeight) {
        this.endWeight = endWeight;
    }

    public int getEnvTemperature() {
        return envTemperature;
    }

    public void setEnvTemperature(int envTemperature) {
        this.envTemperature = envTemperature;
    }

    public int getStartFire() {
        return startFire;
    }

    public void setStartFire(int startFire) {
        this.startFire = startFire;
    }

    public int getStartDruation() {
        return startDruation;
    }

    public void setStartDruation(int startDruation) {
        this.startDruation = startDruation;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(getBeanCountry())) {
            sb.append(getBeanCountry()).append(" ");
        }
        sb.append(getBeanName());
        if (!TextUtils.isEmpty(getPeople())) {
            sb.append(" (").append(getPeople()).append(")");
        }
        return sb.toString();
    }

    public static String formatWeightRatio(int startWeight, int endWeight) {
        return startWeight <= 0 ? "-" : String.format("%.2f%%", (1.f - endWeight / (float) startWeight) * 100);
    }
}
