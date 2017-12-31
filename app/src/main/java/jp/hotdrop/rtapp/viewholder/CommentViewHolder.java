package jp.hotdrop.rtapp.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import jp.hotdrop.rtapp.R;
import jp.hotdrop.rtapp.models.Comment;

public class CommentViewHolder extends RecyclerView.ViewHolder {

    private TextView authorView;
    private TextView bodyView;

    public CommentViewHolder(View itemView) {
        super(itemView);

        authorView = itemView.findViewById(R.id.comment_author);
        bodyView = itemView.findViewById(R.id.comment_body);
    }

    public void setComment(Comment comment) {
        authorView.setText(comment.author);
        bodyView.setText(comment.text);
    }
}
