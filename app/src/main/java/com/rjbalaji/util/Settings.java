package com.rjbalaji.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {

	private String SELECTED_PAYOUT = "SelectedPayout";

	private SharedPreferences pref;
	private static Settings store;
	private static String TYPE = "Type";
	public static String MUSIC_ALBUM = "MusicAlbum";

	public static Settings getInstance(Context context) {
		if (store == null)
			store = new Settings(context);
		return store;
	}

	// private Context context;
	private Settings(Context context) {
		pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public String toString() {
		Map<String, ?> map = pref.getAll();
		Iterator<String> itr = map.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();

		}
		return super.toString();
	}

	public String getData(String key) {
		return pref.getString(key, "");
	}

	public void setData(String value, String key) {
		pref.edit().putString(key, value).commit();
	}

	public int getType() {
		return pref.getInt(TYPE, 0);
	}

	public void setType(int type) {
		pref.edit().putInt(TYPE, type).commit();
	}
	
	public String getAlbumName() {
		return pref.getString("albumName", "");
	}

	public void setAlbumName(String albumName) {
		pref.edit().putString("albumName", albumName).commit();
	}

}
