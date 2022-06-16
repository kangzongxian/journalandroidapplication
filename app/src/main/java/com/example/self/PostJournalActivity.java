package com.example.self;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.system.StructUtsname;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

import model.Journal;
import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {

    // Access Gallery Code
    // Not useful for now
    // private int GALLERY_CODE = 1;
    private String TAG = "PostJournalActivity";

    // Initialise Widgets
    private Button saveButton;
    private ProgressBar progressBar;
    private ImageView addPhotoButton;
    private EditText titleEditText;
    private EditText thoughtsEditText;
    private TextView currentUserTextView;
    private ImageView imageView;

    // Will assign our current UserId and Username
    private String currentUserId;
    private String currentUserName;

    // Firebase Fields
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    // Connection to Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    // Reference to the Journal, NOT the User
    private CollectionReference collectionReference = db.collection("Journal");

    // Reference the imageUri
     private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        // Get the instance form Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Get storage Reference
        storageReference = FirebaseStorage.getInstance().getReference();

        // Get our Widgets
        progressBar = findViewById(R.id.post_progressBar);
        titleEditText = findViewById(R.id.post_title_et);
        thoughtsEditText = findViewById(R.id.post_description_et);
        currentUserTextView = findViewById(R.id.post_username_textview);

        imageView = findViewById(R.id.post_imageView);
        saveButton = findViewById(R.id.post_save_journal_button);
        saveButton.setOnClickListener(this);

        addPhotoButton = findViewById(R.id.postCameraButton);
        addPhotoButton.setOnClickListener(this);

        // Make the bar invisible straight away, cause we only want it to roll when we add stuff
        progressBar.setVisibility(View.INVISIBLE);

        // Get the userId and username and set it, only if instance not null
        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUsername();

            currentUserTextView.setText(currentUserName);
        }

        // Add the authStateListener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Get current user
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.post_save_journal_button:
                //Save Journal
                saveJournal();
                break;
            case R.id.postCameraButton:
                // Get Image from Gallery/Phone
                startActivityIntent.launch("image/*");
                // Get anything that is image-related
                break;
        }

    }

    private void saveJournal() {
        // Get the values
        String title = titleEditText.getText().toString().trim();
        String thoughts = thoughtsEditText.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        // Validate them, only do stuff if not empty
        Log.d("Hello", String.valueOf(imageUri));
        Log.d("Hello", String.valueOf(imageUri != null));
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null) {
            // Create a path to Journal Images
            // However, each image we add needs to be UNIQUE (we can use timestamps to help us)
            Log.d("Hello", "Can enter path");
            final StorageReference filepath = storageReference
                    // Go into Journal Images folder
                    .child("journal_images")
                    // And the unique location of the image
                    .child("my_image_" + Timestamp.now().getSeconds()); // Get unique image ids

            // Put the file into storage
            filepath.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            // Get download Url
                            // This will allow us to get the image URL
                            // onSuccess's Uri uri is the one to let us get the url
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Log.d("Hello", "Can build Journal");

                                    // Get the image URL
                                    String imageUrl = uri.toString();
                                    // TODO: Create a Journal Object - model
                                    // Create our Journal Object, to be added to DB
                                    Journal journal = new Journal();
                                    journal.setTitle(title);
                                    journal.setThought(thoughts);
                                    journal.setImageUrl(imageUrl);
                                    journal.setTimeAdded(new Timestamp(new Date()));
                                    journal.setUserName(currentUserName);
                                    journal.setUserId(currentUserId);

                                    // TODO: Invoke our collectionReference (In Firestore)
                                    // Add Journal to the Database
                                    collectionReference.add(journal)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    // Make progress bar invisible since success already
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    Log.d("Hello", "Can add journal");

                                                    // Intent to go to another activity
                                                    startActivity(new Intent(PostJournalActivity.this,
                                                            JournalListActivity.class));

                                                    // Call finish to finish the current activity
                                                    // Since after this we will move to a new activity
                                                    finish();

                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d(TAG, "onFaiure: " + e.getMessage());

                                                }
                                            });

                                    // TODO: And save a Journal instance

                                }
                            });




                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.d("Hello", "Failed" + e.getMessage());
                        }
                    });
        } else {

        }
    }

    // New way to startActivityForResult(), cause deprecated
    // This is the method to get images now
    // We input a string, so Launcher is <String>
    // Then we getting back a Uri type
    ActivityResultLauncher<String> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    Log.d("Hello", "Inside");
                    if (result != null) {
                        imageView.setImageURI(result);
                        imageUri = result;

                    }

                }
            });

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }


    // When stopped, we don't want to listen anymore
    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}