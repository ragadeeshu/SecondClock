package se.deeshu.secondclock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.RemoteViews;

public class SecondClockAppWidgetProvider extends AppWidgetProvider {
	static DateFormat df = new SimpleDateFormat("HH:mm:ss");
	private static final String LOG_TAG = "SecondClockWidget";
	public static String CLOCK_WIDGET_UPDATE = "se.deeshu.secondclock.SECONDCLOCK_WIDGET_UPDATE";
	public static String YOUR_AWESOME_ACTION = "YourAwesomeAction";

	private PendingIntent createClockTickIntent(Context context) {
		Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d(LOG_TAG,
				"Widget Provider enabled.  Starting timer to update widget every second");
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 1);
		alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
				1000, createClockTickIntent(context));

	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d(LOG_TAG, "Widget Provider disabled. Turning off timer");
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(createClockTickIntent(context));
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		Log.d(LOG_TAG, "Received intent " + intent);

		if (intent.getAction().equals(YOUR_AWESOME_ACTION)) {
			System.out.println();
			PackageManager packageManager = context.getPackageManager();
			Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN)
					.addCategory(Intent.CATEGORY_LAUNCHER);

			// Verify clock implementation
			String clockImpls[][] = {
					{ "HTC Alarm Clock", "com.htc.android.worldclock",
							"com.htc.android.worldclock.WorldClockTabControl" },
					{ "Standar Alarm Clock", "com.android.deskclock",
							"com.android.deskclock.AlarmClock" },
					{ "Froyo Nexus Alarm Clock",
							"com.google.android.deskclock",
							"com.android.deskclock.DeskClock" },
					{ "Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",
							"com.motorola.blur.alarmclock.AlarmClock" },
					{ "Samsung Galaxy Clock",
							"com.sec.android.app.clockpackage",
							"com.sec.android.app.clockpackage.ClockPackage" } };

			boolean foundClockImpl = false;

			for (int i = 0; i < clockImpls.length; i++) {
				String vendor = clockImpls[i][0];
				String packageName = clockImpls[i][1];
				String className = clockImpls[i][2];
				try {
					ComponentName cn = new ComponentName(packageName, className);
					ActivityInfo aInfo = packageManager.getActivityInfo(cn,
							PackageManager.GET_META_DATA);
					alarmClockIntent.setComponent(cn);
					Log.d(LOG_TAG, "Found " + vendor + " --> " + packageName
							+ "/" + className);
					foundClockImpl = true;
				} catch (NameNotFoundException e) {
					Log.d(LOG_TAG,vendor + " does not exists");
				}
			}

			if (foundClockImpl) {
				PendingIntent pendingIntent = PendingIntent.getActivity(
						context, 0, alarmClockIntent, 0);
				// add pending intent to your component
				// ....
				RemoteViews views = new RemoteViews(context.getPackageName(),
						R.layout.clock_layout);
				views.setOnClickPendingIntent(R.id.widget1label, pendingIntent);

				AppWidgetManager
						.getInstance(context)
						.updateAppWidget(
								intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS),
								views);
			}
		}

		if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
			Log.d(LOG_TAG, "Clock update");
			// Get the widget manager and ids for this widget provider, then
			// call the shared
			// clock update method.
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(), getClass().getName());
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
			for (int appWidgetID : ids) {
				updateAppWidget(context, appWidgetManager, appWidgetID);
			}
		}
	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		Log.i("ExampleWidget",
				"Updating widgets " + Arrays.asList(appWidgetIds));

		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Create an Intent to launch ExampleActivity
			// Intent intent = new Intent(context, WidgetActivity.class);
			// // PendingIntent pendingIntent =
			// PendingIntent.getActivity(context,
			// // 0, intent, 0);
			Intent intent = new Intent(context,
					SecondClockAppWidgetProvider.class);
			intent.setAction(YOUR_AWESOME_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, 0);

			// Get the layout for the App Widget and attach an on-click listener
			// to the button
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.clock_layout);
			views.setOnClickPendingIntent(R.id.widget1label, pendingIntent);

			// To update a label
			views.setTextViewText(R.id.widget1label, df.format(new Date()));

			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	public static void updateAppWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId) {
		String currentTime = df.format(new Date());

		RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.clock_layout);

		updateViews.setTextViewText(R.id.widget1label, currentTime);
		appWidgetManager.updateAppWidget(appWidgetId, updateViews);
	}
}