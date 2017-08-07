package com.sema.sema.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.sema.sema.chatrooms.ChatroomActivity;
import com.sema.sema.chatrooms.HashChatroomActivity;
import com.sema.sema.models.People;
import com.sema.sema.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab3explore extends Fragment {

    private FirebaseAuth mAuth;
    private RecyclerView mMembersList, mHashList;
    private LinearLayoutManager topHashtagLayoutManager, memberLayoutManager;
    private Query mQueryPostChats, mQueryTrends;
    private DatabaseReference mDatabaseUsers, mDatabaseUnread, mDatabaseNotification, mDatabaseViews;
    private DatabaseReference mDatabaseChatroom, mDatabaseHashtag, mDatabase,  mDatabaseJoinedHashtag;
    private ProgressBar mProgressBar;

    private SliderLayout mDemoSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tab3explore, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseNotification = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mDatabaseJoinedHashtag = FirebaseDatabase.getInstance().getReference().child("joined_hashtags").child(mAuth.getCurrentUser().getUid());
        mDatabaseViews = FirebaseDatabase.getInstance().getReference().child("hash_views");
        mDatabaseHashtag = FirebaseDatabase.getInstance().getReference().child("all_hashtags");
        mDatabaseUnread = FirebaseDatabase.getInstance().getReference().child("Unread");
        mDatabaseChatroom = FirebaseDatabase.getInstance().getReference().child("Chatrooms");
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mQueryPostChats = mDatabaseHashtag.orderByChild(mAuth.getCurrentUser().getUid()).equalTo(mAuth.getCurrentUser().getUid());
        mQueryTrends = mDatabaseHashtag.orderByChild("trends").limitToLast(10);

        mHashList = (RecyclerView) v.findViewById(R.id.hash_list);
        topHashtagLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mHashList.setLayoutManager(topHashtagLayoutManager);

        mMembersList = (RecyclerView) v.findViewById(R.id.Members_list);
        memberLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
       /* mMembersList.setLayoutManager(new GridLayoutManager(getActivity(), 3));*/
        mMembersList.setLayoutManager(memberLayoutManager);

        mDemoSlider = (SliderLayout) v.findViewById(R.id.slider);
        setUpDemoSlider();

        return v;
    }

    private void setUpDemoSlider() {
        HashMap<String,Integer> file_maps = new HashMap<String, Integer>();
        file_maps.put("If you do,build a great experience", R.drawable.slider1);
        file_maps.put("Learn from your competitor,never copy.", R.drawable.slider2);
        file_maps.put("Create content that teaches", R.drawable.slider3);
        file_maps.put("Nunua,commerce leaders", R.drawable.slider4);


        for(String name : file_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(getActivity());
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(file_maps.get(name))
                    .setScaleType(BaseSliderView.ScaleType.Fit);


            textSliderView.bundle(new Bundle());
            // textSliderView.getBundle().putString("extra",name);

            mDemoSlider.addSlider(textSliderView);
        }
        mDemoSlider.setPresetTransformer(SliderLayout.Transformer.Accordion);
        mDemoSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mDemoSlider.setCustomAnimation(new DescriptionAnimation());
        mDemoSlider.setDuration(4000);
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<People, tab3explore.topHashViewHolder> firebaseRecyclerAdapter2 = new  FirebaseRecyclerAdapter<People, tab3explore.topHashViewHolder>(

                People.class,
                R.layout.hashtags_grid,
                tab3explore.topHashViewHolder.class,
                mQueryTrends


        ) {
            @Override
            protected void populateViewHolder(final tab3explore.topHashViewHolder viewHolder, final People model, int position) {

                final String post_key = getRef(position).getKey();
                final String PostKey = getRef(position).getKey();

                viewHolder.setHashtag(model.getHashtag());
                viewHolder.setImage(getContext(), model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDatabaseViews.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                        mDatabaseUnread.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                        mDatabaseNotification.child(mAuth.getCurrentUser().getUid()).removeValue();

                        // open hashtag chatroom
                        Intent cardonClick = new Intent(getActivity(), HashChatroomActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key);
                        startActivity(cardonClick);
                    }
                });

            }



        };

       /* topHashtagLayoutManager.setReverseLayout(true);*/
        mHashList.setAdapter(firebaseRecyclerAdapter2);

        FirebaseRecyclerAdapter<People, tab3explore.LocalsViewHolder> firebaseRecyclerAdapter = new  FirebaseRecyclerAdapter<People, tab3explore.LocalsViewHolder>(

                People.class,
                R.layout.locals_item,
                tab3explore.LocalsViewHolder.class,
                mDatabaseUsers


        ) {
            @Override
            protected void populateViewHolder(final tab3explore.LocalsViewHolder viewHolder, final People model, int position) {

                final String post_key = getRef(position).getKey();
                final String PostKey = getRef(position).getKey();

                viewHolder.setName(model.getName());
                viewHolder.setAddress(model.getAddress());
                viewHolder.setImage(getContext(), model.getImage());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // open chatroom
                        Intent cardonClick = new Intent(getActivity(), ChatroomActivity.class);
                        cardonClick.putExtra("heartraise_id", post_key);
                        startActivity(cardonClick);
                    }
                });

            }


        };

       /* topHashtagLayoutManager.setReverseLayout(true);*/
        mMembersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class topHashViewHolder extends RecyclerView.ViewHolder {

        View mView;

        CircleImageView mPostImg;
        ImageView mLikeBtn, groupIcon;
        TextView mUnreadTxt, txname, txhash, txdate, txmessage;
        DatabaseReference mDatabaseLike;
        RelativeLayout counterTxt;
        FirebaseAuth mAuth;
        ProgressBar mProgressBar;

        public topHashViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);
            txname = (TextView) mView.findViewById(R.id.post_name);
            txdate = (TextView) mView.findViewById(R.id.post_date);
            txhash = (TextView) mView.findViewById(R.id.post_hashtag);
            txmessage = (TextView) mView.findViewById(R.id.post_message);
            mPostImg = (CircleImageView) mView.findViewById(R.id.post_image);
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
            mUnreadTxt = (TextView) mView.findViewById(R.id.unreadCounter);
            mAuth = FirebaseAuth.getInstance();
            counterTxt = (RelativeLayout) mView.findViewById(R.id.counter);
            mLikeBtn = (ImageView) mView.findViewById(R.id.favourite);
            mDatabaseLike.keepSynced(true);
            Query mQueryPostChats;

        }


        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setAddress(String address) {

            TextView post_address = (TextView) mView.findViewById(R.id.post_address);
            post_address.setText(address);

        }

        public void setHashtag(String hashtag) {

            TextView post_hashtag = (TextView) mView.findViewById(R.id.post_hashtag);
            post_hashtag.setText(hashtag);
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

    public static class LocalsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        Button mChatBtn;

        ProgressBar mProgressBar;

        public LocalsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            //  mChatBtn = (Button) mView.findViewById(R.id.chatBtn);
            mProgressBar = (ProgressBar) mView.findViewById(R.id.progressBar);

        }

        public void setName(String name) {

            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(name);
        }

        public void setAddress(String address) {

            TextView post_address = (TextView) mView.findViewById(R.id.post_address);
            post_address.setText(address);

        }

        public void setImage(final Context ctx, final String image) {

            final ImageView civ = (ImageView) mView.findViewById(R.id.post_image);

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
