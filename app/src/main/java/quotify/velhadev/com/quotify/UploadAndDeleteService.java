package quotify.velhadev.com.quotify;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import quotify.velhadev.com.quotify.Utils.Tags;
import quotify.velhadev.com.quotify.postUi.PersonalPost;
import quotify.velhadev.com.quotify.postUi.Post;

/**
 * Created by abhishek on 25/03/18.
 */

public class UploadAndDeleteService extends IntentService {

    public static final String ACTION_UPLOAD = "quotify.velhadev.com.quotify.upload_image";
    public static final String ACTION_DELETE = "quotify.velhadev.com.quotify.delete_image";
    public static final String RECIEVE_UPLOAD_RESULT = "quotify.velhadev.com.quotify.UPLOAD";


    private FirebaseUser firebaseUser;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;

    private byte[] data;
    private String fileName;
    private String postId;


    public UploadAndDeleteService() {
        super("UploadAndDeleteService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();

        if(action.equals(ACTION_UPLOAD)){
            data = intent.getByteArrayExtra(Tags.IMAGE_DATA);
            if(data != null){
                initializeFirebase();
                uploadImage();
            }
        }
        else if(action.equals(ACTION_DELETE)){
            fileName = intent.getStringExtra(Tags.FILE_NAME);
            postId = intent.getStringExtra(Tags.POST_ID);
            initializeFirebase();
            deleteImage();
        }
    }

    private void uploadImage() {
        final String userUid = firebaseUser.getUid();
        final String fileName = userUid + new Date().getTime() + ".jpg";

        StorageReference storageReference = firebaseStorage.getReference();

        StorageReference imageRef = storageReference.child(userUid + "/" + fileName);

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                addToDatabase(userUid, taskSnapshot.getDownloadUrl(), fileName);
            }
        });

    }

    private void initializeFirebase() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void addToDatabase(String userUid, Uri downloadUrl, String fileName) {
        String key = databaseReference.push().getKey();
        Post post = new Post(userUid, downloadUrl.toString(), fileName, key);
        Map<String, Object> postValues = post.toMap();
        PersonalPost personalPost = new PersonalPost(downloadUrl.toString(), fileName, key);
        Map<String, Object> personalPostValues = personalPost.toMap();

        Map<String, Object> updateChildren = new HashMap<>();
        updateChildren.put("/personal_posts/" + userUid + "/" + key, personalPostValues);
        updateChildren.put("/all_posts/" + key, postValues);
        databaseReference.updateChildren(updateChildren).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                eventHandled(true, false);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                eventHandled(false, false);
            }
        });
    }

    private void eventHandled(boolean handled, boolean isdeleteTask) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(RECIEVE_UPLOAD_RESULT);
        intent.putExtra(Tags.UPLOAD_HANDLED, handled);
        intent.putExtra(Tags.DELETE_TASK, isdeleteTask);
        sendBroadcast(intent);
    }

    private void deleteImage() {
        final String uId = firebaseUser.getUid();
        StorageReference fileReference =
                FirebaseStorage.getInstance()
                        .getReference()
                        .child(uId + "/" + fileName);

        fileReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                removeFileRef(uId);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                eventHandled(false, true);
            }
        });

    }

    private void removeFileRef(final String uId) {
        DatabaseReference postRef =
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("all_posts/" + postId);

        postRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DatabaseReference personalPostRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("personal_posts/" + uId + "/" + postId);
                personalPostRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        eventHandled(true, true);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        eventHandled(false, true);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                eventHandled(false, true);
            }
        });
    }

}

