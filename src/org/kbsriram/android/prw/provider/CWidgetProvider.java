package org.kbsriram.android.prw.provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.RemoteViews;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.kbsriram.android.prw.R;
import org.kbsriram.android.prw.data.CSimpleLineDatabase;
import org.kbsriram.android.prw.service.CBackgroundService;
import org.kbsriram.android.prw.service.CListViewService;
import org.kbsriram.android.prw.util.CUtils;

public class CWidgetProvider
    extends AppWidgetProvider
{
    @Override
    public void onDisabled(Context ctx)
    { CUtils.uninstallUpdateWidgetAlarm(ctx);  }

    @Override
    public void onUpdate
        (Context ctx, AppWidgetManager mgr, int[] ids)
    {
        CUtils.installUpdateWidgetAlarm(ctx);
        CBackgroundService.asyncUpdateWidget(ctx);
    }

    // This gets called back from the background service, off the
    // main thread.
    public static void updateWidget(Context ctx)
    {
        GregorianCalendar cal = new GregorianCalendar();

        String maxim = getMaxim(ctx, cal);
        String this_day = getThisDayInHistory(ctx, cal);
        String today = DateFormat.format("MMMM dd, ", cal).toString();

        if (this_day != null) {
            this_day = today + " "+this_day;
        }
        updateContent(ctx.getApplicationContext(), maxim, this_day);
    }

    private final static String getMaxim(Context ctx, GregorianCalendar cal)
    {
        // Use the current "day" to pull out a quote.
        long epoch_day = cal.get(Calendar.YEAR)*366l +
            cal.get(Calendar.DAY_OF_YEAR);

        // tmp
        // epoch_day = 69l;

        try {
            return CSimpleLineDatabase.getLine
                (epoch_day, ctx.getAssets().open("maxims.txt"));
        }
        catch (IOException ioe) {
            // Desperation - return a simple quote for now.
            CUtils.LOGD(TAG, "Woops", ioe);
            return "Love your neighbor; yet don't pull down your hedge.";
        }
    }

    private final static String getThisDayInHistory
        (Context ctx, GregorianCalendar cal)
    {
        // Use the current "hour" to pull out a this_day_in_history.
        long week_hour = cal.get(Calendar.WEEK_OF_YEAR)*168l +
            cal.get(Calendar.HOUR_OF_DAY);

        // tmp
        // week_hour = 36l;

        String db_file = "events/"+cal.get(Calendar.MONTH)+"/"
            +cal.get(Calendar.DAY_OF_MONTH)+".txt";

        String ret = null;

        // skip errors, in case my db is incomplete.
        try {
            ret = CSimpleLineDatabase.getLine
                (week_hour, ctx.getAssets().open(db_file));
        }
        catch (IOException ioe) {
            CUtils.LOGD(TAG, "Skip bad: "+db_file, ioe);
        }
        return ret;
    }

    @SuppressWarnings("deprecation")
    private final static void updateContent
        (Context ctx, String maxim, String this_day)
    {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        String data = maxim;
        String share = maxim + "\n- Benjamin Franklin";
        if (this_day != null) {
            data += "\n"+this_day;
            share += "\n\n"+this_day;
        }

        int[] ids = mgr.getAppWidgetIds
            (new ComponentName(ctx, CWidgetProvider.class));
        for (int id : ids) {
            RemoteViews rv =
                new RemoteViews(ctx.getPackageName(), R.layout.widget);

            // Setup the list-adapter for the remote view.
            Intent intent =
                new Intent(ctx, CListViewService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .putExtra(CListViewService.LIST_ITEMS, data);

            // When intents are compared, the extras are ignored, so
            // we need to embed the extras into the data so that the
            // extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // NB: deprecated after api-14; but minsdk is 12.
            rv.setRemoteAdapter(id, R.id.widget_lv, intent);
            // rv.setRemoteAdapter(R.id.widget_lv, intent);

            Intent share_intent = new Intent();
            share_intent.setAction(Intent.ACTION_SEND);
            share_intent.putExtra(Intent.EXTRA_TEXT, share);
            share_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
            share_intent.setType("text/plain");
            share_intent = Intent.createChooser
                (share_intent, ctx.getResources().getText(R.string.share_with));

            PendingIntent pi = PendingIntent.getActivity
                (ctx, 0, share_intent, PendingIntent.FLAG_CANCEL_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_lv, pi);

            mgr.updateAppWidget(id, rv);
        }
    }

    private final static String TAG = CUtils.makeLogTag(CWidgetProvider.class);
}

