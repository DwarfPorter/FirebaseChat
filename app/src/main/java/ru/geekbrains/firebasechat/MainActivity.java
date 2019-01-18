package ru.geekbrains.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 40404;
    private static final String TAG = "FirebaseChat";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private GoogleSignInClient googleSignInClient;
    private String email;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String token = BuildConfig.CLIENT_TOKEN;
        token = getString(R.string.default_web_client_id);
        //token = "37111180985-o1i3rb1db274bcvt7iadcksid6268ck5.apps.googleusercontent.com";

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(token)
                //.requestServerAuthCode("WEBAPPLICATION CLIENT ID")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInClient.silentSignIn ()
                .addOnCompleteListener (this, new OnCompleteListener<GoogleSignInAccount>() {

                    @Override
                    public void onComplete (@NonNull Task<GoogleSignInAccount> task) {
                        handleSignInResult (task);
                    }
                });

        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null){
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            return;
        }
        initializeFirebase(account);*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                initializeFirebase(account);
            } catch (ApiException e) {
                Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            if (completedTask.isSuccessful()){
                GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                initializeFirebase(account);
                return;
            }
            signIn();
            //initializeFirebase(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }

    private void signIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void initializeFirebase(GoogleSignInAccount account){
        email = account.getEmail();
        TextView gmail = findViewById(R.id.gMail);
        gmail.setText(email);

        firebaseAuth = FirebaseAuth.getInstance();

        String token = account.getIdToken();

        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    return;
                }
                firebaseUser = firebaseAuth.getCurrentUser();
                updateUI();
            }
        });
    }

    private void updateUI(){
        TextView userText = findViewById(R.id.userName);
        userText.setText(firebaseUser.getDisplayName());
    }

}
