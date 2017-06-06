package com.sema.sema;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private String mPostKey = null;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers;
    private FirebaseAuth mAuth;
    private ImageView searchBtn, backBtn;
    private EditText searchInput;
    private Query mQueryMembers;
    private RecyclerView mMembersList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardonClick = new Intent(FriendsActivity.this, AddFriendsActivity.class);
                startActivity(cardonClick);
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });


        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mMembersList = (RecyclerView) findViewById(R.id.Members_list);
        mMembersList.setLayoutManager(new LinearLayoutManager(this));
        mMembersList.setHasFixedSize(true);

        mDatabaseUsers.keepSynced(true);

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
                R.layout.member2_row,
                LetterViewHolder.class,
                mDatabaseUsers


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final People model, int position) {

                final String PostKey = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(getApplicationContext(), model.getImage());

                // open chatroom activity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent cardonClick = new Intent(FriendsActivity.this, ChatroomActivity.class);
                        cardonClick.putExtra("heartraise_id", PostKey );
                        startActivity(cardonClick);
                    }
                });

                viewHolder.mChatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(FriendsActivity.this, ViewProfileActivity.class);
                        cardonClick.putExtra("heartraise_id", PostKey );
                        startActivity(cardonClick);
                    }
                });

                mDatabaseUsers.child(PostKey).child("location").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String city = dataSnapshot.child("city").getValue().toString();
                        String locality = dataSnapshot.child("address").getValue().toString();

                        viewHolder.city.setText(city);
                        viewHolder.locality.setText(locality);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

        };

        mMembersList.setAdapter(firebaseRecyclerAdapter);

    }



    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        CircleImageView mChatBtn;

        TextView city, locality;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mChatBtn = (CircleImageView) mView.findViewById(R.id.post_image);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
            city = (TextView) mView.findViewById(R.id.post_city);
            locality = (TextView) mView.findViewById(R.id.post_locality);
        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setStatus(String status) {

            TextView post_status = (TextView) mView.findViewById(R.id.status);
            post_status.setText(status);
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


}
