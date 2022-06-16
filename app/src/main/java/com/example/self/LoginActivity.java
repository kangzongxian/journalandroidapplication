package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import util.JournalApi;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createAcctButton;

    private AutoCompleteTextView emailAddress;
    private EditText password;
    private ProgressBar progressBar;

    // Firebase Stuff
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Link to Users part of the Database
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Get Firebase Instance
        firebaseAuth = FirebaseAuth.getInstance();


        // Get the buttons
        loginButton = findViewById(R.id.email_sign_in_button);
        createAcctButton = findViewById(R.id.create_acct_button_login);

        // Get the widgets
        emailAddress = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progress);



        // Go from LoginActivity to CreateAccountActivity
        createAcctButton.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
        });

        loginButton.setOnClickListener(view -> {
            loginEmailPassWordUser(emailAddress.getText().toString().trim(),
                    password.getText().toString().trim());
        });

    }

    private void loginEmailPassWordUser(String email, String pwd) {

        progressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {
            // Use the inbuilt signin function
            firebaseAuth.signInWithEmailAndPassword(email, pwd)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Get user and ID
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                            String currentUserId = user.getUid();

                            // From the collectionReference, we try to find the currentUserId
                            collectionReference
                                    .whereEqualTo("userId", currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                            // If no error, all is good
                                            if (error != null) {
                                                return;
                                            }

                                            assert value != null;
                                            // Check if value is not empty, that means got something
                                            if (!value.isEmpty()) {
                                                progressBar.setVisibility(View.INVISIBLE);

                                                // Loop through and get the username
                                                // We set it with our JournalApi
                                                // The one we get the username and userId
                                                for (QueryDocumentSnapshot snapshot : value) {
                                                    JournalApi journalApi = JournalApi.getInstance();
                                                    journalApi.setUsername(snapshot.getString("username"));
                                                    journalApi.setUserId(snapshot.getString("userId"));

                                                    // Go to ListActivity after that
                                                    startActivity(new Intent(LoginActivity.this,
                                                            PostJournalActivity.class));
                                                    finish();
                                                }



                                            }
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });

        } else {
            progressBar.setVisibility(View.INVISIBLE);

            // Notify user if invalid fields
            Toast.makeText(LoginActivity.this,
                    "Please enter email and password", Toast.LENGTH_LONG).show();
        }
    }
}