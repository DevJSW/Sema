package com.sema.sema.chatrooms;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import com.sema.sema.R;
import com.sema.sema.activities.SendCameraActivity;
import com.sema.sema.activities.SendPhotoActivity;
import com.sema.sema.activities.ViewProfileActivity;
import com.sema.sema.adapter.ChatAdapter;
import com.sema.sema.models.Chat;
import com.sema.sema.services.GPSTracker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class ChatroomActivity extends AppCompatActivity {

    private static final String TAG = ChatroomActivity.class.getSimpleName();

    Button b1, b2, b3, b4, b5, b6, b7, b8, b9;
    int turn;

    private String mPostKey = null;
    private TextView mNoPostTxt;
    private ImageView cameraBtn;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressDialog mProgress;
    private RecyclerView mCommentList;
    private DatabaseReference mDatabase, mDatabaseUsersTyping;
    private DatabaseReference mDatabaseComment,  mDatabaseUsers, mDatabaseChatroom, mDatabaseUnread, mDatabaseNotification,  mDatabaseLastSeen, mDatabaseTyping, mDatabaseTick;
    private DatabaseReference mDatabaseUser, mDatabaseLatestMessage,  mDatabaseUsersOnline;
    private DatabaseReference mDatabaseUser2;
    private DatabaseReference mDatabasePostChats;
    private DatabaseReference mDatabaseChatNotification;
    private Query mQueryPostChats;
    private Query mQueryInAscending;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorage;
    private FirebaseAuth mAuth;
    private ImageView mSendBtn, mGame;
    private EditText mCommentField;
    private Uri audioUri = null;
    private Uri videoUri = null;
    private static int AUDIO_REQUEST =1;
    private static int REQUEST_TAKE_GALLERY_VIDEO =1;
    private Boolean mProcessStopChat = false;
    private Menu menu;
    Context context = this;

    GPSTracker gps;
    Geocoder geocoder;
    List<Address> addresses;

   /* private LinearLayoutManager mLayoutManager;*/
    EmojiconEditText emojiconEditText;
    EmojiconTextView textView;
    ImageView emojiImageView;
    EmojIconActions emojIcon;
    View rootView;
    private Query mQueryChats;

    private Boolean isFabOpen = false;
    private FloatingActionButton fab,fab1,fab2,fab3,fab4,fab5;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;

    ChatAdapter chatAdapter;
    private final List<Chat> commentList = new ArrayList<>();
    LinearLayoutManager mLinearlayout;


    /** Called when the activity is first created. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.mCustomToolbarChat);
        setSupportActionBar(my_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //keep layout on top of keyboard

        // SETTING UP FONTS
        final Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");



        Window window = ChatroomActivity.this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor( ChatroomActivity.this,R.color.colorPrimaryDark));

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


        mStorage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(this);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseChatNotification = FirebaseDatabase.getInstance().getReference().child("chat_notifications");
        rootView = findViewById(R.id.root_view);
        emojiImageView = (ImageView) findViewById(R.id.emoji_btn);
        emojiconEditText = (EmojiconEditText) findViewById(R.id.emojicon_edit_text);
        emojiconEditText.setTypeface(custom_font);
        emojIcon = new EmojIconActions(this, rootView, emojiconEditText, emojiImageView);
        emojIcon.ShowEmojIcon();
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.e(TAG, "Keyboard opened!");
            }

            @Override
            public void onKeyboardClose() {
                Log.e(TAG, "Keyboard closed");
            }
        });
        //mNoPostTxt = (TextView) findViewById(R.id.noPostTxt);
        //final RelativeLayout hello = (RelativeLayout) findViewById(R.id.hello);

        /*initUserStatusIndicator();*/

        mAuth = FirebaseAuth.getInstance();
        mPostKey = getIntent().getExtras().getString("heartraise_id");

        /*mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).setValue("isOnline");*/

        mDatabasePostChats = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseUsersTyping = FirebaseDatabase.getInstance().getReference().child("users_typing");
        mDatabaseLatestMessage = FirebaseDatabase.getInstance().getReference().child("latest_messages");
        mDatabaseTyping = FirebaseDatabase.getInstance().getReference().child("Typing");
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mDatabaseUnread = FirebaseDatabase.getInstance().getReference().child("Unread");
        mDatabaseNotification = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mDatabaseTick = FirebaseDatabase.getInstance().getReference().child("Tick_watcher");
        mQueryPostChats = mDatabasePostChats.orderByChild("post_key").equalTo(mPostKey);
        mQueryChats = mDatabasePostChats.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());

        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mDatabaseUser2 = FirebaseDatabase.getInstance().getReference().child("Users");

        chatAdapter = new ChatAdapter(commentList);

        mCommentList = (RecyclerView) findViewById(R.id.comment_list);
        mLinearlayout = new LinearLayoutManager(ChatroomActivity.this);

        mCommentList.setHasFixedSize(true);
        mCommentList.setLayoutManager(mLinearlayout);

        mCommentList.setAdapter(chatAdapter);

        // clear unread messages
        mDatabaseUnread.child(mPostKey).child(mAuth.getCurrentUser().getUid()).removeValue();

      /*  mCommentList.setLayoutManager(new LinearLayoutManager(this));*/

        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseComment.keepSynced(true);
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseChatroom.keepSynced(true);
        mDatabaseUser.keepSynced(true);
        mDatabaseUnread.keepSynced(true);
        mDatabaseNotification.keepSynced(true);
        mDatabaseLatestMessage.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(mPostKey).child(mAuth.getCurrentUser().getUid());
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mQueryInAscending = mDatabase.orderByChild("date").startAt(-1 * new Date().getTime());
        mSendBtn = (ImageView) findViewById(R.id.sendBtn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

        mDatabaseUser2.child(mPostKey);
        mDatabaseUser2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String userimg = (String) dataSnapshot.child("image").getValue();
                final String username = (String) dataSnapshot.child("name").getValue();

                // load image on toolbar
                CircleImageView userImgToolbar = (CircleImageView) findViewById(R.id.toolbarImg);
                Picasso.with(ChatroomActivity.this).load(userimg).into(userImgToolbar);

                // set username on toolbar
                TextView toolbar_username = (TextView) findViewById(R.id.toolbar_username);
                toolbar_username.setText(username);
                toolbar_username.setTypeface(custom_font);


                mDatabaseUser2.child(mPostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String date = (String) dataSnapshot.child("last_seen").getValue().toString();
                        final String userimg = (String) dataSnapshot.child("image").getValue();
                        final String username = (String) dataSnapshot.child("name").getValue();
                        Boolean userOnline = (Boolean) dataSnapshot.child("isOnline").getValue();
                        Boolean user_typing = (Boolean) dataSnapshot.child("Typing").getValue();

                        // load image on toolbar
                        CircleImageView userImgToolbar = (CircleImageView) findViewById(R.id.toolbarImg);
                        Picasso.with(ChatroomActivity.this).load(userimg).into(userImgToolbar);

                        // set username on toolbar
                        TextView toolbar_username = (TextView) findViewById(R.id.toolbar_username);
                        toolbar_username.setText(username);

                        RelativeTimeTextView toolbar_last_seen = (RelativeTimeTextView) findViewById(R.id.toolbar_last_seen_date);
                       /* toolbar_last_seen.setReferenceTime(Long.parseLong(date));*/
                        toolbar_last_seen.setTypeface(custom_font);

                        if (dataSnapshot.hasChild("isOnline") || dataSnapshot.hasChild("Typing")) {
                             if (userOnline == true && user_typing == false) {

                                 toolbar_last_seen.setText("Online");

                                 toolbar_last_seen.setText("Typing...");

                             } else if (userOnline == true && user_typing == true){

                             }else {

                                 toolbar_last_seen.setReferenceTime(Long.parseLong(date));
                             }
                        } else {
                            toolbar_last_seen.setReferenceTime(Long.parseLong(date));
                        }


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

        CircleImageView userImgToolbar = (CircleImageView) findViewById(R.id.toolbarImg);
        userImgToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        LinearLayout tap_view = (LinearLayout) findViewById(R.id.open_view);
        tap_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent cardonClick = new Intent(ChatroomActivity.this, ViewProfileActivity.class);
                cardonClick.putExtra("heartraise_id", mPostKey );
                startActivity(cardonClick);
            }
        });

        ImageView camera = (ImageView) findViewById(R.id.quickShot);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardonClick = new Intent(ChatroomActivity.this, SendCameraActivity.class);
                cardonClick.putExtra("heartraise_id", mPostKey );
                startActivity(cardonClick);
            }
        });

        ImageView gameBtn = (ImageView) findViewById(R.id.ic_game);
        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cardonClick = new Intent(ChatroomActivity.this, SendPhotoActivity.class);
                cardonClick.putExtra("heartraise_id", mPostKey );
                startActivity(cardonClick);

               /* final Context context = ChatroomActivity.this;

                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.gamestudio_dialog);
                dialog.setTitle("Game");

                b1 = (Button)  dialog.findViewById(R.id.b1);
                b2 = (Button)  dialog.findViewById(R.id.b2);
                b3 = (Button)  dialog.findViewById(R.id.b3);
                b4 = (Button)  dialog.findViewById(R.id.b4);
                b5 = (Button)  dialog.findViewById(R.id.b5);
                b6 = (Button)  dialog.findViewById(R.id.b6);
                b7 = (Button)  dialog.findViewById(R.id.b7);
                b8 = (Button)  dialog.findViewById(R.id.b8);
                b9 = (Button)  dialog.findViewById(R.id.b9);

                turn = 1;

                b1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b1.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b1.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b1.setText("O");
                            }
                        }
                        endGame();
                    }
                });
                b2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b2.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b2.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b2.setText("O");
                            }
                        }
                        endGame();

                    }
                });
                b3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b3.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b3.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b3.setText("O");
                            }
                        }
                        endGame();
                    }
                });
                b4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b4.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b4.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b4.setText("O");
                            }
                        }
                        endGame();
                    }
                });
                b5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b5.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b5.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b5.setText("O");
                            }
                        }
                        endGame();
                    }
                });
                b6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b6.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b6.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b6.setText("O");
                            }
                        }
                        endGame();
                    }
                });
                b7.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b7.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b7.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b7.setText("O");
                            }
                        }
                        endGame();
                    }
                });
                b8.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b8.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b8.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b8.setText("O");
                            }
                        }
                        endGame();
                    }
                });
                b9.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (b9.getText().toString().equals("")) {

                            if (turn == 1) {
                                turn = 2;
                                b9.setText("X");
                            } else if (turn == 2) {
                                turn = 1;
                                b9.setText("O");
                            }
                        }
                        endGame();
                    }

                });


                // if button is clicked, close the custom dialog

                dialog.show();
*/
            }
        });

        mDatabaseUser2.child(mPostKey).child("location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("address")) {

                    String txt_city = dataSnapshot.child("city").getValue().toString();
                    String txt_locality = dataSnapshot.child("address").getValue().toString();


                    TextView locality = (TextView) findViewById(R.id.post_locality);
                    locality.setText(txt_locality);

                } else {

                    LinearLayout liny = (LinearLayout) findViewById(R.id.liny_loc);
                    liny.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        geocoder = new Geocoder(this, Locale.getDefault());

        // create class object
        gps = new GPSTracker(ChatroomActivity.this);
        // check if GPS enabled
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // \n is for new line
            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("latitude").setValue(latitude);
            mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("longitude").setValue(longitude);

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();

                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("city").setValue(city);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("country").setValue(country);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("address").setValue(address);

                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("address").setValue(address);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("city").setValue(city);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("state").setValue(state);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("country").setValue(country);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("postalCode").setValue(postalCode);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("location").child("knownName").setValue(knownName);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("city").setValue(city);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("country").setValue(country);
                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("address").setValue(address);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
        }

        mDatabaseUsersOnline = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        mDatabaseUsersOnline.child("isOnline").setValue(true);

        mDatabaseComment.keepSynced(true);

    }

    private void LoadMessage() {
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Chat message = dataSnapshot.getValue(Chat.class);

                commentList.add(message);
                chatAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mDatabaseUsersOnline.child("isOnline").setValue(false);
    }

   /* @Override
    protected void onStop() {
        super.onStop();
        mDatabaseUsersOnline.child("isOnline").setValue(false);
    }*/
    private void initOnlineListener()
    {
        DatabaseReference mDatabaseUsersOnline = FirebaseDatabase.getInstance().getReference().child("users_online");
        mDatabaseUsersOnline.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mPostKey))
                {
                    RelativeTimeTextView toolbar_last_seen = (RelativeTimeTextView) findViewById(R.id.toolbar_last_seen_date);
                    toolbar_last_seen.setText("is Online");

                } else
                {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initUserStatusIndicator() {
        FirebaseDatabase.getInstance().getReference().child("users_online").child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mPostKey == null) {
                    RelativeTimeTextView toolbar_last_seen = (RelativeTimeTextView) findViewById(R.id.toolbar_last_seen_date);
                    toolbar_last_seen.setText("is Online");
                } else {

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void endGame() {

        String a, b, c, d, e, f, g, h, i;
        Boolean end = false;

        a = b1.getText().toString();
        b = b2.getText().toString();
        c = b3.getText().toString();

        d = b4.getText().toString();
        e = b5.getText().toString();
        f = b6.getText().toString();

        g = b7.getText().toString();
        h = b8.getText().toString();
        i = b9.getText().toString();

        if(a.equals("X") && b.equals("X") && c.equals("X")) {

            Toast.makeText(ChatroomActivity.this, "Winner player X!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(a.equals("X") && e.equals("X") && i.equals("X")) {

            Toast.makeText(ChatroomActivity.this, "Winner player X!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(a.equals("X") && d.equals("X") && g.equals("X")) {

            Toast.makeText(ChatroomActivity.this, "Winner player X!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(b.equals("X") && e.equals("X") && h.equals("X")) {

            Toast.makeText(ChatroomActivity.this, "Winner player X!", Toast.LENGTH_LONG).show();
            end = true;
        }
        if(c.equals("X") && f.equals("X") && i.equals("X")) {

            Toast.makeText(ChatroomActivity.this, "Winner player X!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(d.equals("X") && e.equals("X") && f.equals("X")) {

            Toast.makeText(ChatroomActivity.this, "Winner player X!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(g.equals("X") && h.equals("X") && i.equals("X")) {

            Toast.makeText(ChatroomActivity.this, "Winner player X!", Toast.LENGTH_LONG).show();
            end = true;
        }



        if(a.equals("O") && b.equals("O") && c.equals("O")) {

            Toast.makeText(ChatroomActivity.this, "Winner player O!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(a.equals("O") && e.equals("O") && i.equals("O")) {

            Toast.makeText(ChatroomActivity.this, "Winner player O!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(a.equals("O") && d.equals("O") && g.equals("O")) {

            Toast.makeText(ChatroomActivity.this, "Winner player O!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(b.equals("O") && e.equals("O") && h.equals("O")) {

            Toast.makeText(ChatroomActivity.this, "Winner player O!", Toast.LENGTH_LONG).show();
            end = true;
        }
        if(c.equals("O") && f.equals("O") && i.equals("O")) {

            Toast.makeText(ChatroomActivity.this, "Winner player O!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(d.equals("O") && e.equals("O") && f.equals("O")) {

            Toast.makeText(ChatroomActivity.this, "Winner player O!", Toast.LENGTH_LONG).show();
            end = true;
        }

        if(g.equals("O") && h.equals("O") && i.equals("O")) {

            Toast.makeText(ChatroomActivity.this, "Winner player O!", Toast.LENGTH_LONG).show();
            end = true;
        }


        if (end) {
            b1.setEnabled(false);
            b2.setEnabled(false);
            b3.setEnabled(false);
            b4.setEnabled(false);
            b5.setEnabled(false);
            b6.setEnabled(false);
            b7.setEnabled(false);
            b8.setEnabled(false);
            b9.setEnabled(false);
        }

    }

    private void addToLastSeen() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        mDatabaseLastSeen.child(mAuth.getCurrentUser().getUid()).child("last_seen").setValue(System.currentTimeMillis());
    }

    private void startPosting() {
        // mProgress.setMessage("Posting...");

        Date date = new Date();
        final String stringDate = DateFormat.getTimeInstance().format(date);

        final String message_val = emojiconEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(message_val)) {

            //mProgress.show();
            //pushing chats into chat's tab

            final DatabaseReference newPostReceiverLatestMsg = mDatabaseLatestMessage.child(mPostKey).child(mAuth.getCurrentUser().getUid());
            final DatabaseReference newPostSenderLatestMsg = mDatabaseLatestMessage.child(mAuth.getCurrentUser().getUid()).child(mPostKey);

            //my screen
            final DatabaseReference newPost = mDatabaseChatroom.child(mPostKey).child(mCurrentUser.getUid()).push();
            //reviever screen
            final DatabaseReference newPost3 = mDatabaseChatroom.child(mCurrentUser.getUid()).child(mPostKey).push();


          /*  //post message to unread child
            final DatabaseReference newPost2Unread = mDatabaseUnread.child(mAuth.getCurrentUser().getUid()).child(mPostKey).push();

            final DatabaseReference newPostTick = mDatabaseTick.child(mAuth.getCurrentUser().getUid()).push();

            // SEND MESSAGE DETAILS TO NOTIFICATIONS DATABASE
            final DatabaseReference newPost2Notification = mDatabaseNotification.child(mPostKey).push();

            // post last active date to user data
            final DatabaseReference newPost4 = mDatabaseUser;*/



            mDatabaseUser2.child(mPostKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                    final String userimg = (String) dataSnapshot.child("image").getValue();
                    final String username = (String) dataSnapshot.child("name").getValue();
                    final String reveiver_uid = (String) dataSnapshot.child("uid").getValue();

                    // getting user details
                    final String reciever_name = (String) dataSnapshot.child("name").getValue();
                    final String reciever_image = (String) dataSnapshot.child("image").getValue();

                    mDatabaseUser.addValueEventListener(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            final String userimg2 = (String) dataSnapshot.child("image").getValue();
                            final String username2 = (String) dataSnapshot.child("name").getValue();

                            mProcessStopChat = true;

                            if (mProcessStopChat) {

                                // reciever chat
                                newPostReceiverLatestMsg.child("message").setValue(message_val);
                                newPostReceiverLatestMsg.child("uid").setValue(mCurrentUser.getUid());
                                newPostReceiverLatestMsg.child("name").setValue(dataSnapshot.child("name").getValue());
                                newPostReceiverLatestMsg.child("image").setValue(dataSnapshot.child("image").getValue());
                                newPostReceiverLatestMsg.child("sender_uid").setValue(mCurrentUser.getUid());
                                newPostReceiverLatestMsg.child("date").setValue(dataSnapshot.child("date").getValue());
                                newPostReceiverLatestMsg.child("post_key").setValue(mPostKey);


                                newPostSenderLatestMsg.child("message").setValue(message_val);
                                newPostSenderLatestMsg.child("uid").setValue(mCurrentUser.getUid());
                                newPostSenderLatestMsg.child("name").setValue(username);
                                newPostSenderLatestMsg.child("image").setValue(userimg);
                                newPostSenderLatestMsg.child("sender_uid").setValue(mPostKey);
                                newPostSenderLatestMsg.child("date").setValue(dataSnapshot.child("date").getValue());
                                newPostSenderLatestMsg.child("post_key").setValue(mPostKey);

                                //sender  screen
                                newPost.child("message").setValue(message_val);
                                newPost.child("uid").setValue(mCurrentUser.getUid());
                                newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                                newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                                newPost.child("sender_uid").setValue(mCurrentUser.getUid());
                                newPost.child("date").setValue(stringDate);
                                newPost.child("post_key").setValue(mPostKey);


                                // reciever screen
                                newPost3.child("message").setValue(message_val);
                                newPost3.child("uid").setValue(mCurrentUser.getUid());
                                newPost3.child("name").setValue(dataSnapshot.child("name").getValue());
                                newPost3.child("image").setValue(dataSnapshot.child("image").getValue());
                                newPost3.child("read").setValue(false);
                                newPost3.child("receiver_uid").setValue(reveiver_uid);
                                newPost3.child("date").setValue(stringDate);
                                newPost3.child("post_key").setValue(mPostKey);
                                newPost3.child("change_chat_icon").setValue(mPostKey);
                                newPost3.child("unread_listener").setValue(mPostKey);


                                //SEND NOTIFICATION
                                HashMap<String, String> NotificationData = new HashMap<>();
                                NotificationData.put("from", mCurrentUser.getUid());
                                NotificationData.put("message", message_val);
                                NotificationData.put("sent_date", stringDate);
                                NotificationData.put("post_key", mPostKey);

                                mDatabaseChatNotification.child(mPostKey).setValue(NotificationData);


                                //clear edit text after message has been sent
                                EditText edit = (EditText) findViewById(R.id.emojicon_edit_text);
                                edit.setText(null);

                                mProcessStopChat = false;

                            }

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

            // mProgress.dismiss();

        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        LoadMessage();
        mDatabaseUsersOnline.child("isOnline").setValue(true);

      /*  final FirebaseRecyclerAdapter<Chat, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, CommentViewHolder>(

                Chat.class,
                R.layout.chat_row,
                CommentViewHolder.class,
                mDatabase


        ) {


            @Override
            public int getItemViewType(int position) {
                final String post_key = getRef(position).getKey();

                return super.getItemViewType(position);
            }

            @Override
            public void onBindViewHolder(final CommentViewHolder holder, int position, List<Object> payloads) {

                final String post_key = getRef(position).getKey();

                mDatabaseComment.child(mPostKey).child(mCurrentUser.getUid()).child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_photo = (String) dataSnapshot.child("photo").getValue();
                        final String chat_icon = (String) dataSnapshot.child("change_chat_icon").getValue();

                        if (post_photo != null) {

                            holder.mCardPhoto.setVisibility(View.VISIBLE);
                            holder.min_lay.setVisibility(View.GONE);

                            holder.mCardPhoto2.setVisibility(View.VISIBLE);
                            holder.min_lay2.setVisibility(View.GONE);

                            // if card has my uid, then change chat balloon shape
                        } else {

                            holder.mCardPhoto.setVisibility(View.GONE);
                            holder.min_lay.setVisibility(View.VISIBLE);

                            holder.mCardPhoto2.setVisibility(View.GONE);
                            holder.min_lay2.setVisibility(View.VISIBLE);

                        }

                        if (chat_icon == null) {

                            holder.rely.setVisibility(View.VISIBLE);
                            holder.liny.setVisibility(View.GONE);

                            // if card has my uid, then change chat balloon shape
                        } else {

                            holder.rely.setVisibility(View.GONE);
                            holder.liny.setVisibility(View.VISIBLE);
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                super.onBindViewHolder(holder, position, payloads);
            }

            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Chat model, final int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setMessage(model.getMessage());
                viewHolder.setDate(model.getDate());
                viewHolder.setPhoto(getApplicationContext(), model.getPhoto());
                // viewHolder.setName(model.getName());
                // viewHolder.setImage(getApplicationContext(), model.getImage());


                // delete unread listener
                mDatabaseChatroom.child(mPostKey).child(mAuth.getCurrentUser().getUid()).child(post_key).child("unread_listener").removeValue();



            }
        };*/
        final LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
       /* mCommentList.setAdapter(firebaseRecyclerAdapter);*/


      /*  mLayoutManager = new LinearLayoutManager(ChatroomActivity.this);
        // mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(true);

        // Now set the layout manager and the adapter to the RecyclerView
        mCommentList.setLayoutManager(mLayoutManager);
*/
        //mRecycler.smoothScrollToPosition(mMessagesAdapter.getItemCount() - 1);
        mCommentList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, final int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mCommentList.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCommentList.smoothScrollToPosition(bottom);
                    }
                }, 0);
            }
        });


        /*firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();

                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mLayoutManager.setStackFromEnd(true);
                    mCommentList.scrollToPosition(positionStart);
                }
            }
        });

*/

        final long delay = 1000; // 1 seconds after user stops typing
        final long[] last_text_edit = {0};
        final Handler handler = new Handler();

        final Runnable input_finish_checker = new Runnable() {
            public void run() {
                if (System.currentTimeMillis() > (last_text_edit[0])) {
                    // ............
                    // ............

                   /* //show typing
                    mDatabaseUsers.child("Typing").child(mPostKey).child(mAuth.getCurrentUser().getUid()).setValue("Typing");
                    // IF THERE IS UNREAD MESSAGE, DELETE.
                    mDatabaseUnread.child(mPostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                    // remove date
                    mDatabaseLastSeen.child(mAuth.getCurrentUser().getUid()).removeValue();*/

                }
            }
        };

        //checking if a user is typing
        EditText editText = (EditText) findViewById(R.id.emojicon_edit_text);
        editText.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged (CharSequence s,int start, int count,
                                                                           int after){
                                            }
                                            @Override
                                            public void onTextChanged ( final CharSequence s, int start, int before,
                                                                        int count){
                                                //You need to remove this to run only once
                                                handler.removeCallbacks(input_finish_checker);

                                            }
                                            @Override
                                            public void afterTextChanged ( final Editable s){
                                                //avoid triggering event when text is empty
                                                if (s.length() > 0) {
                                                    last_text_edit[0] = System.currentTimeMillis();
                                                    handler.postDelayed(input_finish_checker, delay);

                                                    // HIDE AUDIO BUTTON WHILE USER IS TYPING
                                                    ImageView game = (ImageView) findViewById(R.id.ic_game);
                                                    game.setVisibility(View.GONE);

                                                    ImageView camera = (ImageView) findViewById(R.id.quickShot);
                                                    camera.setVisibility(View.GONE);

                                                    ImageView sendy = (ImageView) findViewById(R.id.sendBtn);
                                                    sendy.setVisibility(View.VISIBLE);


                                                } else {

                                                                /*Date date = new Date();
                                                                final String stringDate = DateFormat.getDateTimeInstance().format(date);
*

                                                    //hide/ remove typing
                                                    /*mDatabaseUsers.child("Typing").child(mPostKey).child(mAuth.getCurrentUser().getUid()).removeValue();*/


                                                    // SHOW AUDIO BUTTON WHILE USER IS TYPING
                                                    ImageView game = (ImageView) findViewById(R.id.ic_game);
                                                    game.setVisibility(View.VISIBLE);

                                                    ImageView camera = (ImageView) findViewById(R.id.quickShot);
                                                    camera.setVisibility(View.VISIBLE);

                                                    ImageView sendy = (ImageView) findViewById(R.id.sendBtn);
                                                    sendy.setVisibility(View.GONE);

                                                }
                                            }
                                        }

        );


        EditText editText2 = (EditText) findViewById(R.id.emojicon_edit_text);
        editText2.addTextChangedListener(new TextWatcher() {

            boolean isTyping = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            private Timer timer = new Timer();
            private final long DELAY = 2000; // milliseconds

            @Override
            public void afterTextChanged(final Editable s) {
                Log.d("", "");
                if(!isTyping) {
                    Log.d(TAG, "started typing");
                    // Send notification for start typing event
                    mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("Typing").setValue(true);

                    isTyping = true;
                }
                timer.cancel();
                timer = new Timer();
                timer.schedule(
                        new TimerTask() {
                            @Override
                            public void run() {
                                isTyping = false;
                                Log.d(TAG, "stopped typing");
                                //send notification for stopped typing event
                                mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("Typing").setValue(false);

                            }
                        },
                        DELAY
                );

            }
        });

    }



    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseUnread;
        FirebaseAuth mAuth;
        ImageView mImage,  mImage2, groupIcon, mSingleTick, mDoubleTick;
        TextView tx, tx2, txname, txname2, txdate, txdate2, txcaption, txcaption2;
        RelativeLayout rely, min_lay2;
        LinearLayout liny,  mCardPhoto, mCardPhoto2, min_lay;
        ProgressBar mProgressBar;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mCardPhoto = (LinearLayout) mView.findViewById(R.id.chat_image);
            tx = (TextView) mView.findViewById(R.id.post_message);
            tx2 = (TextView) mView.findViewById(R.id.post_message2);
            txname = (TextView) mView.findViewById(R.id.post_name);
            txdate = (TextView) mView.findViewById(R.id.post_date);
            txdate2 = (TextView) mView.findViewById(R.id.post_date2);
            txcaption = (TextView) mView.findViewById(R.id.captionInput);
            txcaption2 = (TextView) mView.findViewById(R.id.captionInput2);
            min_lay = (LinearLayout) mView.findViewById(R.id.main_lay);
            mImage = (ImageView) mView.findViewById(R.id.post_image);
            mCardPhoto2 = (LinearLayout) mView.findViewById(R.id.chat_image2);
            min_lay2 = (RelativeLayout) mView.findViewById(R.id.chat_balloon);
            mImage2 = (ImageView) mView.findViewById(R.id.post_image2);
            //  groupIcon = (ImageView) mView.findViewById(R.id.group_icon);
            liny = (LinearLayout) mView.findViewById(R.id.liny);
            rely = (RelativeLayout) mView.findViewById(R.id.rely);
            mDoubleTick = (ImageView)mView.findViewById(R.id.double_tick);
            mAuth = FirebaseAuth.getInstance();
            mDatabaseUnread = FirebaseDatabase.getInstance().getReference().child("Unread");
            mDatabaseUnread.keepSynced(true);


        }


        public void setMessage(String message) {

            TextView post_message = (TextView) mView.findViewById(R.id.post_message);
            post_message.setText(message);

            TextView post_message2 = (TextView) mView.findViewById(R.id.post_message2);
            post_message2.setText(message);

            TextView post_caption = (TextView) mView.findViewById(R.id.captionInput);
            post_caption.setText(message);

            TextView post_caption2 = (TextView) mView.findViewById(R.id.captionInput2);
            post_caption2.setText(message);
        }


        public void setDate(String date) {

            RelativeTimeTextView post_date = (RelativeTimeTextView) mView.findViewById(R.id.post_date);
            post_date.setText(date);

            RelativeTimeTextView post_date2 = (RelativeTimeTextView) mView.findViewById(R.id.post_date2);
            post_date2.setText(date);
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

            final ImageView post_photo2 = (ImageView) mView.findViewById(R.id.post_photo2);

            Picasso.with(ctx)
                    .load(photo)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(post_photo2, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {


                            Picasso.with(ctx).load(photo).into(post_photo2);
                        }
                    });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;
            default:
                if (id == R.id.action_gallery) {

                    Intent cardonClick = new Intent(ChatroomActivity.this, SendPhotoActivity.class);
                    cardonClick.putExtra("heartraise_id", mPostKey );
                    startActivity(cardonClick);


                } else if (id == R.id.action_audio) {

                    Intent audioIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    audioUri = Uri.fromFile(new File("path/to/audio.mp3"));
                    audioIntent.setType("audio/mpeg");
                    startActivityForResult(audioIntent, AUDIO_REQUEST);


                } else if (id == R.id.action_video) {

                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,"Select Video"),REQUEST_TAKE_GALLERY_VIDEO);


                }


                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).setValue("isOnline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == AUDIO_REQUEST && resultCode == RESULT_OK) {

            audioUri = data.getData();

        } else if (requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK) {

            videoUri = data.getData();
        }

        sendAudio();
    }

    private void sendAudio() {
        mProgress.setMessage("Loading audio, please wait...");

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        /*final String caption_val = mCaption.getText().toString().trim();*/

        final String user_id = mAuth.getCurrentUser().getUid();
        final String uid = user_id.substring(0, Math.min(user_id.length(), 4));

        if (audioUri != null) {

            mProgress.show();

           /* StorageReference filepath = mStorage.child("chats_images").child(mImageUri.getLastPathSegment());
*/
            StorageMetadata metadata = new StorageMetadata.Builder().setContentType("audio/mpeg").build();
            UploadTask uploadTask = mStorage.child("audio/"+audioUri.getLastPathSegment()).putFile(audioUri, metadata);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });

            // Listen for state changes, errors, and completion of the upload.
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (10 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                    mProgress.setMessage("Upload is " + progress + "% done");

                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload is paused");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUrl = taskSnapshot.getMetadata().getDownloadUrl();

                    //mDatabaseChatroom.child("test").child("audio_url").setValue(downloadUrl);

                    Toast.makeText(ChatroomActivity.this, (CharSequence) downloadUrl, Toast.LENGTH_LONG).show();
                    mProgress.dismiss();

                }
            });


        }
    }

}
