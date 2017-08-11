package com.sema.sema.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sema.sema.R;
import com.sema.sema.chatrooms.ChatroomActivity;
import com.sema.sema.models.People;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    /*private String mPostKey = null;*/
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseUsers2, mDatabase, mDatabaseLastSeen;
    private FloatingActionButton fabAlbum;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ImageView searchBtn, mGroupIcon;
    private EditText searchInput;
    private ProgressDialog mProgress;
    private RecyclerView mAlbumList;

    private Uri mImageUri = null;
    private static int GALLERY_REQUEST =1;

    Uri imageUri;
    Uri selectedImageUri = null;
    final int TAKE_PICTURE = 115;

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
        mDatabaseUsers2 = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
      /*  mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(mPostKey).child("members");*/
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mProgress = new ProgressDialog(this);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabaseUsers.keepSynced(true);
        mDatabaseLastSeen.keepSynced(true);

        mAlbumList = (RecyclerView) findViewById(R.id.album_list);
        mAlbumList.setHasFixedSize(true);
        mAlbumList.setLayoutManager(new GridLayoutManager(this, 3));

       /* mAlbumList = (RecyclerView) findViewById(R.id.album_list);
        mAlbumList.setHasFixedSize(true);

        LinearLayoutManager albumLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mAlbumList.setLayoutManager(albumLayoutManager);*/


        collapsingToolbarLayout= (CollapsingToolbarLayout) findViewById(R.id.cardInfo_collapsing);


        fabAlbum = (FloatingActionButton) findViewById(R.id.fab);
        fabAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Context context = ProfileActivity.this;

                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.popup_dialog_album);
                dialog.setTitle("Add to Album");

                // set the custom dialog components - text, image and button
                // send current user uid to block user child
                LinearLayout blockLiny = (LinearLayout) dialog.findViewById(R.id.blockLiny);
                blockLiny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // push uid to block user child in database

                        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, GALLERY_REQUEST);

                       /* sendToAlbum();*/
                        dialog.dismiss();
                    }
                });

                LinearLayout deleteLiny = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                deleteLiny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                        File photoFile = new File(Environment.getExternalStorageDirectory(),  "Photo.png");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                        imageUri = Uri.fromFile(photoFile);
                        startActivityForResult(intent, TAKE_PICTURE);

                       /* sendToAlbum();*/

                        dialog.dismiss();
                    }
                });


                // if button is clicked, close the custom dialog

                dialog.show();

            }
        });

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

        final TextView mNoAnsTxt = (TextView) findViewById(R.id.noAnsTxt);
        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("album").addValueEventListener(new ValueEventListener() {
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

    private void sendToAlbum() {

        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this, R.style.CustomAppTheme);
        pd.setMessage("Posting to Album");
        pd.show();

        StorageReference filepath = mStorage.child("users_albums").child(mImageUri.getLastPathSegment());

        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("album").push().child("photo").setValue(downloadUrl.toString());

                pd.dismiss();

                Toast.makeText(ProfileActivity.this, "Posted to Album", Toast.LENGTH_LONG).show();

            }


        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();
           // mAddPhoto.setImageURI(mImageUri);
            sendToAlbum();

        }
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri mImageUri = imageUri;
                    sendToAlbum();
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();



        FirebaseRecyclerAdapter<People, ProfileActivity.LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<People, ProfileActivity.LetterViewHolder>(

                People.class,
                R.layout.photo_grid,
                ProfileActivity.LetterViewHolder.class,
                mDatabaseUsers2.child("album")


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
