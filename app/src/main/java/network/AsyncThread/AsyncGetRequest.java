package network.AsyncThread;

import android.os.AsyncTask;
import android.util.Log;
import expenses.Expenses;
import network.NetworkHandler;
import network.Request.BaseRequest;
import network.Request.GetRequest;
import org.json.JSONException;

import java.text.ParseException;
import java.util.List;

import static utils.Utils.getCurrentDate;
import static utils.Utils.getCurrentMonth;

/**
 * Created by olof on 2016-10-03.
 */
public class AsyncGetRequest extends AsyncTask<Expenses, Void, List<Expenses>> implements AbstractAsyncReqyest
{
    private final NetworkHandler myNetworkHandler;
    private static final String TAG = "AsyncGetRequest";
    private boolean networkTaskDone;
    private BaseRequest myRequest;

    public AsyncGetRequest(NetworkHandler networkHandler, BaseRequest request)
    {
        myNetworkHandler = networkHandler;
        myRequest = request;
        networkTaskDone = false;
    }
    @Override
    protected List<Expenses> doInBackground(Expenses... params)
    {

        List<Expenses> responseData = null;
        try
        {
            responseData = myNetworkHandler.createGetRequest(myRequest);
        }
        catch (JSONException | ParseException e)
        {
            e.printStackTrace();
            Log.e(TAG, "buildJsonRequest: " + e.toString());
        }
        networkTaskDone = true;
        return responseData;
    }

    @Override
    public boolean isDone()
    {
        return networkTaskDone;
    }
}
