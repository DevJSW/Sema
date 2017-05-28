package com.sema.sema;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendsActivity extends AppCompatActivity  implements SearchView.OnQueryTextListener{

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Intent.ACTION_SEND);
                myIntent.setType("text/plain");
                String shareBody ="Hi, lets start chatting on Sema app. download it from the link below https://play.google.com/store/apps/details?id=com.sema.sema";
                String shareSub = "Hey ";
                myIntent.putExtra(Intent.EXTRA_SUBJECT,shareBody);
                myIntent.putExtra(Intent.EXTRA_TEXT,shareBody);
                startActivity(Intent.createChooser(myIntent,"Invite a friend"));
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

                final String  post_key = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(getApplicationContext(), model.getImage());

                // open chatroom activity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(AddFriendsActivity.this, ChatroomActivity.class);
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_menu, menu);
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
    public boolean onQueryTextChange(String newText) {

        FirebaseRecyclerAdapter<People, LetterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<People, LetterViewHolder>(

                People.class,
                R.layout.member2_row,
                LetterViewHolder.class,
                mDatabaseUsers.orderByChild("name").startAt(newText.toUpperCase())


        ) {
            @Override
            protected void populateViewHolder(LetterViewHolder viewHolder, People model, int position) {
                final String post_key = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setImage(getApplicationContext(), model.getImage());

                // open chatroom activity
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent cardonClick = new Intent(AddFriendsActivity.this, ChatroomActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key );
                        startActivity(cardonClick);
                    }
                });

            }
        };
        mMembersList.setAdapter(firebaseRecyclerAdapter);
        return false;


    }


}


