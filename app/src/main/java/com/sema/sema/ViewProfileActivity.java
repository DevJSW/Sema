package com.sema.sema;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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
import com.squareup.picasso.Picasso;

public class ViewProfileActivity extends AppCompatActivity {

    private String mPostKey = null;
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
        setContentView(R.layout.activity_view_profile2);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cardonClick = new Intent(ViewProfileActivity.this, MapActivity.class);
                cardonClick.putExtra("heartraise_id", mPostKey );
                startActivity(cardonClick);
            }
        });

        mPostKey = getIntent().getExtras().getString("heartraise_id");
        mAuth = FirebaseAuth.getInstance();
        mGroupIcon = (ImageView) findViewById(R.id.group_icon);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(mPostKey).child("members");
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLastSeen.keepSynced(true);
        mDatabaseChatroom.keepSynced(true);

        collapsingToolbarLayout= (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                Picasso.with(ViewProfileActivity.this).load(image).into(mGroupIcon);

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
        });

        mDatabaseLastSeen.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String seen = dataSnapshot.child("last_seen").getValue().toString();

                TextView mSeen = (TextView) findViewById(R.id.post_last_seen);
                mSeen.setText(seen);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseUsers.child(mPostKey).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("country") || dataSnapshot.hasChild("city") || dataSnapshot.hasChild("address")) {

                    String user_country = dataSnapshot.child("country").getValue().toString();
                    String user_city = dataSnapshot.child("city").getValue().toString();
                    String user_address = dataSnapshot.child("address").getValue().toString();

                    TextView country = (TextView) findViewById(R.id.post_country);
                    country.setText(user_country);

                    TextView city = (TextView) findViewById(R.id.post_city);
                    city.setText(user_city);

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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}
