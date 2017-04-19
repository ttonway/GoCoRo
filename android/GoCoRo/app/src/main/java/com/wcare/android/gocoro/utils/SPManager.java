package com.wcare.android.gocoro.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPManager {
    public static final String KEY_USER_APK_DOWNLOAD_ID = "app.apk_download_id";

	private static final String SP_NAME = "gocoro";
	
	public static boolean contains(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		return sp.contains(key);
	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putString(key, value).apply();
	}

	public static String getString(Context context, String key,
			String defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getString(key, defaultValue);
	}

	public static boolean getBoolean(Context context, String key,
			boolean defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getBoolean(key, defaultValue);
	}

	public static void setBoolean(Context context, String key, boolean value) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putBoolean(key, value).apply();
	}

	public static long getLong(Context context, String key, long defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getLong(key, defaultValue);
	}

	public static void setLong(Context context, String key, long value) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putLong(key, value).apply();
	}
	
	public static float getFloat(Context context, String key, float defaultValue) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		return sp.getFloat(key, defaultValue);
	}
	
	public static void setFloat(Context context, String key, float value) {
		SharedPreferences sp = context.getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putFloat(key, value).apply();
	}
}
