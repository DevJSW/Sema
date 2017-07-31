package com.sema.sema;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daimajia.slider.library.SliderLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TrendsActivity extends AppCompatActivity {

    private String mPostKey = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseViews;
    private FirebaseAuth mAuth;
    private ImageView searchBtn, backBtn;
    private EditText searchInput;
    private Query mQueryMembers, mQueryTrends;
    private RecyclerView mMembersList;
    private LinearLayoutManager mLayoutManager;

    private SliderLayout mDemoSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("all_hashtags");
        mDatabaseViews = FirebaseDatabase.getInstance().getReference().child("hash_views");
        mQueryMembers = mDatabaseUsers.orderByChild("hashtag").startAt(mPostKey);
        mQueryTrends = mDatabaseUsers.orderByChild("trends").limitToLast(10);
        mMembersList = (RecyclerView) findViewById(R.id.Members_list);
        mMembersList.setLayoutManager(new LinearLayoutManager(this));
        mMembersList.setHasFixedSize(true);

        mDatabaseUsers.keepSynced(true);

        //the slider or image carousel
        mDemoSlider = (SliderLayout) findViewById(R.id.slider);

    }
    void refreshItems() {
        // Load items
        // ...

        // Load complete
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<People, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<People, LetterViewHolder>(

                People.class,
                R.layout.hashtag_row,
                LetterViewHolder.class,
                mQueryTrends

        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final People model, int position) {

                final String PostKey = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setDate(model.getDate());
                viewHolder.setMessage(model.getMessage());
                viewHolder.setHashtag(model.getHashtag());
                viewHolder.setImage(getApplicationContext(), model.getImage());

                // open chatroom activity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDatabaseViews.child(PostKey).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                        Intent cardonClick = new Intent(TrendsActivity.this, HashChatroomActivity.class);
                        cardonClick.putExtra("heartraise_id", PostKey );
                        startActivity(cardonClick);
                    }
                });

            }

        };
        final LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mLayoutManager = new LinearLayoutManager(TrendsActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mMembersList.setAdapter(firebaseRecyclerAdapter);
        mMembersList.setLayoutManager(mLayoutManager);
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

        public void setMessage(String message) {

            TextView post_message = (TextView) mView.findViewById(R.id.post_message);
            post_message.setText(message);
        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setHashtag(String hashtag) {

            TextView post_hashtag = (TextView) mView.findViewById(R.id.post_hashtag);
            post_hashtag.setText(hashtag);
        }

        public void setDate(String date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(date);
        }
        public void setImage(final Context ctx, final String image) {

            final CircleImageView civ = (CircleImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(civ, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {


                    Picasso.with(ctx).load(image).into(civ);
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

