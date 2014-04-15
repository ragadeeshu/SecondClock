package se.deeshu.secondclock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class SecondClockAppWidgetProvider extends AppWidgetProvider {
	private static DateFormat shortdf = new SimpleDateFormat("HH:mm");
	private static DateFormat longdf = new SimpleDateFormat("HH:mm:ss");
	private static DateFormat date = new SimpleDateFormat("MMM-dd");
	private static int textSize = 55;
	private static boolean backgroundIsHidden = false;
	private static final String LOG_TAG = "SecondClockWidget";
	public static String CLOCK_WIDGET_UPDATE = "se.deeshu.secondclock.SECONDCLOCK_WIDGET_UPDATE";
	public static String CLICKED_CLOCK_ACTION = "Clicked";

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
		calendar.set(Calendar.MILLISECOND, 0);
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

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		changeTextSize(context, Integer.parseInt(sharedPreferences.getString(
				ClockPreferenceActivity.KEY_PREF_SIZE, "55")));

		changeBackground(context, sharedPreferences.getBoolean(
				ClockPreferenceActivity.KEY_PREF_BACKGROUND, false));

		// updateClock(context);

		for (int i : appWidgetIds) {
			Intent intent = new Intent(context,
					SecondClockAppWidgetProvider.class);
			intent.setAction(CLICKED_CLOCK_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, 0);

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.clock_layout);
			views.setOnClickPendingIntent(R.id.widgetclocktext, pendingIntent);
			String time;
			if (smallTime(appWidgetManager, i))
				time = shortdf.format(new Date());
			else
				time = longdf.format(new Date());
			if (includeDate(appWidgetManager, i)) {
				views.setTextViewText(R.id.widgetdatetext,
						date.format(new Date()));
				views.setViewVisibility(R.id.widgetdatetext, View.VISIBLE);
			} else
				views.setViewVisibility(R.id.widgetdatetext, View.GONE);
			views.setTextViewText(R.id.widgetclocktext, time);

			appWidgetManager.updateAppWidget(i, views);

		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		// Log.d(LOG_TAG, "Received intent " + intent);

		if (intent.getAction().equals(CLICKED_CLOCK_ACTION)) {
			openAlarm(context, intent);
		}

		if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(context);
			ComponentName thisAppWidget = new ComponentName(
					context.getPackageName(), getClass().getName());
			int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
			onUpdate(context, appWidgetManager, ids);
		}
	}

	public static void changeTextSize(Context context, int size) {
		textSize = size;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.clock_layout);
		remoteViews.setFloat(R.id.widgetclocktext, "setTextSize", size);
		remoteViews.setFloat(R.id.widgetdatetext, "setTextSize", size - 10);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context,
				SecondClockAppWidgetProvider.class);
		manager.updateAppWidget(thisAppWidget, remoteViews);
	}

	public static void changeBackground(Context context, boolean hidden) {
		backgroundIsHidden = hidden;
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.clock_layout);
		if (hidden)
			remoteViews.setInt(R.id.widgetlayout, "setBackgroundResource", 0);
		else
			remoteViews.setInt(R.id.widgetlayout, "setBackgroundResource",
					R.drawable.background);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context,
				SecondClockAppWidgetProvider.class);
		manager.updateAppWidget(thisAppWidget, remoteViews);
	}

	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {

		Log.d(LOG_TAG, "Changed dimensions");
		// updateClock(context);
		int[] id = { appWidgetId };
		onUpdate(context, appWidgetManager, id);
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);
	}

	// private void updateClock(Context context) {
	// // Log.d(LOG_TAG, "Clock update");
	//
	// ComponentName thisAppWidget = new ComponentName(
	// context.getPackageName(), getClass().getName());
	// AppWidgetManager appWidgetManager = AppWidgetManager
	// .getInstance(context);
	// int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
	//
	// final int N = ids.length;
	//
	// // Log.i("SecondClockWidget",
	// // "Updating widgets " + Arrays.asList(appWidgetIds));
	//
	// for (int i = 0; i < N; i++) {
	// int appWidgetId = ids[i];
	//
	// RemoteViews views = new RemoteViews(context.getPackageName(),
	// R.layout.clock_layout);
	//
	// Intent intent = new Intent(context,
	// SecondClockAppWidgetProvider.class);
	// intent.setAction(CLICKED_CLOCK_ACTION);
	// PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	// 0, intent, 0);
	//
	// views.setOnClickPendingIntent(R.id.widgetclocktext, pendingIntent);
	// String time;
	// if (smallTime(appWidgetManager, appWidgetId))
	// time = shortdf.format(new Date());
	// else
	// time = longdf.format(new Date());
	// if (includeDate(appWidgetManager, appWidgetId)) {
	// views.setTextViewText(R.id.widgetdatetext,
	// date.format(new Date()));
	// views.setViewVisibility(R.id.widgetdatetext, View.VISIBLE);
	// } else
	// views.setViewVisibility(R.id.widgetdatetext, View.GONE);
	// views.setTextViewText(R.id.widgetclocktext, time);
	//
	// appWidgetManager.updateAppWidget(appWidgetId, views);
	// }
	//
	// }

	private PendingIntent createClockTickIntent(Context context) {
		Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	private void openAlarm(Context context, Intent intent) {

		Intent openClockIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
		openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(openClockIntent);

	}

	private boolean smallTime(AppWidgetManager appWidgetManager, int appWidgetId) {
		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		int minWidth = options
				.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

		int columns = getCellsForSize(minWidth);
		return (columns == 2);

	}

	private boolean includeDate(AppWidgetManager appWidgetManager,
			int appWidgetId) {
		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		int minHeight = options
				.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

		int rows = getCellsForSize(minHeight);
		return (rows == 2);

	}

	private static int getCellsForSize(int size) {
		int n = 2;
		while (70 * n - 30 < size) {
			++n;
		}
		return n - 1;
	}

}