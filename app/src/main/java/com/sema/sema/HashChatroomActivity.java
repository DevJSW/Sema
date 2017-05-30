package com.sema.sema;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;

public class HashChatroomActivity extends AppCompatActivity {

    private static final String TAG = HashChatroomActivity.class.getSimpleName();
    private String mPostKey = null;
    private TextView mNoPostTxt;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressDialog mProgress;
    private RecyclerView mCommentList;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseComment, mDatabaseChatroom, mDatabaseUnread, mDatabaseHashtag, mDatabaseLastSeen, mDatabaseViews;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseUser2;
    private DatabaseReference mDatabasePostChats;
    private Query mQueryPostChats;
    private Query mQueryInAscending;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private ImageView mSendBtn;
    private EditText mCommentField;
    private Uri mImageUri = null;
    private static int GALLERY_REQUEST = 1;
    private Boolean mProcessStopChat = false;
    private Boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Menu menu;
    Context context = this;

    private LinearLayoutManager mLayoutManager;
    EmojiconEditText emojiconEditText;
    EmojiconTextView textView;
    ImageView emojiImageView;
    EmojIconActions emojIcon;
    View rootView;
    private Query mQueryChats;


    /** Called when the activity is first created. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hash_chatroom);
        Toolbar my_toolbar = (Toolbar) findViewById(R.id.mCustomToolbarHash);
        //keep layout on top of keyboard

        // Font path
        final Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        setSupportActionBar(my_toolbar);
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


        mAuth = FirebaseAuth.getInstance();
        mPostKey = getIntent().getExtras().getString("heartraise_id");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");

        mDatabasePostChats = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mDatabaseHashtag = FirebaseDatabase.getInstance().getReference().child("Hashtag");
        mDatabaseUnread = FirebaseDatabase.getInstance().getReference().child("Unread");
        mDatabaseViews = FirebaseDatabase.getInstance().getReference().child("hash_views");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mQueryPostChats = mDatabasePostChats.orderByChild("post_key").equalTo(mPostKey);
        mQueryChats = mDatabasePostChats.orderByChild("uid").equalTo(mAuth.getCurrentUser().getUid());

        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mDatabaseUser2 = FirebaseDatabase.getInstance().getReference().child("Users");
        mCommentList = (RecyclerView) findViewById(R.id.comment_list);
        mCommentList.setHasFixedSize(true);
        // clear unread messages
        mDatabaseUnread.child(mPostKey).child(mAuth.getCurrentUser().getUid()).removeValue();

        mCommentList.setLayoutManager(new LinearLayoutManager(this));
        mDatabaseComment = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseComment.keepSynced(true);
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mDatabaseChatroom.keepSynced(true);
        mDatabaseHashtag.keepSynced(true);
        mDatabaseUser.keepSynced(true);
        mDatabaseUnread.keepSynced(true);
        mDatabaseLike.keepSynced(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Hashtag").child(mPostKey).child("Chats");
        mQueryInAscending = mDatabase.orderByChild("date").startAt(-1 * new Date().getTime());
        mSendBtn = (ImageView) findViewById(R.id.sendBtn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });

        // toolbar back button
        ImageView toolbar_back = (ImageView) findViewById(R.id.toolbar_back);
        toolbar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashChatroomActivity.this.finish();
            }
        });

        mDatabaseHashtag.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String userimg = (String) dataSnapshot.child("image").getValue();
                final String username = (String) dataSnapshot.child("name").getValue();
                final String hashtag = (String) dataSnapshot.child("hashtag").getValue();
                final CircleImageView civ = (CircleImageView) findViewById(R.id.post_image);
                final TextView name = (TextView) findViewById(R.id.post_name);

                // load image on toolbar
                CircleImageView userImgToolbar = (CircleImageView) findViewById(R.id.toolbarImg);
                Picasso.with(HashChatroomActivity.this).load(userimg).into(userImgToolbar);

                // set username on toolbar
                TextView toolbar_username = (TextView) findViewById(R.id.toolbar_username);
                toolbar_username.setText(username);
                toolbar_username.setTypeface(custom_font);

                TextView toolbar_hashtag = (TextView) findViewById(R.id.toolbar_hashtag);
                toolbar_hashtag.setText(hashtag);
                toolbar_hashtag.setTypeface(custom_font);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of comments in a hashtag
        mDatabaseHashtag.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView chat_counter = (TextView) findViewById(R.id.chat_counter);
                chat_counter.setText(dataSnapshot.getChildrenCount() + "");
                chat_counter.setTypeface(custom_font);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of views in a hashtag
        mDatabaseViews.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView views_counter = (TextView) findViewById(R.id.view_counter);
                views_counter.setText(dataSnapshot.getChildrenCount() + "");
                views_counter.setTypeface(custom_font);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // count number of views in a hashtag
        mDatabaseLike.child(mPostKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView like_counter = (TextView) findViewById(R.id.star_counter);
                like_counter.setText(dataSnapshot.getChildrenCount() + "");

                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    ImageView star_icon = (ImageView) findViewById(R.id.star_icon);
                    star_icon.setImageResource(R.drawable.ic_star_white);
                } else {

                    ImageView star_icon = (ImageView) findViewById(R.id.star_icon);
                    star_icon.setImageResource(R.drawable.ic_favourite_);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseComment.keepSynced(true);
        addToLastSeen();

    }

    private void addToLastSeen() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        mDatabaseLastSeen.child(mAuth.getCurrentUser().getUid()).child("last_seen").setValue(stringDate);
    }


    private void startPosting() {
        // mProgress.setMessage("Posting...");

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        final String message_val = emojiconEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(message_val)) {

            //mProgress.show();
            //pushing chats into chat's tab

            //sender chat screen
            final DatabaseReference newPost = mDatabaseHashtag.child(mPostKey).child("Chats").push();
            final DatabaseReference newPost2 = mDatabaseHashtag.child(mPostKey);


            //post message to unread child
            final DatabaseReference newPost2Unread = mDatabaseUnread.child(mAuth.getCurrentUser().getUid()).child(mPostKey).push();

            // post last active date to user data
            final DatabaseReference newPost4 = mDatabaseUser;



            mDatabaseUser2.child(mPostKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                    final String userimg = (String) dataSnapshot.child("image").getValue();
                    final String username = (String) dataSnapshot.child("name").getValue();


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


                                // unread
                                newPost2Unread.child("message").setValue(message_val);

                                // reciever chat
                                newPost.child("message").setValue(message_val);
                                newPost.child("uid").setValue(mCurrentUser.getUid());
                                newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                                newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                                newPost.child("sender_uid").setValue(mCurrentUser.getUid());
                                newPost.child("date").setValue(stringDate);
                                newPost.child("post_key").setValue(mPostKey);
                                newPost.child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());

                                //update messege showing on tab1 chats activity
                                newPost2.child("message").setValue(message_val);


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

        final FirebaseRecyclerAdapter<Chat, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chat, CommentViewHolder>(

                Chat.class,
                R.layout.hash_row,
                CommentViewHolder.class,
                mDatabase


        ) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Chat model, int position) {

                final String post_key = getRef(position).getKey();

                viewHolder.setMessage(model.getMessage());
                viewHolder.setDate(model.getDate());
                viewHolder.setName(model.getName());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setLikeBtn(post_key);

                // SETTING UP FONTS
                Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");
                viewHolder.tx.setTypeface(custom_font);
                viewHolder.txdate.setTypeface(custom_font);
                viewHolder.txname.setTypeface(custom_font);
                viewHolder.txcaption.setTypeface(custom_font);

                mDatabaseLike.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                viewHolder.ReyLikeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(mProcessLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {


                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();

                                        mDatabaseLike.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        mProcessLike = false;

                                    }else {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());

                                        mDatabaseLike.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                viewHolder.mLikeCount.setText(dataSnapshot.getChildrenCount() + "");
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        mProcessLike = false;

                                    }

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                    }
                });


                //DELETE UNREAD MESSAGES WHILE SCROLLING
                mCommentList.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                            // Do something
                            mDatabaseUnread.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                        } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            // Do something
                            mDatabaseUnread.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                        } else {
                            // Do something
                        }
                    }
                });

                mDatabaseComment.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String group_uid = (String) dataSnapshot.child("this_is_a_group").getValue();

                        mDatabaseHashtag.child(mPostKey).child("Chats").child(post_key).addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final String post_photo = (String) dataSnapshot.child("photo").getValue();
                                final String chat_icon = (String) dataSnapshot.child("change_chat_icon").getValue();

                                if (post_photo != null) {

                                    viewHolder.setPhoto(getApplicationContext(), model.getPhoto());
                                    viewHolder.mCardPhoto.setVisibility(View.VISIBLE);
                                    viewHolder.min_lay.setVisibility(View.GONE);


                                    // if card has my uid, then change chat balloon shape
                                } else {

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


                mDatabaseComment.child(mCurrentUser.getUid()).child(mPostKey).child(post_key).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String post_photo = (String) dataSnapshot.child("photo").getValue();
                        final String chat_icon = (String) dataSnapshot.child("change_chat_icon").getValue();

                        if (post_photo != null) {

                            viewHolder.setPhoto(getApplicationContext(), model.getPhoto());
                            viewHolder.mCardPhoto.setVisibility(View.VISIBLE);

                            // if card has my uid, then change chat balloon shape
                        } else {

                            viewHolder.mCardPhoto.setVisibility(View.GONE);
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mDatabaseUser.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        final String current_user_uid = (String) snapshot.child("uid").getValue();

                        mDatabaseComment.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot snapshot) {

                                final String reciever_uid = (String) snapshot.child("uid").getValue();


                                if (snapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                    //viewHolder.rely.setVisibility(View.VISIBLE);
                                    //viewHolder.liny.setVisibility(View.GONE);

                                } else {

                                    // viewHolder.rely.setVisibility(View.GONE);
                                    // viewHolder.liny.setVisibility(View.VISIBLE);
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

                viewHolder.mCardPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //  Intent cardonClick = new Intent(Chatroom2Activity.this, OpenPhotoActivity.class);
                        // cardonClick.putExtra("heartraise_id", post_key );
                        // startActivity(cardonClick);

                    }
                });


            }
        };
        final LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mCommentList.setAdapter(firebaseRecyclerAdapter);
        // mLinearLayoutManager.setReverseLayout(false);
        //mLinearLayoutManager.setStackFromEnd(true);

        mLayoutManager = new LinearLayoutManager(HashChatroomActivity.this);
        // mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(true);

        // Now set the layout manager and the adapter to the RecyclerView
        mCommentList.setLayoutManager(mLayoutManager);

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

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


        final long delay = 1000; // 1 seconds after user stops typing
        final long[] last_text_edit = {0};
        final Handler handler = new Handler();

        final Runnable input_finish_checker = new Runnable() {
            public void run() {
                if (System.currentTimeMillis() > (last_text_edit[0] + delay - 500)) {
                    // ............
                    // ............
                    TextView typing = (TextView) findViewById(R.id.typing_watcher);
                    TextView toolbar_last_seen = (TextView) findViewById(R.id.toolbar_last_seen_date);
                    typing.setVisibility(View.VISIBLE);
                    toolbar_last_seen.setVisibility(View.GONE);
                }
            }
        };

        mDatabaseChatroom.child(mPostKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

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
                                                            } else {

                                                                TextView typing = (TextView) findViewById(R.id.typing_watcher);
                                                                TextView toolbar_last_seen = (TextView) findViewById(R.id.toolbar_last_seen_date);
                                                                typing.setVisibility(View.GONE);
                                                                toolbar_last_seen.setVisibility(View.VISIBLE);

                                                            }
                                                        }
                                                    }

                    );

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // if recyclerview is at the bottom, clear any unread messages
        final boolean[] loading = {true};
        final int[] pastVisiblesItems = new int[1];
        final int[] visibleItemCount = new int[1];
        final int[] totalItemCount = new int[1];

        mCommentList.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount[0] = mLayoutManager.getChildCount();
                    totalItemCount[0] = mLayoutManager.getItemCount();
                    pastVisiblesItems[0] = mLayoutManager.findFirstVisibleItemPosition();

                    if (loading[0])
                    {
                        if ( (visibleItemCount[0] + pastVisiblesItems[0]) >= totalItemCount[0])
                        {
                            loading[0] = false;
                            Log.v("...", "Last Item Wow !");
                            mDatabaseUnread.child(mPostKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                        }
                    }
                }
            }
        });

    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        View mView;

        DatabaseReference mDatabaseUnread;
        DatabaseReference mDatabaseLike;
        TextView  mLikeCount, tx, tx2, txname, txname2, txdate, txdate2, txcaption, txcaption2;
        FirebaseAuth mAuth;
        ImageView mImage, mCardPhoto2, mImage2;
        RelativeLayout rely;
        LinearLayout liny,  min_lay,  mCardPhoto;
        ProgressBar mProgressBar;
        RelativeLayout ReyLikeBtn;

        public CommentViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            //font

            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            tx = (TextView) mView.findViewById(R.id.post_message);
            txname = (TextView) mView.findViewById(R.id.post_name);
            txdate = (TextView) mView.findViewById(R.id.post_date);
            txcaption = (TextView) mView.findViewById(R.id.captionInput);
            min_lay = (LinearLayout) mView.findViewById(R.id.main_lay);
            mDatabaseLike.keepSynced(true);
            mAuth = FirebaseAuth.getInstance();
            mLikeCount = (TextView) mView.findViewById(R.id.likeCount);
            mCardPhoto = (LinearLayout) mView.findViewById(R.id.chat_image);
            mImage = (ImageView) mView.findViewById(R.id.post_image);
            //  groupIcon = (ImageView) mView.findViewById(R.id.group_icon);
            ReyLikeBtn = (RelativeLayout) mView.findViewById(R.id.counter);
            liny = (LinearLayout) mView.findViewById(R.id.liny);
            rely = (RelativeLayout) mView.findViewById(R.id.rely);
            //mSingleTick = (ImageView)mView.findViewById(R.id.singleTick);
            // mDoubleTick = (ImageView)mView.findViewById(R.id.doubleTick);
            mDatabaseUnread = FirebaseDatabase.getInstance().getReference().child("Unread");


        }

        public void setLikeBtn(final String post_key) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                        ReyLikeBtn.setBackgroundResource(R.drawable.heart_red);


                    } else {

                        ReyLikeBtn.setBackgroundResource(R.drawable.heart_grey);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        public void setMessage(String message) {

            TextView post_message = (TextView) mView.findViewById(R.id.post_message);
            post_message.setText(message);

            TextView post_caption = (TextView) mView.findViewById(R.id.captionInput);
            post_caption.setText(message);

        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);


        }


        public void setDate(String date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(date);


        }

        public void setImage(final Context ctx, final String image) {
            final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);

            Picasso.with(ctx)
                    .load(image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(post_image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {


                            Picasso.with(ctx).load(image).into(post_image);
                        }
                    });

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
                if (id == R.id.action_settings) {

                    Intent cardonClick = new Intent(HashChatroomActivity.this, HashSendPhotoActivity.class);
                    cardonClick.putExtra("heartraise_id", mPostKey );
                    startActivity(cardonClick);
                }


                return super.onOptionsItemSelected(item);
        }
    }

}

