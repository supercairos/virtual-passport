package io.romain.passport.logic.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

import java.util.Set;


public class SharedPrefHelper {

	private SharedPreferences mPrefs;

	public SharedPrefHelper(Context context) {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return mPrefs.getBoolean(key, defValue);
	}

	public void putBoolean(String key, boolean value) {
		mPrefs.edit()
				.putBoolean(key, value)
				.apply();
	}

	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}

	public void putInt(String key, int value) {
		mPrefs.edit()
				.putInt(key, value)
				.apply();
	}

	public long getLong(String key, long defValue) {
		return mPrefs.getLong(key, defValue);
	}

	public void putLong(String key, long value) {
		mPrefs.edit()
				.putLong(key, value)
				.apply();
	}

	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}

	public void putString(String key, String value) {
		mPrefs.edit()
				.putString(key, value)
				.apply();
	}

	public Set<String> getStringSet(String key, Set<String> defaultValue) {
		return mPrefs.getStringSet(key, defaultValue);
	}

	public void putStringSet(String key, Set<String> values) {
		mPrefs.edit()
				.putStringSet(key, values)
				.apply();
	}

	public void remove(String key) {
		mPrefs.edit()
				.remove(key)
				.apply();
	}

	public void registerOnPrefChangeListener(OnSharedPreferenceChangeListener listener) {
		mPrefs.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterOnPrefChangeListener(OnSharedPreferenceChangeListener listener) {
		mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
	}
}

