package quotify.velhadev.com.quotify.widget;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;

import quotify.velhadev.com.quotify.postUi.Post;


/**
 * Created by abhishek on 24/03/18.
 */

public class UpdateQuoteService extends IntentService {

    public static String ACTION_REFRESH_WIDGET = "quotify.velhadev.com.quotify.refresh_widget";

    private Post post = null;

    public UpdateQuoteService() {
        super("UpdateQuoteService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        if (action.equals(ACTION_REFRESH_WIDGET))
            startRefreshing();
    }

    public static void refreshWidgetData(Context context) {
        Intent intent = new Intent(context, UpdateQuoteService.class);
        intent.setAction(ACTION_REFRESH_WIDGET);
        context.startService(intent);
    }

    private void startRefreshing() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("all_posts");
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int postCount = (int) dataSnapshot.getChildrenCount();
                if (postCount > 0) {
                    int randomNumber = new Random().nextInt(postCount);
                    Iterator itr = dataSnapshot.getChildren().iterator();

                    for (int i = 0; i < randomNumber; i++) {
                        itr.next();
                    }
                    DataSnapshot childSnapshot = (DataSnapshot) itr.next();
                    post = childSnapshot.getValue(Post.class);
                }

                Context context = getApplicationContext();
                final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, QuotifyWidget.class));

                @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        if (post == null) return null;
                        return getBitmap(post.getPostUrl());
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        QuotifyWidget.updateQuotifyWidget(getApplicationContext(), appWidgetManager, appWidgetIds, (Bitmap) o);
                    }
                };
                asyncTask.execute();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Bitmap getBitmap(String url) {
        URL imageUrl = null;
        try {
            imageUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (imageUrl == null) return null;

        HttpURLConnection httpURLConnection = null;
        Bitmap bitmap = null;
        try {
            httpURLConnection = (HttpURLConnection) imageUrl.openConnection();
            httpURLConnection.setRequestMethod("GET");
            if (httpURLConnection.getResponseCode() == 200) {
                InputStream inputStream = httpURLConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null)
                httpURLConnection.disconnect();
        }
        return bitmap;
    }


}
