package com.sema.sema.activities;

import android.os.Build;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sema.sema.R;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    /*private String mPostKey = null;*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseChatroom, mDatabase, mDatabaseLastSeen;
    private FirebaseAuth mAuth;
    private ImageView searchBtn, mGroupIcon;
    private EditText searchInput;
    private Query mQueryMembers;
    private RecyclerView mMembersList;

    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_profile);

        /*android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.detail_toolbar);*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Window window = ProfileActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ProfileActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


       /* mPostKey = getIntent().getExtras().getString("heartraise_id");*/
        mAuth = FirebaseAuth.getInstance();
        mGroupIcon = (ImageView) findViewById(R.id.user_avator);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
      /*  mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(mPostKey).child("members");*/
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLastSeen.keepSynced(true);

        collapsingToolbarLayout= (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);

       /* mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                Picasso.with(ProfileActivity.this).load(image).into(mGroupIcon);

                // collapsingToolbarLayout= (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);
                collapsingToolbarLayout.setTitle(name);


                TextView mStarus = (TextView) findViewById(R.id.post_status);
                mStarus.setText(status);

                TextView mJoined = (TextView) findViewById(R.id.post_date);
                mJoined.setText(date);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("status") || dataSnapshot.hasChild("city") || dataSnapshot.hasChild("address")) {

                    String user_status = dataSnapshot.child("status").getValue().toString();
                    String user_last_seen = dataSnapshot.child("last_seen").getValue().toString();
                    String user_address = dataSnapshot.child("address").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();


                    Picasso.with(ProfileActivity.this).load(image).into(mGroupIcon);

                    collapsingToolbarLayout.setTitle(name);

                    TextView country = (TextView) findViewById(R.id.post_status);
                    country.setText(user_status);

                    TextView city = (TextView) findViewById(R.id.post_last_seen);
                    city.setText(user_last_seen);

                    TextView address = (TextView) findViewById(R.id.post_address);
                    address.setText(user_address);

                } else {}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }
}
