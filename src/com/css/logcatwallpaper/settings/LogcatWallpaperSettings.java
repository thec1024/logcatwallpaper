package com.css.logcatwallpaper.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import com.css.logcatwallpaper.R;

public class LogcatWallpaperSettings extends PreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.logcat_wallpaper_settings);
		
		Preference pollDelayPref = getPreferenceScreen().findPreference(getString(R.string.key_poll_delay_param));
		pollDelayPref.setOnPreferenceChangeListener(getDelayPreferenceChangeListener());
		
		Preference drawDelayPref = getPreferenceScreen().findPreference(getString(R.string.key_draw_delay_param));
		drawDelayPref.setOnPreferenceChangeListener(getDelayPreferenceChangeListener());
		
		Preference vColor = getPreferenceScreen().findPreference(getString(R.string.key_verbose_color));
		vColor.setOnPreferenceChangeListener(getColorPreferenceChangeListener());
		Preference dColor = getPreferenceScreen().findPreference(getString(R.string.key_debug_color));
		dColor.setOnPreferenceChangeListener(getColorPreferenceChangeListener());
		Preference iColor = getPreferenceScreen().findPreference(getString(R.string.key_info_color));
		iColor.setOnPreferenceChangeListener(getColorPreferenceChangeListener());
		Preference wColor = getPreferenceScreen().findPreference(getString(R.string.key_warning_color));
		wColor.setOnPreferenceChangeListener(getColorPreferenceChangeListener());
		Preference eColor = getPreferenceScreen().findPreference(getString(R.string.key_error_color));
		eColor.setOnPreferenceChangeListener(getColorPreferenceChangeListener());
		Preference fColor = getPreferenceScreen().findPreference(getString(R.string.key_fatal_color));
		fColor.setOnPreferenceChangeListener(getColorPreferenceChangeListener());
		//Preference filterText = getPreferenceScreen().findPreference(getString(R.string.key_filter_param));
	}
	
	private OnPreferenceChangeListener getColorPreferenceChangeListener() {
		return new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if(newValue instanceof String && ((String) newValue).startsWith("#"))
					return true;
				return false;
			}
		};
	}
	
	private OnPreferenceChangeListener getDelayPreferenceChangeListener() {
		return new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Integer value = Integer.parseInt((String)newValue);
				if(value.intValue() >= 50 && value.intValue() <= 60000)
					return true;
				return false;
			}
		};
	}
}
