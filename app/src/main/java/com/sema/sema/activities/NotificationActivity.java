package com.sema.sema.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sema.sema.R;

public class NotificationActivity extends AppCompatActivity {

    private CheckBox mIncomingMessage, mDefaultRingtone, mVibrate, mLight, mHashTone;
    private DatabaseReference mDatabaseIncomingNotification, mDatabaseRingtone, mDatabaseVibrate, mDatabaseLight;
    private FirebaseAuth mAuth;
    private boolean proccessNotification = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseIncomingNotification = FirebaseDatabase.getInstance().getReference().child("IncomingNotification");
        mDatabaseRingtone = FirebaseDatabase.getInstance().getReference().child("IncomingRingtone");
        mDatabaseVibrate = FirebaseDatabase.getInstance().getReference().child("IncomingVibrate");
        mDatabaseLight = FirebaseDatabase.getInstance().getReference().child("IncomingLight");
        mDatabaseIncomingNotification.keepSynced(true);
        mDatabaseRingtone.keepSynced(true);
        mDatabaseLight.keepSynced(true);
        mDatabaseVibrate.keepSynced(true);

        mAuth = FirebaseAuth.getInstance();


        addListenerOnChk();

        mLight = (CheckBox) findViewById(R.id.light);
        mLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                proccessNotification = true;
                mDatabaseLight.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (proccessNotification) {

                            if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                mDatabaseLight.child(mAuth.getCurrentUser().getUid()).removeValue();
                                mLight.setChecked(false);
                                mLight.toggle();

                                proccessNotification = false;

                            } else {

                                mDatabaseLight.child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                mLight.setChecked(true);
                                mLight.toggle();

                                proccessNotification = false;
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        mVibrate = (CheckBox) findViewById(R.id.vibrate);
        mVibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                proccessNotification = true;

                mDatabaseVibrate.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (proccessNotification) {
                            if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                mDatabaseVibrate.child(mAuth.getCurrentUser().getUid()).removeValue();
                                mVibrate.setChecked(false);
                                mVibrate.toggle();

                            } else {

                                mDatabaseVibrate.child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                mVibrate.setChecked(true);
                                mVibrate.toggle();

                                proccessNotification = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        mDefaultRingtone = (CheckBox) findViewById(R.id.notification_tone);
        mDefaultRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                proccessNotification = true;

                mDatabaseRingtone.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (proccessNotification) {
                            if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                                mDatabaseRingtone.child(mAuth.getCurrentUser().getUid()).removeValue();
                                mDefaultRingtone.setChecked(false);
                                mDefaultRingtone.toggle();

                                proccessNotification = false;

                            } else {

                                mDatabaseRingtone.child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                                mDefaultRingtone.setChecked(true);
                                mDefaultRingtone.toggle();

                                proccessNotification = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });


        mIncomingMessage = (CheckBox) findViewById(R.id.default_ringtone);
       mIncomingMessage.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               proccessNotification = true;

               mDatabaseIncomingNotification.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {

                       if (proccessNotification) {
                           if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                               mDatabaseIncomingNotification.child(mAuth.getCurrentUser().getUid()).removeValue();
                               mIncomingMessage.setChecked(false);

                               proccessNotification = false;

                           } else {

                               mDatabaseIncomingNotification.child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                               mIncomingMessage.setChecked(true);

                               proccessNotification = false;

                           }

                       }
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });

           }
       });

    }

    private void addListenerOnChk() {

        mDatabaseIncomingNotification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    mIncomingMessage.setChecked(true);
                } else {
                    mIncomingMessage.setChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRingtone.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    mDefaultRingtone.setChecked(true);
                } else {
                    mDefaultRingtone.setChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseVibrate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    mVibrate.setChecked(true);
                } else {
                    mVibrate.setChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mHashTone = (CheckBox) findViewById(R.id.hashtag_tone);
        mDatabaseIncomingNotification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                   // mHashTone.setChecked(true);
                } else {
                  //  mHashTone.setChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseLight.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    mLight.setChecked(true);
                } else {
                    mLight.setChecked(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
}
