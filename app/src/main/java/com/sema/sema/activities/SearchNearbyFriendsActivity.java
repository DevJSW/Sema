package com.sema.sema.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchNearbyFriendsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

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
        setContentView(R.layout.activity_search_nearby_friends);

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
                mDatabaseUsers.orderByChild("location").equalTo(mAuth.getCurrentUser().getUid())


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final People model, int position) {

                final String  post_key = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(getApplicationContext(), model.getImage());

                // open chatroom activity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(SearchNearbyFriendsActivity.this, ChatroomActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);

                    }
                });

            }

        };

        mMembersList.setAdapter(firebaseRecyclerAdapter);

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

        public void setStatus(String status) {

            TextView post_status = (TextView) mView.findViewById(R.id.status);
            post_status.setText(status);
        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
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

        public void setCountry(String country) {

            TextView post_country = (TextView) mView.findViewById(R.id.post_country);
            post_country.setText(country);


        }

        public void setCity(String city) {

            TextView post_city = (TextView) mView.findViewById(R.id.post_city);
            post_city.setText(city);


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu_, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);
        return true;
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

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {

        FirebaseRecyclerAdapter<People, LetterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<People, LetterViewHolder>(

                People.class,
                R.layout.member2_row,
                LetterViewHolder.class,
                mDatabaseUsers.orderByChild("address").startAt(newText.toUpperCase())


        ) {
            @Override
            protected void populateViewHolder(LetterViewHolder viewHolder, People model, int position) {
                final String post_key = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setCity(model.getCity());
                viewHolder.setCountry(model.getCountry());

                // open chatroom activity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(SearchNearbyFriendsActivity.this, ChatroomActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
                    }
                });

                mDatabaseUsers.orderByChild("address").startAt(newText.toUpperCase()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == null){

                            TextView mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);
                            ImageView mNoPostImg = (ImageView) findViewById(R.id.noPostChat);

                            mNoPostImg.setVisibility(View.VISIBLE);
                            mNoPostTxt.setVisibility(View.VISIBLE);
                        } else {

                            TextView mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);
                            ImageView mNoPostImg = (ImageView) findViewById(R.id.noPostChat);

                            mNoPostImg.setVisibility(View.GONE);
                            mNoPostTxt.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mMembersList.setAdapter(firebaseRecyclerAdapter);
        return false;


    }


}
