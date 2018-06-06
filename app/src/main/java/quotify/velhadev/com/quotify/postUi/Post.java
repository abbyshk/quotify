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
public class Post implements Parcelable {

    private String owner;
    private String postUrl;
    private String fileName;
    private String postId;

    public Post() {

    }

    public Post(String owner, String postUrl, String fileName, String postId) {
        this.owner = owner;
        this.postUrl = postUrl;
        this.fileName = fileName;
        this.postId = postId;
    }

    protected Post(Parcel in) {
        owner = in.readString();
        postUrl = in.readString();
        fileName = in.readString();
        postId = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getOwner() {
        return owner;
    }

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
        result.put("owner", this.owner);
        result.put("postUrl", this.postUrl);
        result.put("fileName", this.fileName);
        result.put("postId", this.postId);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(owner);
        dest.writeString(postUrl);
        dest.writeString(fileName);
        dest.writeString(postId);
    }
}
