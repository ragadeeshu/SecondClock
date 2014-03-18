package se.deeshu.secondclock;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
import android.widget.Toast;

public class SecondClockAppWidgetProvider extends AppWidgetProvider {
	private static DateFormat shortdf = new SimpleDateFormat("HH:mm");
	private static DateFormat longdf = new SimpleDateFormat("HH:mm:ss");;
	private static final String LOG_TAG = "SecondClockWidget";
	public static String CLOCK_WIDGET_UPDATE = "se.deeshu.secondclock.SECONDCLOCK_WIDGET_UPDATE";
	public static String CLICKED_CLOCK_ACTION = "Clicked";
	static Timer timer = null;

	// private PendingIntent createClockTickIntent(Context context) {
	// Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
	// PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
	// intent, PendingIntent.FLAG_UPDATE_CURRENT);
	// return pendingIntent;
	// }

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Log.d(LOG_TAG,
				"Widget Provider enabled.  Starting timer to update widget every second");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 1);
		cal.set(Calendar.MILLISECOND, 0);
		timer.scheduleAtFixedRate(new ClockTimerTask(context, this),
				cal.getTime(), 1000);

	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.d(LOG_TAG, "Widget Provider disabled. Turning off timer");
		timer.cancel();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		// Log.d(LOG_TAG, "Received intent " + intent);

		if (intent.getAction().equals(CLICKED_CLOCK_ACTION)) {
			openClock(context, intent);
		}
	}

	private void updateClock(Context context) {
		// Log.d(LOG_TAG, "Clock update");

		ComponentName thisAppWidget = new ComponentName(
				context.getPackageName(), getClass().getName());
		AppWidgetManager appWidgetManager = AppWidgetManager
				.getInstance(context);
		int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);

		final int N = ids.length;

		// Log.i("SecondClockWidget",
		// "Updating widgets " + Arrays.asList(appWidgetIds));

		for (int i = 0; i < N; i++) {
			int appWidgetId = ids[i];

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.clock_layout);

			Intent intent = new Intent(context,
					SecondClockAppWidgetProvider.class);
			intent.setAction(CLICKED_CLOCK_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, 0);

			views.setOnClickPendingIntent(R.id.widget1label, pendingIntent);

			if (getDateformat(appWidgetManager, appWidgetId))
				views.setTextViewText(R.id.widget1label,
						shortdf.format(new Date()));
			else
				views.setTextViewText(R.id.widget1label,
						longdf.format(new Date()));

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}

	}

	private void openClock(Context context, Intent intent) {

		Intent openClockIntent = new Intent(AlarmClock.ACTION_SET_ALARM);
		openClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(openClockIntent);

	}

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int N = appWidgetIds.length;
		if (timer == null) {
			timer = new Timer();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, 1);
			cal.set(Calendar.MILLISECOND, 0);
			timer.scheduleAtFixedRate(new ClockTimerTask(context, this),
					cal.getTime(), 1000);

		}

		// Log.i("SecondClockWidget",
		// "Updating widgets " + Arrays.asList(appWidgetIds));

		for (int i = 0; i < N; i++) {

			Intent intent = new Intent(context,
					SecondClockAppWidgetProvider.class);
			intent.setAction(CLICKED_CLOCK_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
					0, intent, 0);

			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.clock_layout);
			views.setOnClickPendingIntent(R.id.widget1label, pendingIntent);

		}
	}

	public void onDeleted(Context context, int[] appWidgetIds) {
		timer.cancel();
		super.onDeleted(context, appWidgetIds);
	}

	public static void changeTextSize(Context context, int size) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.clock_layout);
		remoteViews.setFloat(R.id.widget1label, "setTextSize", size);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		ComponentName thisAppWidget = new ComponentName(context,
				SecondClockAppWidgetProvider.class);
		manager.updateAppWidget(thisAppWidget, remoteViews);
	}

	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {

		Log.d(LOG_TAG, "Changed dimensions");

		updateClock(context);
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
				newOptions);
	}

	private boolean getDateformat(AppWidgetManager appWidgetManager,
			int appWidgetId) {
		Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
		int minWidth = options
				.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

		int columns = getCellsForSize(minWidth);
		return (columns == 2);

	}

	private static int getCellsForSize(int size) {
		int n = 2;
		while (70 * n - 30 < size) {
			++n;
		}
		return n - 1;
	}

	private class ClockTimerTask extends TimerTask {
		AppWidgetManager appWidgetManager;
		SecondClockAppWidgetProvider parent;
		Context context;

		public ClockTimerTask(Context context,
				SecondClockAppWidgetProvider parent) {
			this.parent = parent;
			this.context = context;
		}

		@Override
		public void run() {

			parent.updateClock(context);

		}
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