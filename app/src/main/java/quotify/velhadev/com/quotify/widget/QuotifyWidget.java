package quotify.velhadev.com.quotify.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import quotify.velhadev.com.quotify.R;
import quotify.velhadev.com.quotify.postUi.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class QuotifyWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Bitmap bitmap) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.quotify_widget);

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            loggedIn(context, views, bitmap);
        }

        else{
            loggedOut(context, views);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static void loggedOut(Context context, RemoteViews views) {
        views.setViewVisibility(R.id.temp_iv, View.VISIBLE);
        views.setViewVisibility(R.id.tv_widget, View.VISIBLE);
        views.setImageViewResource(R.id.widget_iv, R.drawable.auth_screen_background);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.tv_widget, pendingIntent);
    }

    private static void loggedIn(Context context, RemoteViews views, Bitmap bitmap) {

        if(bitmap == null) {
            views.setImageViewResource(R.id.temp_iv, R.drawable.ic_refresh_white_24px);
            views.setTextViewText(R.id.tv_widget, context.getString(R.string.widget_no_data));
            Intent intent = new Intent(context, UpdateQuoteService.class);
            intent.setAction(UpdateQuoteService.ACTION_REFRESH_WIDGET);
            PendingIntent pendingIntent = PendingIntent.getService(context,
                    1,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.temp_iv, pendingIntent);
        }

        else{
            views.setViewVisibility(R.id.temp_iv, View.GONE);
            views.setViewVisibility(R.id.tv_widget, View.GONE);
            views.setImageViewBitmap(R.id.widget_iv, bitmap);
        }
    }

    public static void updateQuotifyWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, Bitmap bitmap){
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, bitmap);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            UpdateQuoteService.refreshWidgetData(context);
    }

    @Override
    public void onEnabled(Context context) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, QuotifyWidget.class));
            updateQuotifyWidget(context,
                    manager,
                    ids,
                    null);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

