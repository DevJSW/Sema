package com.sema.sema;

import android.*;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*import static com.facebook.login.widget.ProfilePictureView.TAG;*/

/**
 * Created by shephard on 6/27/17.
 */

public class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CALLBACK = 1;
    private ProgressDialog mProgressDialog;

    private DatabaseReference onlinePresenceReference;
    private DatabaseReference onlineRef;
    private DatabaseReference currentUserRef;

    public static String getEmail() {
        String email;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else {
            email = null;
        }
        return email;
    }

    public String getUid() {
        String Uid;
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Uid = null;
        }
        return Uid;
    }

    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

   /* public void setupFont() {
        FontUtility.replaceDefaultFont(this, "DEFAULT", "fonts/Montserrat-Regular.ttf");
    }*/

    public void initPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CALLBACK);
            }

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_PERMISSIONS_CALLBACK);
            }

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CALLBACK);
            }

        }

    }

    public void initOnlinePresence() {

        onlinePresenceReference = FirebaseDatabase.getInstance().getReference();
        onlineRef = onlinePresenceReference.child(".info/connected");
        currentUserRef = onlinePresenceReference.child("/users_online/" + getUid());

       /* onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange:" + dataSnapshot);
                if (dataSnapshot.getValue(Boolean.class)) {
                    currentUserRef.onDisconnect().setValue(System.currentTimeMillis());
                    currentUserRef.setValue(true);
                }
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "onCancelled:" + databaseError);
            }
        });*/

        /*final DatabaseReference onlineViewersCountRef = onlinePresenceReference.child("/presence");
        onlineViewersCountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Log.d(TAG, "DataSnapshot:" + dataSnapshot);
                onlineViewerCountTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(final DatabaseError databaseError) {
                Log.d(TAG, "DatabaseError:" + databaseError);
            }
        });*/
    }


}
