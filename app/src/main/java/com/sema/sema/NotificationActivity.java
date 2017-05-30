package com.sema.sema;

import android.content.SharedPreferences;
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

public class NotificationActivity extends AppCompatActivity {

    private CheckBox mIncomingMessage, mDefaultRingtone, mVibrate, mLight;
    private DatabaseReference mDatabaseIncomingNotification;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabaseIncomingNotification = FirebaseDatabase.getInstance().getReference().child("IncomingNotification");
        mAuth = FirebaseAuth.getInstance();

        mDatabaseIncomingNotification.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    SharedPreferences.Editor editor = getSharedPreferences("com.sema.sema", MODE_PRIVATE).edit();
                    editor.putBoolean("Service on", true);
                    editor.commit();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addListenerOnChk();

        mIncomingMessage = (CheckBox) findViewById(R.id.default_ringtone);
       mIncomingMessage.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               mDatabaseIncomingNotification.addValueEventListener(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {

                       if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                           mDatabaseIncomingNotification.child(mAuth.getCurrentUser().getUid()).removeValue();
                           mIncomingMessage.setChecked(false);
                           mIncomingMessage.toggle();

                       } else {

                           mDatabaseIncomingNotification.child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getUid());
                           mIncomingMessage.setChecked(true);
                           mIncomingMessage.toggle();

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

                if (dataSnapshot.hasChild(mAuth.getCurrentUser().getUid())) {

                    mIncomingMessage.setChecked(true);
                } else {
                    mIncomingMessage.setChecked(false);
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
