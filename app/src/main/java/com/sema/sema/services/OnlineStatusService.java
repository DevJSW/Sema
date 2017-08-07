package com.sema.sema.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;

/**
 * Created by Shephard on 7/5/2017.
 */

public class OnlineStatusService extends Service {

    private DatabaseReference currentUserRef;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
        {

            DatabaseReference onlinePresenceReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference onlineRef = onlinePresenceReference.child(".info/connected");
            currentUserRef = onlinePresenceReference.child("/users_online/" + FirebaseAuth.getInstance().getCurrentUser().getUid());

            onlineRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot)
                {
                    Log.d(TAG, "onDataChange:" + dataSnapshot);
                    if (dataSnapshot.getValue(Boolean.class))
                    {
                        currentUserRef.onDisconnect().setValue(System.currentTimeMillis());
                        currentUserRef.setValue(true);
                    }
                }


                @Override
                public void onCancelled(final DatabaseError databaseError)
                {
                    Log.d(TAG, "onCancelled:" + databaseError);
                }
            });
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
