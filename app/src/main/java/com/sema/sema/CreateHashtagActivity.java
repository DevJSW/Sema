package com.sema.sema;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateHashtagActivity extends AppCompatActivity {

    private EditText inputHashtag;
    private CircleImageView mPostUserimg;
    private DatabaseReference mDatabaseUsers, mDatabaseHashtag;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private ProgressDialog mprogress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hashtag);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();

        mprogress = new ProgressDialog(this);
        inputHashtag = (EditText) findViewById(R.id.hashtagInput);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseHashtag = FirebaseDatabase.getInstance().getReference().child("Hashtag");

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_hashtag_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:
                if (id == R.id.action_sendBtn) {

                    Date date = new Date();
                    final String stringDate = DateFormat.getDateTimeInstance().format(date);

                    final String status = inputHashtag.getText().toString().trim();
                    if (!TextUtils.isEmpty(status)) {

                        final DatabaseReference newPost = mDatabaseHashtag;

                        // adding my user uid to hashtag chatroom
                        final DatabaseReference newPost2 = mDatabaseHashtag.child("hashtag_chatroom").push();

                        mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                // getting user uid
                                newPost.child("hashtag").setValue(inputHashtag);
                                newPost.child("uid").setValue(dataSnapshot.child("uid").getValue());
                                newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                                newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                                newPost.child("date").setValue(stringDate);

                                newPost2.child(auth.getCurrentUser().getUid()).child("uid").setValue(dataSnapshot.getValue());


                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }


                }

                return super.onOptionsItemSelected(item);
        }
    }

}
