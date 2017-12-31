package jp.hotdrop.rtapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import jp.hotdrop.rtapp.models.Post;
import jp.hotdrop.rtapp.models.User;
import timber.log.Timber;

public class NewPostActivity extends BaseActivity {

    private static final String TAG = NewPostActivity.class.getSimpleName();

    private DatabaseReference mDatabase;

    private EditText mTitleField;
    private EditText mBodyField;
    private FloatingActionButton mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTitleField = findViewById(R.id.field_title);
        mBodyField = findViewById(R.id.field_body);
        mSubmitButton = findViewById(R.id.fab_submit_post);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });
    }

    private void submitPost() {
        final String title = mTitleField.getText().toString();
        final String body = mBodyField.getText().toString();

        // TODO validateはメソッドを分けたほうがいいかな
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(getString(R.string.text_warning_required));
            return;
        }

        if (TextUtils.isEmpty(body)) {
            mBodyField.setError(getString(R.string.text_warning_required));
            return;
        }

        disabledEditingField();
        Toast.makeText(this, getString(R.string.toast_post_loading), Toast.LENGTH_SHORT).show();

        final String userId = getUid();
        mDatabase.child(getString(R.string.child_users))
                .child(userId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = dataSnapshot.getValue(User.class);
                                if (user == null) {
                                    Timber.e("User %s is unexpectedly null", userId);
                                    Toast.makeText(NewPostActivity.this, getString(R.string.toast_post_failure), Toast.LENGTH_SHORT).show();
                                } else {
                                    writeNewPost(userId, user.username, title, body);
                                }

                                enableEditingField();
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Timber.w(databaseError.toException(), "getUser:onCancelled");
                                enableEditingField();
                            }
                        }
        );
    }

    private void enableEditingField() {
        mTitleField.setEnabled(true);
        mBodyField.setEnabled(true);
        mSubmitButton.setVisibility(View.VISIBLE);
    }

    private void disabledEditingField() {
        mTitleField.setEnabled(false);
        mBodyField.setEnabled(false);
        mSubmitButton.setVisibility(View.GONE);
    }

    private void writeNewPost(String userId, String username, String title, String body) {
        String key = mDatabase.child(getString(R.string.child_posts))
                .push()
                .getKey();
        Post post = new Post(userId, username, title, body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/"+ getString(R.string.child_posts) + "/" + key, postValues);
        childUpdates.put("/" + getString(R.string.child_user_posts) + "/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
}
