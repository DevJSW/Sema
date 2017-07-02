package com.sema.sema;

import android.app.Application;
import android.content.Context;

/**
 * Created by Shephard on 7/1/2017.
 */

public class sema extends Application {

    private static sema mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized sema getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }

}
