package org.kbsriram.android.prw.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import java.util.concurrent.LinkedBlockingQueue;
import org.kbsriram.android.prw.provider.CWidgetProvider;
import org.kbsriram.android.prw.util.CUtils;

public class CBackgroundService extends IntentService
{
    public CBackgroundService()
    { super("poor-richards-background-service"); }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Special case.
        if (METHOD_UPDATE_WIDGET == intent.getIntExtra(METHOD_NAME, 0)) {
            pushTask(null, new Task() {
                    public void runTask(final Context ctx) {
                        CWidgetProvider.updateWidget(ctx);
                    }
                });
        }

        // run through tasks in the queue, then exit.
        Task task;
        while ((task = s_queue.poll()) != null) {
            runTask(task);
        }
    }

    private void runTask(Task task)
    {
        // Carefully run the task, and log any exceptions.
        try {
            task.runTask(this);
        }
        catch (Throwable th) {
            CUtils.LOGW(TAG, "Failed to run task", th);
        }
    }

    public static void asyncUpdateWidget(Context ctx)
    { ctx.startService(makeAsyncUpdateWidgetIntent(ctx)); }

    public static Intent makeAsyncUpdateWidgetIntent(Context ctx)
    {
        Intent ret = new Intent(ctx, CBackgroundService.class);
        ret.putExtra(METHOD_NAME, METHOD_UPDATE_WIDGET);
        return ret;
    }

    public final static void pushTask(final Context ctx, final Task t)
    {
        try { s_queue.put(t); }
        catch (InterruptedException iex) {
            CUtils.LOGW(TAG, "unable to enqueue", iex);
            return;
        }
        if (ctx != null) {
            ctx.startService(new Intent(ctx, CBackgroundService.class));
        }
    }

    public interface Task
    {
        public void runTask(Context ctx)
            throws Exception;
    }

    private final static LinkedBlockingQueue<Task> s_queue =
        new LinkedBlockingQueue<Task>();

    private final static String METHOD_NAME = "method_name";
    private final static int METHOD_UPDATE_WIDGET = 2;

    private final static String TAG = CUtils.makeLogTag
        (CBackgroundService.class);
}
