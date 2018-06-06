package quotify.velhadev.com.quotify.postUi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import quotify.velhadev.com.quotify.R;

/**
 * Created by abhishek on 22/03/18.
 */

public class PersonalPostsViewAdapter extends RecyclerView.Adapter<PersonalPostsViewAdapter.PersonalPostViewHolder> {

    private List<PersonalPost> data;
    private Context context;
    private PersonalPostOnClickListener listener;

    public PersonalPostsViewAdapter(Context context) {
        this.context = context;
        listener = (PersonalPostOnClickListener) context;
        data = new ArrayList<>();
    }

    public interface PersonalPostOnClickListener{
        void onPersonalPostClick(int position, List<PersonalPost> data);
    }

    @NonNull
    @Override
    public PersonalPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.personal_collection_image, parent, false);
        return new PersonalPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PersonalPostViewHolder holder, int position) {
        Uri uri = Uri.parse(data.get((data.size() - 1) - position).getPostUrl());
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide.with(context).load(uri).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.progressBar.setVisibility(View.GONE);
                return false;
            }
        }).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class PersonalPostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private ProgressBar progressBar;
        public PersonalPostViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_personal_collection);
            progressBar = itemView.findViewById(R.id.post_progress_bar);
            imageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            listener.onPersonalPostClick(position, data);
        }
    }

    public void addData(PersonalPost post) {
        data.add(post);
        notifyDataSetChanged();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

}
