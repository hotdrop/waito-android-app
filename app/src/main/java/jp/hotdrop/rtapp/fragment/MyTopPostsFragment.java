package jp.hotdrop.rtapp.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import jp.hotdrop.rtapp.R;

public class MyTopPostsFragment extends PostListFragment {

    public MyTopPostsFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        return databaseReference.child(getString(R.string.child_user_posts)).child(getUid()).orderByChild("starCount");
    }
}
