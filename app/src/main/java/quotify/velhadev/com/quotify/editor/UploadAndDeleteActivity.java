package quotify.velhadev.com.quotify.editor;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import quotify.velhadev.com.quotify.R;
import quotify.velhadev.com.quotify.UploadAndDeleteService;
import quotify.velhadev.com.quotify.Utils.Tags;
import quotify.velhadev.com.quotify.imageViewer.ImageViewer;

public class UploadAndDeleteActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;

    private boolean isDeleteTask;
    private byte[] data;
    private String postId;
    private String fileName;
    private boolean handled;

    private EventBroadcastReceiver eventBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        this.setFinishOnTouchOutside(false);
        data = getIntent().getByteArrayExtra(Tags.IMAGE_DATA);
        initializeFirebase();

        if (firebaseUser != null && data != null) {
            uploadImage();
        } else if (firebaseUser != null) {
            fileName = getIntent().getStringExtra(Tags.FILE_NAME);
            postId = getIntent().getStringExtra(Tags.POST_ID);
            if (postId != null && fileName != null)
                deleteImage();
            else
                finish();
        } else {
            finish();
        }
    }

    private void deleteImage() {

        Intent intent = new Intent(this, UploadAndDeleteService.class);
        intent.setAction(UploadAndDeleteService.ACTION_DELETE);
        intent.putExtra(Tags.POST_ID, postId);
        intent.putExtra(Tags.FILE_NAME, fileName);
        startService(intent);

        eventBroadcastReceiver = new EventBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(UploadAndDeleteService.RECIEVE_UPLOAD_RESULT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(eventBroadcastReceiver, intentFilter);

    }

    private void uploadImage() {
        Intent intent = new Intent(this, UploadAndDeleteService.class);
        intent.setAction(UploadAndDeleteService.ACTION_UPLOAD);
        intent.putExtra(Tags.IMAGE_DATA, data);
        startService(intent);

        eventBroadcastReceiver = new EventBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(UploadAndDeleteService.RECIEVE_UPLOAD_RESULT);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(eventBroadcastReceiver, intentFilter);
    }

    public void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(Tags.UPLOAD_HANDLED, handled);

        if (isDeleteTask)
            setResult(ImageViewer.DELETE_DATA_REQUEST, intent);
        else
            setResult(EditorActivity.UPLOAD_REQUEST_CODE, intent);

        finish();
    }

    private void initializeFirebase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(eventBroadcastReceiver);
    }

    public class EventBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && intent.getAction().equals(UploadAndDeleteService.RECIEVE_UPLOAD_RESULT)) {
                isDeleteTask = intent.getBooleanExtra(Tags.DELETE_TASK, false);
                handled = intent.getBooleanExtra(Tags.UPLOAD_HANDLED, false);
                returnResult();
            }
        }
    }

}
