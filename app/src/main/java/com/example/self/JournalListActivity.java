package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import model.Journal;
import ui.JournalRecyclerAdapter;
import util.JournalApi;

public class JournalListActivity extends AppCompatActivity {

    // Firebase Stuff
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    // Journal Stuff
    private List<Journal> journalList;
    private RecyclerView recyclerView;
    private JournalRecyclerAdapter journalRecyclerAdapter;

    // CollectionReferences
    private CollectionReference collectionReference = db.collection("Journal");
    private TextView noJournalEntry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        // Assign Firebase stuff
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // Doing necessary stuff for Journal and RecyclerView
        noJournalEntry = findViewById(R.id.list_no_thoughts);
        journalList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu (the ones with add thought and sign out)
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        // Engage actions depending on what item is selcted
        switch (item.getItemId()) {
            case R.id.action_add:
                // Take users to add journal (another activity)

                // Check if user and firebaseAuth not null
                if (user != null && firebaseAuth != null) {
                    startActivity(new Intent(JournalListActivity.this,
                            PostJournalActivity.class));

//                    finish();
                }

                break;
            case R.id.action_signout:
                // Sign user out

                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();

                    // Go back to the beginning, which is MainActivity
                    startActivity(new Intent(JournalListActivity.this,
                            MainActivity.class));

//                    finish();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Get our journals at onStart()
    @Override
    protected void onStart() {
        super.onStart();

        // Get all the stuff pertaining to the current user
        collectionReference.whereEqualTo("userId", JournalApi.getInstance().getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // queryDocumentSnapshots will have all the items we want

                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Get all the journals related to the user
                            // Add them to the journalList
                            for (QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                                // Convert what we get back to a journal
                                Journal journal = journals.toObject(Journal.class);
                                journalList.add(journal);
                            }

                            // Invoke RecylerView and pass the journals List
                            journalRecyclerAdapter = new JournalRecyclerAdapter(JournalListActivity.this,
                                    journalList);
                            recyclerView.setAdapter(journalRecyclerAdapter);
                            // It will know to update itself whenever the data changes
                            journalRecyclerAdapter.notifyDataSetChanged();

                        } else {
                            // If we got no journals, means we tell them that got no journal
                            noJournalEntry.setVisibility(View.VISIBLE);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}