package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // Firestore Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Establish connection to our Users
    private CollectionReference collectionReference = db.collection("Users");

    // Initialise our widgets
    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private EditText userNameEditText;
    private Button createAcctButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Get the instance of the firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

        // Get the widgets
        createAcctButton = findViewById(R.id.create_acct_button);
        progressBar = findViewById(R.id.create_acct_progress);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);
        userNameEditText = findViewById(R.id.username_account);

        // Create authStateListener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    // User is already logged in
                } else {
                    // No uss=er yet
                }
            }
        };

        // Upon clicking, create an account
        createAcctButton.setOnClickListener(view -> {
            // Create a method to create the account

            // Validate the input fields
            if (!TextUtils.isEmpty(emailEditText.getText().toString()) &&
            !TextUtils.isEmpty(passwordEditText.getText().toString()) &&
            !TextUtils.isEmpty(userNameEditText.getText().toString())) {

                // Get the field values
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String username = userNameEditText.getText().toString().trim();

                // Create the account
                createUserEmailAccount(email, password, username);
            } else {
                // If fields invalid, notify user
                Toast.makeText(CreateAccountActivity.this, "Empty Fields not Allowed",
                        Toast.LENGTH_LONG)
                        .show();
            }

        });

    }

    // Method to create account
    private void createUserEmailAccount(String email, String password, String username) {
        // Validate whether the fields are empty using in-built TextUtils class
        if (!TextUtils.isEmpty(email)
        && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {

            // Make the progress bar visible (so ongoing)
            progressBar.setVisibility(View.VISIBLE);

            // Create the user in the DB
            // Add the Listeners
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // User created
                                // Add user to the Firestore DB
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                String currentUserId = currentUser.getUid();

                                // Create a user Map to create user in User collection
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put("userId", currentUserId);
                                userObj.put("username", username);

                                // Save to our Firestore DB
                                // Add the User
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                // Take this user to add 1 Journal Entry
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                // Validate the task
                                                                if (task.getResult().exists()) {
                                                                    // Set progressBar invisible
                                                                    progressBar.setVisibility(View.INVISIBLE);

                                                                    // Get the username of the user
                                                                    String name = task.getResult()
                                                                            .getString("username");

                                                                    // Get the instance of the global API
                                                                    JournalApi journalApi = JournalApi.getInstance();

                                                                    // Set the current Id and user
                                                                    journalApi.setUserId(currentUserId);
                                                                    journalApi.setUsername(username);

                                                                    // Create new intent for user to go to Create Journal
                                                                    // Pass the userId and the username
                                                                    Intent intent = new Intent(CreateAccountActivity.this,
                                                                            PostJournalActivity.class);
                                                                    intent.putExtra("username", name);
                                                                    intent.putExtra("userId", currentUserId);
                                                                    startActivity(intent);

                                                                } else {
                                                                    progressBar.setVisibility(View.INVISIBLE);

                                                                }

                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

;                                // Take user to the AddJournalActivity

                            } else {
                                // Something went wrong
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        }
        else {

        }
    }

    // Before we show user stuff on screen, we want to make sure all the Firebase stuff is done
    // So we do it in onStart()
    @Override
    protected void onStart() {
        super.onStart();

        // Get current user
        currentUser = firebaseAuth.getCurrentUser();

        // Listen to changes in firebase authorization
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}