package com.sema.sema.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sema.sema.chatrooms.ChatroomActivity;
import com.sema.sema.models.People;
import com.sema.sema.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by John on 25-Apr-17.
 */
public class tab2friends extends Fragment {

    String myCurrentChats = null;
    private Button mStartBtn;
    private TextView mNoPostTxt;
    private ImageView mNoPostImg;
    SwipeRefreshLayout mSwipeRefreshLayout;
    private DatabaseReference mDatabaseUsers, mDatabaseBlockThisUser, mDatabaseUnread,  mDatabaseNotification;
    private DatabaseReference mDatabaseChatroom, mDatabaseChatroomsShot, mDatabaseLatestMessage;
    private FirebaseAuth mAuth;
    private RecyclerView mMembersList;
    private Query mQueryPostChats;
    private ProgressBar mProgressBar;
    private Boolean mProcessLike = false;
    private DatabaseReference mDatabaseLike;
    private FloatingActionButton fabHash, fabPerson;
    private Query mQueryUnread;

    private ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab2friends, container, false);


        mStartBtn = (Button) v.findViewById(R.id.startChat);
        mViewPager = (ViewPager) v.findViewById(R.id.container);


        mDatabaseBlockThisUser = FirebaseDatabase.getInstance().getReference().child("BlockThisUser");
        mDatabaseLatestMessage = FirebaseDatabase.getInstance().getReference().child("latest_messages");
        mDatabaseNotification = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mDatabaseUnread = FirebaseDatabase.getInstance().getReference().child("Unread");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mNoPostImg = (ImageView) v.findViewById(R.id.noPostChat);
        mNoPostTxt = (TextView) v.findViewById(R.id.noPostTxt);
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mQueryPostChats = mDatabaseLatestMessage.orderByChild("sender_uid").equalTo(mAuth.getCurrentUser().getUid());
        mMembersList = (RecyclerView) v.findViewById(R.id.Members_list);
        mMembersList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMembersList.setHasFixedSize(true);
        mDatabaseChatroom.keepSynced(true);
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mDatabaseUnread.keepSynced(true);
        mDatabaseNotification.keepSynced(true);
        mQueryPostChats.keepSynced(true);
        mDatabaseLatestMessage.keepSynced(true);

        mDatabaseLatestMessage.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null){

                    mNoPostImg.setVisibility(View.VISIBLE);
                    mNoPostTxt.setVisibility(View.VISIBLE);
                } else {
                    mNoPostImg.setVisibility(View.GONE);
                    mNoPostTxt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseChatroomsShot = FirebaseDatabase.getInstance().getReference().child("Chatrooms").child(mAuth.getCurrentUser().getUid());

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<People, LetterViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<People, LetterViewHolder>(

                People.class,
                R.layout.member_item,
                LetterViewHolder.class,
                mDatabaseLatestMessage.child(mAuth.getCurrentUser().getUid())


        ) {
            @Override
            protected void populateViewHolder(final LetterViewHolder viewHolder, final People model, int position) {

                final String post_key = getRef(position).getKey();
                final String PostKey = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setDate(model.getDate());
                viewHolder.setMessage(model.getMessage());
                viewHolder.setImage(getContext(), model.getImage());
                //viewHolder.setLikeBtn(post_key);


                final Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Aller_Rg.ttf");
                viewHolder.txname.setTypeface(custom_font);
                viewHolder.txmessage.setTypeface(custom_font);
                viewHolder.txdate.setTypeface(custom_font);

                mDatabaseUsers.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("isOnline")){
                            Boolean userOnline = (Boolean) dataSnapshot.child("isOnline").getValue();
                            viewHolder.setUserOnline(userOnline);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //IF USER HAS NO UNREAD MESSAGE, MAKE COUNTER GONE
                mDatabaseUnread.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {
                            viewHolder.counterTxt.setVisibility(View.VISIBLE);
                        } else {
                            viewHolder.counterTxt.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                //counting number of unread messages
                mDatabaseUnread.child(post_key).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        viewHolder.mUnreadTxt.setText(dataSnapshot.getChildrenCount() + "");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mDatabaseChatroom.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final String PostKey = (String) dataSnapshot.child("uid").getValue();
                        final String group = (String) dataSnapshot.child("this_is_a_group").getValue();

                        // open chatroom activity
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                    mDatabaseUnread.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                    mDatabaseNotification.child(mAuth.getCurrentUser().getUid()).removeValue();
                                    Intent cardonClick2 = new Intent(getActivity(), ChatroomActivity.class);
                                    cardonClick2.putExtra("heartraise_id", post_key );
                                    startActivity(cardonClick2);

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                // ON OPEN MESSAGE, CLEAR ALL UNREAD MESSAGE
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mDatabaseUnread.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                        mDatabaseNotification.child(mAuth.getCurrentUser().getUid()).removeValue();
                    }
                });

                mDatabaseUnread.child(post_key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String PostKey = (String) dataSnapshot.child("uid").getValue();

                        // open chatroom activity
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                mDatabaseUnread.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                mDatabaseNotification.child(mAuth.getCurrentUser().getUid()).removeValue();
                                Intent cardonClick = new Intent(getActivity(), ChatroomActivity.class);
                                cardonClick.putExtra("heartraise_id", post_key );
                                startActivity(cardonClick);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.mPostImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;

                        mDatabaseLike.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if(mProcessLike) {

                                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        viewHolder.mLikeBtn.setVisibility(View.GONE);
                                        mProcessLike = false;
                                    }else {

                                        mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                        viewHolder.mLikeBtn.setVisibility(View.VISIBLE);
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



                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {


                        final Context context = getActivity();

                        // custom dialog
                        final Dialog dialog = new Dialog(context);
                        dialog.setContentView(R.layout.popup_dialog_new);
                        dialog.setTitle("Chat options");

                        // set the custom dialog components - text, image and button
                        // send current user uid to block user child
                        LinearLayout blockLiny = (LinearLayout) dialog.findViewById(R.id.blockLiny);
                        blockLiny.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                // push uid to block user child in database
                                final DatabaseReference newPost = mDatabaseBlockThisUser;
                                newPost.child(mAuth.getCurrentUser().getUid()).child(post_key).setValue("Block");
                                AlertDialog diaBox = AskOption2();
                                diaBox.show();
                                dialog.dismiss();
                            }
                        });

                        LinearLayout deleteLiny = (LinearLayout) dialog.findViewById(R.id.deleteLiny);
                        deleteLiny.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                AlertDialog diaBox = AskOption();
                                diaBox.show();
                                dialog.dismiss();
                            }
                        });


                        // if button is clicked, close the custom dialog

                        dialog.show();
                        return false;
                    }

                    private AlertDialog AskOption() {

                        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                                //set message, title, and icon
                                .setTitle("Delete Alert!")
                                .setMessage("Are you sure you want to remove this chat!")

                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //your deleting code

                                        mDatabaseChatroom.child(post_key).removeValue();
                                        dialog.dismiss();
                                    }

                                })



                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        dialog.dismiss();

                                    }
                                })
                                .create();
                        return myQuittingDialogBox;

                    }

                });

            }

            private AlertDialog AskOption2() {

                AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                        //set message, title, and icon
                        .setTitle("Block Alert!")
                        .setMessage("You will no longer recieve messages from this user?")

                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                dialog.dismiss();
                            }

                        })

                        .create();
                return myQuittingDialogBox;


            }



        };

        mMembersList.setAdapter(firebaseRecyclerAdapter);
        checkUserExists();

    }

    private void checkUserExists() {

        mProgressBar.setVisibility(View.VISIBLE);
        final String user_id = mAuth.getCurrentUser().getUid();

        mDatabaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                if (!dataSnapshot.hasChild(user_id)) {

                    mProgressBar.setVisibility(View.GONE);

                }else {

                    mProgressBar.setVisibility(View.GONE);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                mProgressBar.setVisibility(View.GONE);
            }
        });
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



    public static class LetterViewHolder extends RecyclerView.ViewHolder {

        View mView;

        CircleImageView mPostImg;
        ImageView mLikeBtn, groupIcon;
        TextView mUnreadTxt,  txname, txdate, txmessage;
        DatabaseReference mDatabaseLike;
        RelativeLayout counterTxt;
        FirebaseAuth mAuth;
        ProgressBar mProgressBar;

        public LetterViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
            txname = (TextView) mView.findViewById(R.id.post_name);
            txdate = (TextView) mView.findViewById(R.id.post_date);
            txmessage = (TextView) mView.findViewById(R.id.post_message);
            mPostImg = (CircleImageView) mView.findViewById(R.id.post_image);
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mUnreadTxt = (TextView) mView.findViewById(R.id.unreadCounter);
            counterTxt = (RelativeLayout) mView.findViewById(R.id.counter);
           // groupIcon = (ImageView) mView.findViewById(R.id.group_icon);
            mDatabaseLike.keepSynced(true);
            Query mQueryPostChats;

        }



        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setDate(String date) {

            TextView post_date = (TextView) mView.findViewById(R.id.post_date);
            post_date.setText(date);
        }

        public void setMessage(String message) {

            TextView post_message = (TextView) mView.findViewById(R.id.post_message);
            post_message.setText(message);
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

        public void setUserOnline(Boolean online_status) {

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.online_status_icon);
            if (online_status == true) {

                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.GONE);
            }


        }

    }

}