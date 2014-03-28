package se.deeshu.secondclock;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;

public class ControlActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.clock_preferences, true);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new ClockPreferenceActivity())
				.commit();
	}

	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	 getMenuInflater().inflate(R.menu.control, menu);
	 return true;
	 }

}
