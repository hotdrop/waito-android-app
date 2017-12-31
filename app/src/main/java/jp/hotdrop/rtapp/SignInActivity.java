package jp.hotdrop.rtapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.hotdrop.rtapp.models.User;
import timber.log.Timber;

public class SignInActivity extends BaseActivity {

    private static final String TAG = SignInActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @BindView(R.id.field_email)
    EditText mEmailField;

    @BindView(R.id.field_password)
    EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // まだメンバのオブジェクトが残っていれば自動でログインして画面遷移する
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    @OnClick(R.id.button_sign_in)
    void signIn() {
        Timber.d("signIn");

        if (!validateForm()) {
            return;
        }

        // TODO Firebaseの接続が返ってこないとプログレスバーがhideされないので改善の余地あり。addOnFailureListenerを実装すればいい？
        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        // TODO SAM変換できないものか
        // TODO signInWithEmailAndPasswordでNPEになるときがあるのでonFailureListenerとか入れて対応できないか
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            Timber.d("signIn Success!");
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Timber.w(task.getException(), "signIn failure");
                            Toast.makeText(SignInActivity.this, getString(R.string.toast_sign_in_failure), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @OnClick(R.id.button_sign_up)
    void signUp() {
        Timber.d("signUp");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        hideProgressDialog();

                        if (task.isSuccessful()) {
                            Timber.d("createUser Success!");
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Timber.w(task.getException(), "createUser failure");
                            Toast.makeText(SignInActivity.this, getString(R.string.toast_sign_up_failure), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * TODO このバリデーションの中身は必須入力チェックのみ。
     * 必須入力は視覚的に見せるようにしたい。そして次の操作はできないようにしたい。
     *
     * @return
     */
    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError(getString(R.string.text_warning_required));
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError(getString(R.string.text_warning_required));
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        return result;
    }

    private void onAuthSuccess(FirebaseUser user) {
        // TODO ユーザー名をメアドの左側で決め打ちするのなら、SignUpの時にそれを警告した方がいい
        String username = usernameFromEmail(user.getEmail());

        writeNewUser(user.getUid(), username, user.getEmail());

        // TODO startActivityはNavigationクラス作ってそこに任せたい
        startActivity(new Intent(SignInActivity.this, MainActivity.class));
        finish();
    }

    private String usernameFromEmail(String email) {
        return (email.contains("@")) ? email.split("@")[0] : email;
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        mDatabase.child(getString(R.string.child_users)).child(userId).setValue(user);
    }
}
