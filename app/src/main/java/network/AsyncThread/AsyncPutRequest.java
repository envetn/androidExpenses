package network.AsyncThread;

import android.os.AsyncTask;
import android.util.Log;
import expenses.Expenses;
import network.NetworkHandler;
import org.json.JSONException;

/**
 * Created by olof on 2016-09-30.
 */
public class AsyncPutRequest extends AsyncTask<Expenses, Void, Boolean> implements AbstractAsyncReqyest
{
    private final NetworkHandler myNetworkHandler;
    private static final String TAG = "AsyncPutRequest";
    private boolean networkTaskDone;

    public AsyncPutRequest(NetworkHandler networkHandler)
    {
        myNetworkHandler = networkHandler;
        networkTaskDone = false;
    }

    protected Boolean doInBackground(Expenses... params)
    {
        boolean response = false;
        try
        {
            response = myNetworkHandler.putRequest();
        }
        catch (JSONException e)
        {
            Log.e(TAG, "SendData: " + e.toString() + " " + response);
        }
        networkTaskDone = true;
        return response;
    }

    protected void onPostExecute(String result)
    {
    }

    @Override
    protected void onPreExecute()
    {
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
    }

    @Override
    public boolean isDone()
    {
        return networkTaskDone;
    }
}
