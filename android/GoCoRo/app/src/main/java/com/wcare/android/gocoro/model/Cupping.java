package com.wcare.android.gocoro.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by ttonway on 2016/12/19.
 */
public class Cupping extends RealmObject {

    @PrimaryKey
    String uuid;

    String name;
    String comment;
    long time;

    RoastProfile profile;

    float score1;
    float score2;
    float score3;
    float score4;
    float score5;
    float score6;
    float score7;
    float score8;
    float score9;
    float score10;

    // sync with server
    boolean dirty;
    int sid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public RoastProfile getProfile() {
        return profile;
    }

    public void setProfile(RoastProfile profile) {
        this.profile = profile;
    }

    public float getScore1() {
        return score1;
    }

    public void setScore1(float score1) {
        this.score1 = score1;
    }

    public float getScore2() {
        return score2;
    }

    public void setScore2(float score2) {
        this.score2 = score2;
    }

    public float getScore3() {
        return score3;
    }

    public void setScore3(float score3) {
        this.score3 = score3;
    }

    public float getScore4() {
        return score4;
    }

    public void setScore4(float score4) {
        this.score4 = score4;
    }

    public float getScore5() {
        return score5;
    }

    public void setScore5(float score5) {
        this.score5 = score5;
    }

    public float getScore6() {
        return score6;
    }

    public void setScore6(float score6) {
        this.score6 = score6;
    }

    public float getScore7() {
        return score7;
    }

    public void setScore7(float score7) {
        this.score7 = score7;
    }

    public float getScore8() {
        return score8;
    }

    public void setScore8(float score8) {
        this.score8 = score8;
    }

    public float getScore9() {
        return score9;
    }

    public void setScore9(float score9) {
        this.score9 = score9;
    }

    public float getScore10() {
        return score10;
    }

    public void setScore10(float score10) {
        this.score10 = score10;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public float getTotalScore() {
        return score1 + score2 + score3 + score4 + score5 +
                score6 + score7 + score8 + score9 + score10;
    }
}
