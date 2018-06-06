package quotify.velhadev.com.quotify.postUi;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abhishek on 22/03/18.
 */

@IgnoreExtraProperties
public class PersonalPost implements Parcelable {

    private String postUrl;
    private String fileName;
    private String postId;

    public PersonalPost() {

    }

    public PersonalPost(String postUrl, String fileName, String postId) {
        this.postUrl = postUrl;
        this.fileName = fileName;
        this.postId = postId;
    }

    protected PersonalPost(Parcel in) {
        postUrl = in.readString();
        fileName = in.readString();
        postId = in.readString();
    }

    public static final Creator<PersonalPost> CREATOR = new Creator<PersonalPost>() {
        @Override
        public PersonalPost createFromParcel(Parcel in) {
            return new PersonalPost(in);
        }

        @Override
        public PersonalPost[] newArray(int size) {
            return new PersonalPost[size];
        }
    };

    public String getPostUrl() {
        return postUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPostId() {
        return postId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("postUrl", postUrl);
        result.put("fileName", fileName);
        result.put("postId", postId);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(postUrl);
        dest.writeString(fileName);
        dest.writeString(postId);
    }
}
