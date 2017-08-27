package com.sema.sema.models;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sema.sema.services.GPSTracker;
import com.sema.sema.services.OnlineStatusService;
import com.sema.sema.utilis.ConnectivityReceiver;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Shephard on 7/1/2017.
 */

public class sema extends Application {

    private static sema mInstance;

    public static final String LOG_TAG = "sema";

    public boolean wasInBackground = true;

    private DatabaseReference mDatabaseUsersOnline, mDatabaseUsers;
    private FirebaseAuth mAuth;

    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;  // Time allowed for transitions
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        //SET UP REALM
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().build();
        Realm.setDefaultConfiguration(config);

        mInstance = this;
        registerActivityLifecycleCallbacks(activityCallbacks);
        startService(new Intent(this, GPSTracker.class));
        startService(new Intent(this, OnlineStatusService.class));

        //database
        mDatabaseUsersOnline = FirebaseDatabase.getInstance().getReference().child("users_online");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

    }

   /* public boolean isForeground(sema) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }*/

    sema.ActivityLifecycleCallbacks activityCallbacks = new sema.ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
           /* mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).setValue("isOnline");*/
        }
        @Override
        public void onActivityStarted(Activity activity) {
            if (mAuth.getCurrentUser() != null) {
                mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).setValue("isOnline");
            }

        }

        @Override
        public void onActivityResumed(Activity activity) {

            if (wasInBackground) {
                //Do app-wide came-here-from-background code
                if (mAuth.getCurrentUser() != null) {
                    mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).setValue("isOnline");
                }
            }
            stopActivityTransitionTimer();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            startActivityTransitionTimer();
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (mAuth.getCurrentUser() != null) {
                mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).removeValue();
                addToLastSeen();
            }

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (mAuth.getCurrentUser() != null) {
                mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).removeValue();
                addToLastSeen();
            }

        }


    };

    public static synchronized sema getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }


    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                // Task is run when app is exited
                wasInBackground = true;
               /* mDatabaseUsersOnline.child(mAuth.getCurrentUser().getUid()).removeValue();
                addToLastSeen();*/
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }

        this.wasInBackground = false;

    }

    private void addToLastSeen() {

        mDatabaseUsers.child(mAuth.getCurrentUser().getUid()).child("last_seen").setValue(System.currentTimeMillis());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(new Intent(this, GPSTracker.class));
        stopService(new Intent(this, OnlineStatusService.class));
    }

}
