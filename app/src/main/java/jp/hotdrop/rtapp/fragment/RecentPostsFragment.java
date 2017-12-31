package jp.hotdrop.rtapp.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import jp.hotdrop.rtapp.R;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child(getString(R.string.child_posts)).limitToFirst(100);
    }
}
