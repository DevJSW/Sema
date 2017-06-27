package com.sema.sema;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class SendCameraActivity extends AppCompatActivity {

    private String mPostKey = null;

    private ImageView mAddPhoto;
    private EditText mCaption;
    private RecyclerView mCommentList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabasePostUser;
    private DatabaseReference mDatabaseComment;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabasePostComments;
    private Query mQueryPostComment;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;
    private ProgressDialog mProgress;

    Uri imageUri;
    Uri selectedImageUri = null;
    final int TAKE_PICTURE = 115;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_photo2);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAddPhoto = (ImageView) findViewById(R.id.addPhoto);
        mAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        mCaption = (EditText) findViewById(R.id.captionInput);
        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mProgress = new ProgressDialog(this);


        mAuth = FirebaseAuth.getInstance();
        mPostKey = getIntent().getExtras().getString("heartraise_id");

        mDatabasePostComments = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mQueryPostComment = mDatabasePostComments.orderByChild("post_key").equalTo(mPostKey);

        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mCommentList = (RecyclerView) findViewById(R.id.comment_list);
        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseComment.keepSynced(true);
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabasePostUser = FirebaseDatabase.getInstance().getReference().child("Users");

        mDatabaseComment.keepSynced(true);
        mDatabase.keepSynced(true);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startPosting();

            }

        });

        takePhoto();
    }

    private void startPosting() {

        mProgress.setMessage("Send photo, please wait...");
        mProgress.setCancelable(false);

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        final String caption_val = mCaption.getText().toString().trim();

        final String user_id = mAuth.getCurrentUser().getUid();
        final String uid = user_id.substring(0, Math.min(user_id.length(), 4));

        if (imageUri != null) {

            mProgress.show();

            StorageReference filepath = mStorage.child("chats_images").child(imageUri.getLastPathSegment());


            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newPostTap = mDatabaseComment.child(mPostKey);
                    final DatabaseReference newPostTab2 = mDatabaseComment.child(mAuth.getCurrentUser().getUid());

                    final DatabaseReference newPost = mDatabaseComment.child(mPostKey).child(mCurrentUser.getUid()).push();
                    final DatabaseReference newPost2 =mDatabaseComment.child(mCurrentUser.getUid()).child(mPostKey).push();

                    mDatabasePostUser.child(mPostKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            // getting user details
                            final String reciever_name = (String) dataSnapshot.child("name").getValue();
                            final String reciever_image = (String) dataSnapshot.child("image").getValue();

                            final String reciever_uid = (String) dataSnapshot.child("uid").getValue();


                            mDatabaseUser.addValueEventListener(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // reciever chat
                                    newPostTap.child("message").setValue(caption_val);
                                    newPostTap.child("uid").setValue(mCurrentUser.getUid());
                                    newPostTap.child("name").setValue(reciever_name);
                                    newPostTap.child("image").setValue(reciever_image);
                                    newPostTap.child("sender_uid").setValue(mCurrentUser.getUid());
                                    newPostTap.child("date").setValue(stringDate);
                                    newPostTap.child("post_key").setValue(mPostKey);

                                    // unread
                                    // newPost2Unread.child("message").setValue(message_val);

                                    newPostTab2.child("message").setValue(caption_val);
                                    newPostTab2.child("uid").setValue(mCurrentUser.getUid());
                                    newPostTab2.child("name").setValue(dataSnapshot.child("name").getValue());
                                    newPostTab2.child("image").setValue(dataSnapshot.child("image").getValue());
                                    newPostTab2.child("sender_uid").setValue(mPostKey);
                                    newPostTab2.child("date").setValue(stringDate);
                                    newPostTab2.child("post_key").setValue(mPostKey);


                                    newPost.child("message").setValue(caption_val);
                                    newPost.child("photo").setValue(downloadUrl.toString());
                                    newPost.child("uid").setValue(mAuth.getCurrentUser().getUid());
                                    newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                                    newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                                    newPost.child("sender_uid").setValue(mCurrentUser.getUid());
                                    newPost.child("date").setValue(stringDate);
                                    newPost.child("post_key").setValue(mPostKey);


                                    newPost2.child("message").setValue(caption_val);
                                    newPost2.child("photo").setValue(downloadUrl.toString());
                                    newPost2.child("uid").setValue(mAuth.getCurrentUser().getUid());
                                    newPost2.child("name").setValue(dataSnapshot.child("name").getValue());
                                    newPost2.child("image").setValue(dataSnapshot.child("image").getValue());
                                    newPost2.child("sender_uid").setValue(mCurrentUser.getUid());
                                    newPost2.child("date").setValue(stringDate);
                                    newPost2.child("post_key").setValue(mPostKey);
                                    newPost2.child("change_chat_icon").setValue(mPostKey);


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    mProgress.dismiss();

                    Toast.makeText(SendCameraActivity.this, "photo sent", Toast.LENGTH_LONG).show();
                    finish();

                }


            });

        }
    }

    private void takePhoto() {

        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photoFile = new File(Environment.getExternalStorageDirectory(),  "Photo.png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photoFile));
        imageUri = Uri.fromFile(photoFile);
        startActivityForResult(intent, TAKE_PICTURE);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:

                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = imageUri;
                    mAddPhoto.setImageURI(selectedImageUri);
                }
        }
    }

}
