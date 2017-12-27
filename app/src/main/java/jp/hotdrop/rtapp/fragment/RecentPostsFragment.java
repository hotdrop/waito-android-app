package jp.hotdrop.rtapp.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class RecentPostsFragment extends PostListFragment {

    public RecentPostsFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // 多分新しいポストは先頭に追加されているので
        // この指定は最新の100ポストまで、ということになる・・？
        Query recentPostsQuery = databaseReference.child("posts").limitToFirst(100);
        return recentPostsQuery;
    }
}
