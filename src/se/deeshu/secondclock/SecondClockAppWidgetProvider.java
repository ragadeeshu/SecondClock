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
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.widget.RemoteViews;

public class SecondClockAppWidgetProvider extends AppWidgetProvider {
	static DateFormat df = new SimpleDateFormat("HH:mm:ss");
	private static final String LOG_TAG = "SecondClockWidget";
	public static String CLOCK_WIDGET_UPDATE = "se.deeshu.secondclock.SECONDCLOCK_WIDGET_UPDATE";
	public static String CLICKED_CLOCK_ACTION = "Clicked";

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
		// Log.d(LOG_TAG, "Received intent " + intent);

		if (intent.getAction().equals(CLICKED_CLOCK_ACTION)) {
			openClock(context, intent);
		}

		if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
			updateClock(context);
		}
	}

	private void updateClock(Context context) {
		// Log.d(LOG_TAG, "Clock update");

		ComponentName thisAppWidget = new ComponentName(
				context.getPackageName(), getClass().getName());
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);

		onUpdate(context, appWidgetManager, ids);

	}

	private void openClock(Context context, Intent intent) {

		Intent openClockIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
		openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(openClockIntent);

	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		// Log.i("SecondClockWidget",
		// "Updating widgets " + Arrays.asList(appWidgetIds));

		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];

			Intent intent = new Intent(context,
					SecondClockAppWidgetProvider.class);
			intent.setAction(CLICKED_CLOCK_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, 0);

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.clock_layout);
			views.setOnClickPendingIntent(R.id.widget1label, pendingIntent);

			views.setTextViewText(R.id.widget1label, df.format(new Date()));

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	public static void changeTextSize(Context context, int size) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.clock_layout);
		remoteViews.setFloat(R.id.widget1label, "setTextSize", size);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context,
				SecondClockAppWidgetProvider.class);
		// int ids[] = manager.getAppWidgetIds(thisAppWidget);
		manager.updateAppWidget(thisAppWidget, remoteViews);
	}

	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {

		Log.d(LOG_TAG, "Changed dimensions");

		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		int minWidth = options
				.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

		int columns = getCellsForSize(minWidth);
		if (columns == 2) {
			df = new SimpleDateFormat("HH:mm");
		} else {
			df = new SimpleDateFormat("HH:mm:ss");
		}
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);
	}

	private static int getCellsForSize(int size) {
		int n = 2;
		while (70 * n - 30 < size) {
			++n;
		}
		return n - 1;
	}

	// public static void updateAppWidget(Context context,
	// AppWidgetManager appWidgetManager, int appWidgetId) {
	// String currentTime = df.format(new Date());
	//
	// RemoteViews updateViews = new RemoteViews(context.getPackageName(),
	// R.layout.clock_layout);
	//
	// updateViews.setTextViewText(R.id.widget1label, currentTime);
	// appWidgetManager.updateAppWidget(appWidgetId, updateViews);
	// }
}