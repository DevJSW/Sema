package com.sema.sema;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class ViewHashtagActivity extends AppCompatActivity {

    private String mPostKey = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseChatroom, mDatabase, mDatabaseLastSeen, mDatabaseViews, mDatabaseLike, mDatabaseHashtag;
    private FirebaseAuth mAuth;
    private ImageView searchBtn, mGroupIcon;
    private LinearLayoutManager mediaLayoutManager;
    private EditText searchInput;
    private Query mQueryMembers;
    private RecyclerView mMediaList;

    CollapsingToolbarLayout collapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_hashtag);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        mPostKey = getIntent().getExtras().getString("heartraise_id");
        mAuth = FirebaseAuth.getInstance();
        mGroupIcon = (ImageView) findViewById(R.id.group_icon);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(mPostKey).child("members");
        mDatabaseViews = FirebaseDatabase.getInstance().getReference().child("hash_views");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLastSeen.keepSynced(true);
        mDatabaseChatroom.keepSynced(true);

        mMediaList = (RecyclerView) findViewById(R.id.media_list);
        mMediaList.setHasFixedSize(true);

        mediaLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mMediaList.setLayoutManager(mediaLayoutManager);


        collapsingToolbarLayout= (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);
        mDatabaseHashtag = FirebaseDatabase.getInstance().getReference().child("all_hashtags");
        mQueryMembers = mDatabaseHashtag.orderByChild("photo");
        mDatabaseHashtag.keepSynced(true);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("all_hashtags");
        mDatabase.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("hashtag").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();
                String name2 = dataSnapshot.child("name").getValue().toString();
                String date = dataSnapshot.child("date").getValue().toString();
                Picasso.with(ViewHashtagActivity.this).load(image).into(mGroupIcon);

                // collapsingToolbarLayout= (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);
                collapsingToolbarLayout.setTitle(name);

               // TextView mStarus = (TextView) findViewById(R.id.post_status);
             //   mStarus.setText(name2);

             //   TextView mJoined = (TextView) findViewById(R.id.post_date);
             //   mJoined.setText(date);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // count number of comments in a hashtag
        mDatabase.child(mPostKey).child("Chats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView chat_counter = (TextView) findViewById(R.id.post_comments);
                chat_counter.setText(dataSnapshot.getChildrenCount() + "");

                // send number of comments to current post
                mDatabase.child(mPostKey).child("trends").setValue(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of views in a hashtag
        mDatabaseViews.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView views_counter = (TextView) findViewById(R.id.post_views);
                views_counter.setText(dataSnapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of views in a hashtag
        mDatabaseLike.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView like_counter = (TextView) findViewById(R.id.post_favourite);
                like_counter.setText(dataSnapshot.getChildrenCount() + "");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Chat, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, CommentViewHolder>(

                Chat.class,
                R.layout.media_row,
                CommentViewHolder.class,
                mDatabaseHashtag.child(mPostKey).child("Media")


        ) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Chat model, final int position) {

                final String post_key = getRef(position).getKey();


                viewHolder.setPhoto(getApplicationContext(), model.getPhoto());


            }
        };

        mMediaList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseUnread;
        FirebaseAuth mAuth;
        ProgressBar mProgressBar;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;



            mAuth = FirebaseAuth.getInstance();

        }



        public void setPhoto(final Context ctx, final String photo) {
            final ImageView post_photo = (ImageView) mView.findViewById(R.id.post_photo);

            Picasso.with(ctx)
                    .load(photo)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(post_photo, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(ctx).load(photo).into(post_photo);
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
