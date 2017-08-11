package com.sema.sema.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sema.sema.R;
import com.sema.sema.chatrooms.ChatroomActivity;
import com.sema.sema.models.People;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ViewProfileActivity extends AppCompatActivity {

    private String mPostKey = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseChatroom, mDatabase, mDatabaseLastSeen;
    private FirebaseAuth mAuth;
    private ImageView searchBtn, mGroupIcon;
    private EditText searchInput;
    private Query mQueryMembers;
    private RecyclerView mAlbumList;

    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile2);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Window window = ViewProfileActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ViewProfileActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cardonClick = new Intent(ViewProfileActivity.this, ChatroomActivity.class);
                cardonClick.putExtra("heartraise_id", mPostKey );
                startActivity(cardonClick);
            }
        });

        mPostKey = getIntent().getExtras().getString("heartraise_id");
        mAuth = FirebaseAuth.getInstance();
        mGroupIcon = (ImageView) findViewById(R.id.user_avator);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(mPostKey).child("members");
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLastSeen.keepSynced(true);
        mDatabaseChatroom.keepSynced(true);

        mAlbumList = (RecyclerView) findViewById(R.id.album_list);
        mAlbumList.setHasFixedSize(true);
        mAlbumList.setLayoutManager(new GridLayoutManager(this, 3));

        collapsingToolbarLayout= (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);

        mDatabaseUsers.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("status") || dataSnapshot.hasChild("city") || dataSnapshot.hasChild("address")) {

                    String user_status = dataSnapshot.child("status").getValue().toString();
                    String user_last_seen = dataSnapshot.child("last_seen").getValue().toString();
                    String user_address = dataSnapshot.child("address").getValue().toString();
                    String name = dataSnapshot.child("name").getValue().toString();
                    String image = dataSnapshot.child("image").getValue().toString();


                    Picasso.with(ViewProfileActivity.this).load(image).into(mGroupIcon);

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

                    /*TextView country = (TextView) findViewById(R.id.post_country);
                    country.setText(user_country);

                    TextView city = (TextView) findViewById(R.id.post_city);
                    city.setText(user_city);*/

                    TextView address = (TextView) findViewById(R.id.post_address);
                    address.setText(user_address);
                } else {}

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final TextView mNoAnsTxt = (TextView) findViewById(R.id.noAnsTxt);
        mDatabaseUsers.child(mPostKey).child("album").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null){

                    mNoAnsTxt.setVisibility(View.VISIBLE);
                } else {
                    mNoAnsTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<People, ProfileActivity.LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<People, ProfileActivity.LetterViewHolder>(

                People.class,
                R.layout.photo_grid,
                ProfileActivity.LetterViewHolder.class,
                mDatabaseUsers.child(mPostKey).child("album")


        ) {
            @Override
            protected void populateViewHolder(final ProfileActivity.LetterViewHolder viewHolder, final People model, int position) {

                final String  post_key = getRef(position).getKey();

                viewHolder.setPhoto(getApplicationContext(), model.getPhoto());

                // open chatroom activity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

            }

        };

        mAlbumList.setAdapter(firebaseRecyclerAdapter);

    }


    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        Button mChatBtn;

        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            //  mChatBtn = (Button) mView.findViewById(R.id.chatBtn);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);

        }

        public void setPhoto(final Context ctx, final String photo) {

            final ImageView civ = (ImageView) mView.findViewById(R.id.post_photo);

            Picasso.with(ctx).load(photo).networkPolicy(NetworkPolicy.OFFLINE).into(civ, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {


                    Picasso.with(ctx).load(photo).into(civ);
                }
            });
        }

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
