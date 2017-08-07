package com.sema.sema.utilis;

/**
 * Created by Shephard on 7/1/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sema.sema.models.sema;

import java.text.DateFormat;
import java.util.Date;

public class ConnectivityReceiver extends BroadcastReceiver {

    private DatabaseReference mDatabaseUsersOnline;
    private FirebaseAuth mAuth;

    public static ConnectivityReceiverListener connectivityReceiverListener;

    public ConnectivityReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(isConnected);

            mDatabaseUsersOnline = FirebaseDatabase.getInstance().getReference().child("users_online");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).removeValue();
        } else {

            Date date = new Date();

            //final String stringDate = DateFormat.getTimeInstance().format(date);
           /* SimpleDateFormat stringDate;*/
            String stringDate = DateFormat.getTimeInstance().format(date);
           /* stringDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");*/

            DatabaseReference mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users");
            DatabaseReference mDatabaseUsersOnline = FirebaseDatabase.getInstance().getReference().child("users_online");
            mAuth = FirebaseAuth.getInstance();
           /* mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).setValue("isOnline");*/
            mDatabaseUser.child(mAuth.getCurrentUser().getUid()).child("last_seen").setValue(stringDate);
        }
    }

    public static boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) sema.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }


    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(boolean isConnected);
    }

}
