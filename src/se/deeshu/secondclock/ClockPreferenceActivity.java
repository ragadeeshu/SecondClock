package se.deeshu.secondclock;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

public class ClockPreferenceActivity extends PreferenceFragment implements
		OnSharedPreferenceChangeListener {
	public static final String KEY_PREF_SIZE = "pref_size";
	public static final String KEY_PREF_BACKGROUND = "pref_background";

	// private static final Object KEY_PREF_REALLY_SMALL = "pref_really_small";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.clock_preferences);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("SecondClockWidget", "Setting " + key + " changed");
		if (key.equals(KEY_PREF_SIZE)) {
			SecondClockAppWidgetProvider.changeTextSize(getActivity()
					.getApplicationContext(), Integer
					.parseInt(sharedPreferences.getString(key, "55")));
		} else if (key.equals(KEY_PREF_BACKGROUND)) {
			SecondClockAppWidgetProvider.changeBackground(getActivity()
					.getApplicationContext(), sharedPreferences.getBoolean(key,
					false));
		}

	}
}
