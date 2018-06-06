package quotify.velhadev.com.quotify.imageViewer;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;

import quotify.velhadev.com.quotify.R;
import quotify.velhadev.com.quotify.Utils.Tags;
import quotify.velhadev.com.quotify.postUi.PersonalPost;
import quotify.velhadev.com.quotify.postUi.Post;

/**
 * Created by abhishek on 23/03/18.
 */

public class ImageViewerFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.image_viewer_fragment, container, false);

        ImageView imageView = view.findViewById(R.id.image_viewer);

        Boolean flag = getArguments().getBoolean(Tags.IS_PERSONAL_POST);

        if(flag){
            PersonalPost personalPost = getArguments().getParcelable(Tags.POST_DATA);
            Uri uri = Uri.parse(personalPost.getPostUrl());
            Glide.with(getContext()).load(uri).into(imageView);
        }
        else{
            Post post = getArguments().getParcelable(Tags.POST_DATA);
            Uri uri = Uri.parse(post.getPostUrl());
            Glide.with(getContext()).load(uri).into(imageView);
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu, menu);
    }
}
