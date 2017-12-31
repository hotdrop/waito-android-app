package jp.hotdrop.rtapp.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import jp.hotdrop.rtapp.R;
import jp.hotdrop.rtapp.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;
    private TextView authorView;
    private ImageView starView;
    private TextView numStarsView;
    private TextView bodyView;

    public PostViewHolder(View itemView) {
        super(itemView);

        // TODO これはdatabindingで対応する。
        titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.post_num_stars);
        bodyView = itemView.findViewById(R.id.post_body);
    }

    public void bindToPost(Post post, View.OnClickListener starClickListener) {
        titleView.setText(post.title);
        authorView.setText(post.author);
        numStarsView.setText(String.valueOf(post.starCount));
        bodyView.setText(post.body);

        starView.setOnClickListener(starClickListener);
    }

    public void setStar() {
        starView.setImageResource(R.drawable.ic_toggle_star_24);
    }

    public void setNotStar() {
        starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
    }
}
