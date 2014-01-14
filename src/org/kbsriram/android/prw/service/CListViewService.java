package org.kbsriram.android.prw.service;

// Provides data for the listview in the widget.

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import org.kbsriram.android.prw.R;
import org.kbsriram.android.prw.util.CUtils;

public class CListViewService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent)
    { return new Factory(getApplicationContext(), intent); }

    public final static class Factory
        implements RemoteViewsService.RemoteViewsFactory
    {
        private Factory(Context ctx, Intent intent)
        {
            m_ctx = ctx;
            m_widgetid = intent.getIntExtra
                (AppWidgetManager.EXTRA_APPWIDGET_ID,
                 AppWidgetManager.INVALID_APPWIDGET_ID);
            m_items = asArray(intent.getStringExtra(LIST_ITEMS));
        }

        // These are nops - all data shows up when we're initialized.
        public void onCreate() { }
        public void onDestroy() { }

        // basically boilerplate
        public RemoteViews getLoadingView() { return null; }
        public int getCount() { return m_items.length; }
        public int getViewTypeCount() { return (m_items.length<2?1:2); }
        public long getItemId(int position) { return position; }
        public boolean hasStableIds() { return true; }
        public void onDataSetChanged() { }

        // The useful bit.
        public RemoteViews getViewAt(int pos)
        {
            int lid;
            int tid;
            if (pos == 0) {
                lid = R.layout.widget_main_item;
                tid = R.id.widget_main_item_tv;
            }
            else {
                lid = R.layout.widget_secondary_item;
                tid = R.id.widget_secondary_item_tv;
            }
            RemoteViews rv = new RemoteViews(m_ctx.getPackageName(), lid);
            rv.setTextViewText(tid, m_items[pos]);
            rv.setOnClickFillInIntent(tid, new Intent());
            return rv;
        }

        private final static String[] asArray(String v)
        {
            if (v == null) { return new String[0]; }
            return v.split("\n");
        }

        private final Context m_ctx;
        private final int m_widgetid;
        private final String[] m_items;
        private final static String TAG = CUtils.makeLogTag(Factory.class);
    }
    public final static String LIST_ITEMS =
        "org.kbsriram.android.prw.service.listItems";
}
