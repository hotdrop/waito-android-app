package jp.hotdrop.rtapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import jp.hotdrop.rtapp.PostDetailActivity;
import jp.hotdrop.rtapp.R;
import jp.hotdrop.rtapp.models.Post;
import jp.hotdrop.rtapp.viewholder.PostViewHolder;
import timber.log.Timber;

public abstract class PostListFragment extends Fragment {

    private static final String TAG = PostListFragment.class.getSimpleName();

    private DatabaseReference mDatabase;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager mManager = new LinearLayoutManager(getActivity());

        // 新しく登録したものを一番上に持ってくるため逆順にしている。
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        Query postsQuery = getQuery(mDatabase);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(PostViewHolder holder, int position, final Post model) {
                final DatabaseReference postRef = getRef(position);

                final String postKey = postRef.getKey();
                holder.itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                    intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                    startActivity(intent);
                });

                if (model.stars.containsKey(getUid())) {
                    holder.setStar();
                } else {
                    holder.setNotStar();
                }

                holder.bindToPost(model, starView -> {
                    DatabaseReference globalPostRef = mDatabase.child(getString(R.string.child_posts))
                            .child(postRef.getKey());
                    DatabaseReference userPostRef = mDatabase.child(getString(R.string.child_user_posts))
                            .child(model.uid)
                            .child(postRef.getKey());
                    onStarClicked(globalPostRef);
                    onStarClicked(userPostRef);
                });
            }

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                return new PostViewHolder(inflater.inflate(R.layout.item_post, parent, false));
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Timber.d("postTransaction:onComplete:%s", databaseError);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);
}
