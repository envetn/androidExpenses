package network;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import activities.StartActivity;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.Utils.createToast;

/**
 * Created by olof on 2016-05-31.
 */
public class WifiChangedBroadcastReceiver// extends BroadcastReceiver
{
    private Activity myActivity;
    private NetworkHandler myNetworkHandler;
    private final static String TAG = "WifiBroadcastReceiver";
    private final static int NUMBER_OF_TRIES = 5;
    private static AtomicBoolean firstRecieved = new AtomicBoolean(false);
    private Handler broadcastReceiverHandler;
    private Intent myIntent;
    private int numberOfTries = 0;

    public WifiChangedBroadcastReceiver(Activity activity, NetworkHandler networkHandler)
    {
        myActivity = activity;
        myNetworkHandler = networkHandler;

        if(firstRecieved.get())
        {
           firstRecieved.set(false);
        }
        else
        {
            broadcastReceiverHandler = new Handler();
            createBroadcastReceiver();
        }
    }

    private void createBroadcastReceiver()
    {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                myIntent = intent;
                broadcastReceiverHandler.postDelayed(fetchRemoteDataRunnable, 500);

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        myActivity.registerReceiver(broadcastReceiver, intentFilter);
    }

    private Runnable fetchRemoteDataRunnable = new Runnable()
    {

        @Override public void run()
        {
            try
            {

                while (numberOfTries < NUMBER_OF_TRIES && !myNetworkHandler.isOnline() && !myNetworkHandler.isHomeWifi())
                {
                    Log.v(TAG, "onReceive - Wifi connection NOT established ## SLEEP 2000 ##");
                    broadcastReceiverHandler.postDelayed(fetchRemoteDataRunnable, 2000);
                    numberOfTries++;
                }

                if (numberOfTries >= NUMBER_OF_TRIES)
                {
                    Log.v(TAG, "onReceive - Could not connect to network..");
                }
                else
                {
                    Log.v(TAG, "firstReceieved: "  + firstRecieved.get());
                    final String action = myIntent.getAction();
                    if (!firstRecieved.get() && action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
                    {
                        firstRecieved.getAndSet(true);

                        if (myIntent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false))
                        {

                            Log.v(TAG, "onReceive - ## Wifi connection established ## " + firstRecieved.get());
                            Log.v(TAG, "onReceive - sending data");
                            ((StartActivity) myActivity).sendOffData();

                            Log.v(TAG, "onReceive - Sending delete request if needed");
                            ((StartActivity) myActivity).sendDeleteIfNeeded();

                            //                                firstRecieved.getAndSet(false);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                //                    firstRecieved.getAndSet(false);
                Log.v(TAG, "Failed to create broadcastReceiver : " + e.toString());
            }
            numberOfTries = 0;
        }
    };
}

