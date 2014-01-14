package org.kbsriram.android.prw.util;

import android.app.AlarmManager;
import android.content.Context;
import android.app.PendingIntent;
import android.util.Log;
import org.kbsriram.android.prw.service.CBackgroundService;

public class CUtils
{
    private final static boolean IS_PRODUCTION = true;

    public static void uninstallUpdateWidgetAlarm(Context ctx)
    { doWidgetAlarm(ctx, false); }

    public static void installUpdateWidgetAlarm(Context ctx)
    { doWidgetAlarm(ctx, true); }

    public static String makeLogTag(Class cls)
    {
        String str = cls.getSimpleName();
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring
                (0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }
        return LOG_PREFIX + str;
    }

    public static void LOGD(final String tag, String message)
    {
        if (!IS_PRODUCTION || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

    public static void LOGD(final String tag, String message, Throwable cause)
    {
        if (!IS_PRODUCTION || Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message, cause);
        }
    }

    public static void LOGW(final String tag, String message)
    { Log.w(tag, message); }

    public static void LOGW(final String tag, String message, Throwable cause)
    { Log.w(tag, message, cause); }

    private static void doWidgetAlarm(Context ctx, boolean install)
    {
        if (s_widget_alarm_installed == install) { return; }

        AlarmManager am = (AlarmManager)
            ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) { return; }

        PendingIntent pi = PendingIntent.getService
            (ctx, 0, CBackgroundService.makeAsyncUpdateWidgetIntent(ctx),
             PendingIntent.FLAG_UPDATE_CURRENT);

        am.cancel(pi);
        if (install) {
            am.setInexactRepeating
                (AlarmManager.RTC,
                 System.currentTimeMillis() +
                 AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                 AlarmManager.INTERVAL_HALF_HOUR, pi);
        }
        s_widget_alarm_installed = install;
    }

    private static boolean s_widget_alarm_installed = false;
    private static final String LOG_PREFIX = "prw_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;
    private final static String TAG = "prw_CUtils";
}
