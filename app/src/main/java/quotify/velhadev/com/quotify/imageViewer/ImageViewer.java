package quotify.velhadev.com.quotify.imageViewer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.Manifest;
import quotify.velhadev.com.quotify.R;
import quotify.velhadev.com.quotify.Utils.Tags;
import quotify.velhadev.com.quotify.editor.UploadAndDeleteActivity;
import quotify.velhadev.com.quotify.postUi.PersonalPost;
import quotify.velhadev.com.quotify.postUi.Post;

public class ImageViewer extends AppCompatActivity {

    private static final int INVALID_POSITION_VALUE = -1;
    private static final int READ_WRITE_PERMISSION_CODE = 100;
    public static final int DELETE_DATA_REQUEST = 102;

    private ArrayList<?> data;
    private boolean loadToolbar = true;
    private Post postData;
    private PersonalPost personalPostData;

    private ViewPager viewPager;
    private SimpleImageViewerPagerAdapter pagerAdapter;
    private int currentPosition;
    private AlertDialog deleteDialog;


    private FirebaseUser user;

    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            currentPosition = position;
            invalidateOptionsMenu();
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private DialogInterface.OnClickListener cancel = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener delete = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(getApplicationContext(), UploadAndDeleteActivity.class);
            intent.putExtra(Tags.POST_ID, getPostId());
            intent.putExtra(Tags.FILE_NAME, getFileName());
            startActivityForResult(intent, DELETE_DATA_REQUEST);        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInMultiWindowMode())
                loadToolbar = false;
            else
                loadToolbar = true;
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!loadToolbar) {
            AppBarLayout appBarLayout = findViewById(R.id.app_bar);
            appBarLayout.setVisibility(View.GONE);
        }

        data = getIntent().getParcelableArrayListExtra(Tags.POST_DATA);
        currentPosition = getIntent().getIntExtra(Tags.POST_POSITION, INVALID_POSITION_VALUE);

        if (data == null || currentPosition < 0)
            finish();

        viewPager = findViewById(R.id.image_gallery);
        pagerAdapter = new SimpleImageViewerPagerAdapter(getSupportFragmentManager(), data);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentPosition);
        viewPager.addOnPageChangeListener(pageChangeListener);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        createDeleteDialog();

    }

    private void createDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_delete_msg))
                .setPositiveButton(getString(R.string.dialog_msg_delete), delete)
                .setNegativeButton(getString(R.string.dialog_msg_cancel), cancel);
        deleteDialog = builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (loadToolbar)
            selectMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.delete_image:
                deleteDialog.show();
                break;
            case R.id.share_image:
                if(Build.VERSION.SDK_INT >= 23)
                    checkPermissions();
                else
                    shareImage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case READ_WRITE_PERMISSION_CODE:
                if(grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                {
                    shareImage();
                }

                else {
                    Toast.makeText(this,
                            getString(R.string.permission_error),
                            Toast.LENGTH_LONG).show();
                    finish();
                }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == DELETE_DATA_REQUEST) {

            boolean check = data.getBooleanExtra(Tags.UPLOAD_HANDLED, false);

            if (check)
                finish();
            else
                Toast.makeText(getApplicationContext(),
                        getString(R.string.error_deleting_file),
                        Toast.LENGTH_SHORT).show();
        }

    }

    private void checkPermissions() {

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    READ_WRITE_PERMISSION_CODE);
        }

        else {
            shareImage();
        }
    }

    private String getPostId() {
        if (postData != null)
            return postData.getPostId();
        else
            return personalPostData.getPostId();
    }

    private String getFileName() {
        if (postData != null)
            return postData.getFileName();

        return personalPostData.getFileName();
    }

    private void shareImage() {
        ImageView ivImage = findViewById(R.id.image_viewer);
        Uri bmpUri = getLocalBitmapUri(ivImage);
        if (bmpUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } else {
            Toast.makeText(this,
                    getString(R.string.error_sharing_image),
                    Toast.LENGTH_LONG).show();
        }
    }

    private Uri getLocalBitmapUri(ImageView ivImage) {
        Drawable drawable = ivImage.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable) ivImage.getDrawable()).getBitmap();
        } else {
            return null;
        }

        Uri bmpUri = null;

        try {

            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    getString(R.string.app_name) + getFileName());

            if (!file.exists()) {
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
            }

            if (Build.VERSION.SDK_INT < 24)
                bmpUri = Uri.fromFile(file);
            else {
                bmpUri = FileProvider.getUriForFile(ImageViewer.this, Tags.CONTENT_PROVIDER_AUTHORITY, file);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bmpUri;
    }


    private void selectMenu(Menu menu) {
        int id = -1;
        try {
            personalPostData = (PersonalPost) data.get((data.size() - 1) - currentPosition);
            id = R.menu.menu;
        } catch (ClassCastException e) {
            postData = (Post) data.get((data.size() - 1) - currentPosition);
            boolean isAuthUser = checkAuthUser();
            if (isAuthUser)
                id = R.menu.menu;
            else
                id = R.menu.menu_all;
        } finally {
            if (id != -1)
                getMenuInflater().inflate(id, menu);
        }
    }

    private boolean checkAuthUser() {
        if (user == null)
            finish();
        else {
            if (postData != null)
                return postData.getOwner().equals(user.getUid());
        }
        return false;
    }
}
