package com.example.adityasingh.chatapp;

import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    public static final int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);





        mUsername = ANONYMOUS;

        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mMessagesDatabaseReference= mFirebaseDatabase.getReference().child("messages");
        mChatPhotosStorageReference= mFirebaseStorage.getReference().child("chat_photos");

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<Message> Messages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, Messages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click

                Message message=new Message(mMessageEditText.getText().toString(),mUsername,null);
                mMessagesDatabaseReference.push().setValue(message);
                // Clear input box
                mMessageEditText.setText("");
            }
        });


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                 FirebaseUser user = firebaseAuth.getCurrentUser();
                                 if (user != null) {
                                         // User is signed in
                                     onSignedInInitialize(user.getDisplayName());
                                     } else {
                                        // User is signed out
//                                                startActivityForResult(
//                                                                AuthUI.getInstance()
//                                                                               .createSignInIntentBuilder()
//                                                                .setIsSmartLockEnabled(false)
//                                                                .setProviders(
//                                                                        AuthUI.EMAIL_PROVIDER,
//                                                                        AuthUI.GOOGLE_PROVIDER)
//                                                                .build(),
//                                                        RC_SIGN_IN);
                                     onSignedOutCleanup();
                                     List<AuthUI.IdpConfig> providers = Arrays.asList(
                                             new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                             new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
                                     );

                                     startActivityForResult(
                                             AuthUI.getInstance()
                                                     .createSignInIntentBuilder()
                                                     .setIsSmartLockEnabled(false)
                                                     .setProviders(providers)
                                                     .build(),
                                             RC_SIGN_IN);
                                    }
                            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == RC_SIGN_IN) {
                        if (resultCode == RESULT_OK) {
                                // Sign-in succeeded, set up the UI
                                        Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                            } else if (resultCode == RESULT_CANCELED) {
                                // Sign in was canceled by the user, finish the activity
                                        Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                                finish();
                            } else if(requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK) {
                            Uri selectedImageUri= data.getData();
                            StorageReference photoRef= mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

                            // Upload file to Firebase Storage
                                        photoRef.putFile(selectedImageUri)
                                                        .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                // When the image has successfully uploaded, we get its download URL
                                                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                                                        // Set the download URL to the message box, so that the user can send it to the database
                                                                                Message message = new Message(null, mUsername, downloadUrl.toString());
                                                                mMessagesDatabaseReference.push().setValue(message);
                                                           }
                                        });
                        }
                    }
            }

    @Override
    protected void onResume() {
                super.onResume();
                mFirebaseAuth.addAuthStateListener(mAuthStateListener);
            }

    @Override
    protected void onPause() {
                super.onPause();
                if (mAuthStateListener != null) {
                        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                    }

        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
                        case R.id.sign_out_menu:
                                AuthUI.getInstance().signOut(this);
                                return true;
                        default:
                                return super.onOptionsItemSelected(item);
                    }
    }

    private void onSignedInInitialize(String username) {
                mUsername = username;
                attachDatabaseReadListener();
            }

            private void onSignedOutCleanup() {
                mUsername = ANONYMOUS;
                mMessageAdapter.clear();
                detachDatabaseReadListener();
            }

            private void attachDatabaseReadListener() {
                if (mChildEventListener == null) {
                        mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                        Message message = dataSnapshot.getValue(Message.class);
                                        mMessageAdapter.add(message);
                                    }

                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
                        mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
                    }
            }

            private void detachDatabaseReadListener() {
                if (mChildEventListener != null) {
                        mMessagesDatabaseReference.removeEventListener(mChildEventListener);
                        mChildEventListener = null;
                    }
            }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
