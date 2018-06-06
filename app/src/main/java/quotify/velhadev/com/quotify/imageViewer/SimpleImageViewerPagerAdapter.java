package quotify.velhadev.com.quotify.imageViewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import quotify.velhadev.com.quotify.Utils.Tags;
import quotify.velhadev.com.quotify.postUi.PersonalPost;
import quotify.velhadev.com.quotify.postUi.Post;

/**
 * Created by abhishek on 23/03/18.
 */

public class SimpleImageViewerPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<?> data;

    public SimpleImageViewerPagerAdapter(FragmentManager fm, ArrayList<?> data) {
        super(fm);
        this.data = data;
    }

    @Override
    public Fragment getItem(int position) {

        ImageViewerFragment fragment = new ImageViewerFragment();
        Bundle bundle = new Bundle();
        Object data = this.data.get((this.data.size() - 1) - position);
        try {
            bundle.putParcelable(Tags.POST_DATA, (PersonalPost) data);
            bundle.putBoolean(Tags.IS_PERSONAL_POST, true);
        }
        catch (ClassCastException e){
            bundle.putParcelable(Tags.POST_DATA, (Post) data);
            bundle.putBoolean(Tags.IS_PERSONAL_POST, false);
        }
        finally {
            if(data != null)
                fragment.setArguments(bundle);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return data.size();
    }

}
