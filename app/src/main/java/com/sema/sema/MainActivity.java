package com.sema.sema;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mDatabaseUsers, mDatabaseHashtag, mDatabaseNotification, mDatabaseLastSeen;
    private static double longitude;
    private static double latitude;
    private FirebaseAuth auth;
    private FloatingActionButton fabHash, fabPerson;
    private FirebaseAuth mAuth;
    private TextView mLatitude;
    Context mContext;
    private Menu menu;
    private ViewPager mViewPager;

    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext=this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");


        // SETTING UP FONTS
        final Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/Aller_Rg.ttf");

        fabHash = (FloatingActionButton) findViewById(R.id.fabChat);
        fabHash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Context context = MainActivity.this;

                // custom dialog
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.hashtag_dialog);
                dialog.setTitle("Create hashtag");
                dialog.show();


                final EditText hashInput = (EditText) dialog.findViewById(R.id.hashtagInput);
                hashInput.setTypeface(custom_font);
                Button cancel = (Button) dialog.findViewById(R.id.cancel);
                cancel.setTypeface(custom_font);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                Button create = (Button) dialog.findViewById(R.id.create);
                create.setTypeface(custom_font);
                create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        startPosting();
                        dialog.dismiss();
                    }

                    private void startPosting() {

                        Date date = new Date();
                        final String stringDate = DateFormat.getDateTimeInstance().format(date);
                        final String stringDate2 = DateFormat.getDateInstance().format(date);

                        final String hashTag = hashInput.getText().toString().trim();
                        if (TextUtils.isEmpty(hashTag)) {


                        } else {

                            final DatabaseReference newPost = mDatabaseHashtag.push();

                            // adding my user uid to hashtag chatroom
                            final DatabaseReference newPost2 = mDatabaseHashtag.child("hashtag_chatroom").child(auth.getCurrentUser().getUid());

                            mDatabaseUsers.child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    // getting user uid
                                    newPost.child("hashtag").setValue(hashTag);
                                    newPost.child("uid").setValue(dataSnapshot.child("uid").getValue());
                                    newPost.child("name").setValue(dataSnapshot.child("name").getValue());
                                    newPost.child("image").setValue(dataSnapshot.child("image").getValue());
                                    newPost.child("date").setValue(stringDate2);
                                    newPost.child(auth.getCurrentUser().getUid()).setValue(auth.getCurrentUser().getUid());

                                    //newPost2.child(auth.getCurrentUser().getUid()).child("uid").setValue(dataSnapshot.getValue());


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                    }
                });



            }
        });


       // Toast.makeText(mContext, String.valueOf(latitude + longitude),Toast.LENGTH_LONG).show();


        fabPerson = (FloatingActionButton) findViewById(R.id.fabPerson);
        fabPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cardonClick = new Intent(MainActivity.this, AddFriendsActivity.class);
                startActivity(cardonClick);
            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mDatabaseNotification = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mDatabaseLastSeen = FirebaseDatabase.getInstance().getReference().child("Last_Seen");
        mDatabaseHashtag = FirebaseDatabase.getInstance().getReference().child("Hashtag");
        mDatabaseNotification.keepSynced(true);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        // create class object
        gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        if(gps.canGetLocation()){
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

            mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("latitude").setValue(latitude);
            mDatabaseUsers.child(auth.getCurrentUser().getUid()).child("location").child("longitude").setValue(longitude);
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }




        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                switch (position) {
                    case 0:
                        fabHash.show();
                        fabPerson.hide();
                        break;
                    case 1:
                        fabPerson.show();
                        fabHash.hide();
                        break;

                    default:
                        fabHash.hide();
                        fabPerson.hide();
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        checkForNotifications();
        addToLastSeen();

    }





    private void addToLastSeen() {

        Date date = new Date();
        final String stringDate = DateFormat.getDateTimeInstance().format(date);

        mDatabaseLastSeen.child(auth.getCurrentUser().getUid()).child("last_seen").setValue(stringDate);
    }

    private void checkForNotifications() {

        mDatabaseNotification.child(auth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String name = (String) dataSnapshot.child("name").getValue();
                    String image = (String) dataSnapshot.child("image").getValue();
                    String date = (String) dataSnapshot.child("date").getValue();
                    String sender_uid = (String) dataSnapshot.child("uid").getValue();
                    String message = (String) dataSnapshot.child("message").getValue();

                    // send notification to reciever


                    Context context = getApplicationContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent pIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
                    Notification noty = new Notification.Builder(MainActivity.this)
                            .setContentTitle("Sema")
                            .setTicker("You have a new message")
                            .setContentText(name + ": " + message)
                            .setSmallIcon(R.drawable.ic_sema_notification)
                            .setContentIntent(pIntent).getNotification();

                    noty.flags = Notification.FLAG_AUTO_CANCEL;
                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    r.play();
                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    nm.notify(0, noty);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent cardonClick = new Intent(MainActivity.this, SelectInterestActivity.class);
            startActivity(cardonClick);
            return true;
        }  else if (id == R.id.action_add_friends){

            Intent cardonClick = new Intent(MainActivity.this, AddFriendsActivity.class);
            startActivity(cardonClick);
        } else if (id == R.id.action_add_group){

            Intent cardonClick = new Intent(MainActivity.this, DiscoverHashtagActivity.class);
            startActivity(cardonClick);
        } else if (id == R.id.action_trending){

            Intent cardonClick = new Intent(MainActivity.this, TrendsActivity.class);
            startActivity(cardonClick);
        }


        return super.onOptionsItemSelected(item);
    }

       /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            //returning the current tabs
            switch (position) {
                case 0:
                    tab1hashtag tab1 = new tab1hashtag();
                    return tab1;
                case 1:
                    tab2friends tab2 = new tab2friends();
                    return tab2;
                case 2:
                    tab3profile tab3 = new tab3profile();
                    return tab3;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "HASHTAG";
                case 1:
                    return "CHATS";
                case 2:
                    return "PROFILE";
            }
            return null;
        }


    }



}
