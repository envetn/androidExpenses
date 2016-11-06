package network.AsyncThread;

import android.os.AsyncTask;
import android.util.Log;
import network.NetworkHandler;
import org.json.JSONException;

import java.util.List;

/**
 * Created by olof on 2016-10-03.
 */
public class AsyncRemoveRequest extends AsyncTask<String, Void, Boolean> implements AbstractAsyncReqyest
{
    private final List<String> myItemsToRemove;
    private final NetworkHandler myNetworkHandler;
    private static final String TAG = "AsyncRemoveRequest";
    private boolean networkTaskDone;

    public AsyncRemoveRequest(List<String> itemsToRemove, NetworkHandler networkHandler)
    {
        myItemsToRemove = itemsToRemove;
        myNetworkHandler = networkHandler;
        networkTaskDone = false;
    }

    @Override
    protected Boolean doInBackground(String... urls)
    {
        try
        {
            return myNetworkHandler.removeRequest(myItemsToRemove);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Log.e(TAG, "buildJsonRemoveRequest: " + e.toString());
        }
        return false;
    }

    @Override
    public boolean isDone()
    {
        return networkTaskDone;
    }
}
