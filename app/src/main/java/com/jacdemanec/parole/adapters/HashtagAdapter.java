package com.jacdemanec.parole.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.jacdemanec.parole.model.Hashtag;
import com.jacdemanec.parole.R;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HashtagAdapter extends FirebaseRecyclerAdapter<Hashtag, HashtagAdapter.HashtagHolder> {

    private HashtagOnClickListener hashtagOnClickListener;
    private String mUsername;
    private RecyclerView recyclerView;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public HashtagAdapter(@NonNull FirebaseRecyclerOptions<Hashtag> options, HashtagOnClickListener hashtagOnClickListener, String mUsername, RecyclerView recyclerView) {
        super(options);
        this.hashtagOnClickListener = hashtagOnClickListener;
        this.mUsername = mUsername;
        this.recyclerView = recyclerView;
    }

    public void updateUser(String username){
        mUsername = username;
        notifyDataSetChanged();
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        recyclerView.smoothScrollToPosition(getItemCount());
    }

    public interface HashtagOnClickListener {
        void onHashtagClicked(String hashtag);

        void onLikeClicked(String hashtag);

        void onFavoriteClicked(String hashtag);

        void onUnFavoriteClicked(String hashtag);

        void onImageClicked(String imageUrl);
    }

    @Override
    protected void onBindViewHolder(@NonNull final HashtagHolder holder, int position, @NonNull final Hashtag model) {
        boolean liked = false;
        holder.hashtagTextView.setText("#" + model.getTitle());
        holder.descriptionTextView.setText(model.getText());
        setHashtagClicked(holder.hashtagTextView, model.getTitle());
        setHashtagClicked(holder.descriptionTextView, model.getTitle());
        if (model.getLikes() != null) {
            holder.likesTextView.setText(String.valueOf(model.getLikes().size()));
        } else {
            holder.likesTextView.setText("0");
        }
        holder.userTextView.setText(model.getOwner());
        holder.thumbUpButton.setOnClickListener(view -> hashtagOnClickListener.onLikeClicked(model.getTitle()));
        Date date = new Date(Long.parseLong(model.getTimestamp().toString()));
        Log.d("TIMESTAMP", date.toString());

        if (model.getImageUrl()!=null) {
            Glide.with(holder.imageView.getContext())
                    .load(model.getImageUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.imageView);
            setImageClicker(holder.imageView, model.getImageUrl());
        }

        if (model.getFavorites() != null && mUsername !=null) {
            if (model.getFavorites().containsKey(mUsername)) {
                holder.favButton.setImageResource(R.drawable.ic_favorite_black_24dp);
                holder.favButton.setOnClickListener(view -> {
                    hashtagOnClickListener.onUnFavoriteClicked(model.getTitle());
                    holder.favButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                });
            } else {
                setFavoriteClicker(holder.favButton, model.getTitle());
            }
        } else {
            setFavoriteClicker(holder.favButton, model.getTitle());
        }
    }

    private void setFavoriteClicker(ImageButton button, final String hashtag) {
        button.setOnClickListener(view -> hashtagOnClickListener.onFavoriteClicked(hashtag));
    }

    private void setHashtagClicked(TextView textView, final String hashtag){
        textView.setOnClickListener(view -> hashtagOnClickListener.onHashtagClicked(hashtag));
    }

    private void setImageClicker(ImageView imageView, final String imageUrl){
        imageView.setOnClickListener(view -> hashtagOnClickListener.onImageClicked(imageUrl));
    }

    @NonNull
    @Override
    public HashtagHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hashtag, parent, false);
        return new HashtagHolder(view);
    }

    static class HashtagHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tv_hashtag)
        TextView hashtagTextView;
        @BindView(R.id.text)
        TextView descriptionTextView;
        @BindView(R.id.thumb_up)
        ImageButton thumbUpButton;
        @BindView(R.id.likes_count_tv)
        TextView likesTextView;
        @BindView(R.id.user_tv)
        TextView userTextView;
        @BindView(R.id.favorite_button)
        ImageButton favButton;
        @BindView(R.id.hashtag_image)
        ImageView imageView;

        HashtagHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}