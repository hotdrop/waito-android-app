package jp.hotdrop.rtapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.hotdrop.rtapp.models.Comment;
import jp.hotdrop.rtapp.models.Post;
import jp.hotdrop.rtapp.models.User;
import jp.hotdrop.rtapp.viewholder.CommentViewHolder;
import timber.log.Timber;

public class PostDetailActivity extends BaseActivity {

    private static final String TAG = PostDetailActivity.class.getSimpleName();

    public static final String EXTRA_POST_KEY = "post_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private CommentAdapter mAdapter;

    @BindView(R.id.post_author)
    TextView mAuthorView;

    @BindView(R.id.post_title)
    TextView mTitleView;

    @BindView(R.id.post_body)
    TextView mBodyView;

    @BindView(R.id.field_comment_text)
    EditText mCommentField;

    @BindView(R.id.recycler_comments)
    RecyclerView mCommentsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        String postKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if (postKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
        }

        ButterKnife.bind(this);

        mPostReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(getString(R.string.child_posts))
                .child(postKey);
        mCommentsReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(getString(R.string.child_post_comments))
                .child(postKey);

        mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onStart() {
        super.onStart();

        // TODO 一度ポストしたデータは編集不能
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                mAuthorView.setText(post.author);
                mTitleView.setText(post.title);
                mBodyView.setText(post.body);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.w(databaseError.toException(), "loadPost:onCancelled");
                Toast.makeText(PostDetailActivity.this, getString(R.string.toast_post_cancel), Toast.LENGTH_SHORT).show();
            }
        };
        mPostReference.addValueEventListener(postListener);
        mPostListener = postListener;
        mAdapter = new CommentAdapter(this, mCommentsReference);
        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }
        mAdapter.cleanupListener();
    }

    @OnClick(R.id.button_post_comment)
    void postComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance()
                .getReference()
                .child(getString(R.string.child_users))
                .child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        User user = dataSnapshot.getValue(User.class);
                        Comment comment = new Comment(uid, user.username, mCommentField.getText().toString());

                        mCommentsReference.push().setValue(comment);
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {

        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        private List<String> mCommentIds = new ArrayList<>();
        private List<Comment> mComments = new ArrayList<>();

        CommentAdapter(final Context context, DatabaseReference ref) {

            mContext = context;
            mDatabaseReference = ref;

            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildAdded: key=%s", dataSnapshot.getKey());

                    Comment comment = dataSnapshot.getValue(Comment.class);
                    mCommentIds.add(dataSnapshot.getKey());
                    mComments.add(comment);
                    notifyItemInserted(mComments.size() - 1);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildChanged: key=%s", dataSnapshot.getKey());

                    Comment newComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        mComments.set(commentIndex, newComment);
                        notifyItemChanged(commentIndex);
                    } else {
                        Timber.w("onChildChanged:unknown child. key=%s", commentKey);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Timber.d("onChildRemoved: key=%s", dataSnapshot.getKey());

                    String commentKey = dataSnapshot.getKey();
                    int commentIndex = mCommentIds.indexOf(commentKey);
                    if (commentIndex > -1) {
                        mCommentIds.remove(commentIndex);
                        mComments.remove(commentIndex);
                        notifyItemRemoved(commentIndex);
                    } else {
                        Timber.w("onChildRemoved:unknown child. key=%s", commentKey);
                    }
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Timber.d("onChildMoved: key=%s", dataSnapshot.getKey());

                    Comment movedComment = dataSnapshot.getValue(Comment.class);
                    String commentKey = dataSnapshot.getKey();

                    // TODO 未実装
                    // ArrayListの要素移動させるのは計算量的に嫌なのでCollectionにした方がいいかも
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Timber.w(databaseError.toException(), "postComments:onCancelled");
                    Toast.makeText(mContext, mContext.getString(R.string.toast_post_comment_cancel), Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
            Comment comment = mComments.get(position);
            holder.setComment(comment);
        }

        @Override
        public int getItemCount() {
            return mComments.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }
    }
}
