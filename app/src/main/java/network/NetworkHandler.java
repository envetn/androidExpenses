package network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;
import expenses.ExpenseUser;
import expenses.Expenses;
import network.Request.BaseRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.WIFI_STATE_ENABLED;
import static network.Json.JsonHandler.*;

/**
 * Created by olof on 2016-04-04.
 */
public class NetworkHandler
{
    private static final String TAG = "NetworkHandler";
    private static final int SLEEP_TIME = 1000;

    private final static int PORT = 9875;
    private final static String ipAddr = "192.168.1.3";


//    private Handler networkThreadHandler;
//    private WifiConfiguration myWifiConfiguration;
    //private Context myContext;
    private WifiManager myWifiManager;
    private WifiInfo myWifiInfo;

    private ConnectivityManager myConnectManager;


    private List<Expenses> expensesData;
    private List<String> expensesToBeRemoved;
    private List<String> idsSentSuccessful;
    private JSONObject myUserObject;

    public NetworkHandler(Context context)
    {
//        myContext = context;
        myWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        myWifiConfiguration = new WifiConfiguration();
        myConnectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        expensesData = new ArrayList<>();
        expensesToBeRemoved = new ArrayList<>();

//        networkThreadHandler = new Handler();
    }

    public boolean isOnline()
    {
        NetworkInfo activeNetwork = myConnectManager.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnected());
    }

    public boolean isHomeWifi()
    {
        NetworkInfo activeNetwork = myConnectManager.getActiveNetworkInfo();

        if (activeNetwork != null && myWifiManager.getWifiState() == WIFI_STATE_ENABLED)
        {
            try
            {
                //TODO only enter when wifi enabled
                myWifiInfo = myWifiManager.getConnectionInfo();
                String ssid = myWifiInfo.getSSID().replace("\"", "");
                return ssid.equals("*");
            }
            catch (NullPointerException e)
            {
                Log.e(TAG, "Failed to parse SSID");
            }
        }
        return false;

    }

    public boolean removeRequest(List<String> itemsToRemove) throws JSONException
    {
        JSONObject jsonRequest = createJsonRemoveRequest(itemsToRemove);
        String response = executeNetworkPost(jsonRequest);

        return isSuccessFul(response);
    }

    public List<Expenses> createGetRequest(BaseRequest request) throws JSONException, ParseException
    {
        JSONObject jsonRequest = createJsonGetRequest(request);
        String response  = executeNetworkPost(jsonRequest);

        List<Expenses> expenses = new ArrayList<>();

        if (isSuccessFul(response))
        {
            expenses = createExpensesObject(response);
        }
        return expenses;
    }

    public Boolean putRequest() throws JSONException
    {
        idsSentSuccessful = new ArrayList<>();

        for (Expenses expenseData : expensesData)
        {
            //TODO: Create an array of objects instead of sending one by one
            JSONObject jsonObject = createJsonPutRequest(expenseData);
            String response = executeNetworkPost(jsonObject);

            if (isSuccessFul(response))
            {
                idsSentSuccessful.add(expenseData.getUniqueId());
            }

        }
        return !idsSentSuccessful.isEmpty();
    }

    private String executeNetworkPost(JSONObject content) throws JSONException
    {
        content.put("user", myUserObject);

        String request = content.toString() + System.getProperty("line.separator");
        String response;

        try
        {
            URL url = new URL("http://192.168.1.3:8080/RESTexample/rest/expenses/json");
            HttpURLConnection connection = initUrlConnection(url);

            //        OutputStreamWriter outputStrem = new OutputStreamWriter(connection.getOutputStream());
            OutputStream output = connection.getOutputStream();
            output.write(request.getBytes());
            output.flush();

            connection.connect();
            int result = connection.getResponseCode();
            if (hasHttpFailed(result))
            {
                connection.disconnect();
                output.close();

                Log.v(TAG, "ResponseCode: " + result);
                return "Failed to connect Http status: " + result;
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            Log.i(TAG, "Waiting for response");
            response = bufferedReader.readLine();
            Log.i(TAG, "Got response: " + response);

            connection.disconnect();

            bufferedReader.close();
            output.close();
        }
        catch (IOException e)
        {
            Log.e("NetworkHandler", "executeNetworkPost: " + e.getMessage());
            response = "Exception caught: " + e.getMessage();
        }

        return response;
    }

    private HttpURLConnection initUrlConnection(URL url) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setReadTimeout(10000 /* milliseconds */);
        connection.setConnectTimeout(15000 /* milliseconds */);
        connection.setDoInput(true);

        return connection;
    }

    private boolean hasHttpFailed(int result)
    {
        //need true if failed
        return (result != HttpURLConnection.HTTP_ACCEPTED
                && result != HttpURLConnection.HTTP_CREATED
                && result != HttpURLConnection.HTTP_OK);
    }

    private String executeNetworkRequest(JSONObject content)
    {
        String request = content.toString();
        request += System.getProperty("line.separator");
        String response;

        BufferedReader bufferReaderIn;
        PrintWriter printWriterOut;

        try
        {
            Socket socket = new Socket(ipAddr, PORT);
            printWriterOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            bufferReaderIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            printWriterOut.write(request);
            printWriterOut.flush();

            Log.i("NetworkHandler", "Waiting for response");
            response = bufferReaderIn.readLine();

            Log.i("NetworkHandler", "Got response: " + response);

            if (!socket.isConnected())
            {
                socket.close();
                bufferReaderIn.close();
                printWriterOut.close();
            }

            SystemClock.sleep(SLEEP_TIME);
        }
        catch (IOException e)
        {
            Log.e("NetworkHandler", "executeNetworkRequest: " + e.getMessage());
            response = "Exception caught: " + e.getMessage();
        }

        return response;
    }

    public void appendData(Expenses expense)
    {
        expensesData.add(expense);
    }
    public void addUser(ExpenseUser user)
    {
        //TODO: figure out a better way
        myUserObject = new JSONObject();
        try
        {
            myUserObject.put("userId", user.getUsername());
            myUserObject.put("username", user.getUsername());
            myUserObject.put("password", user.getPassword());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    public void clearData()
    {
        expensesData.clear();
    }

    public void appendDataToRemove(List<String> ids)
    {
        Log.v(TAG, "Saving removed ids for later use");
        expensesToBeRemoved.addAll(ids);
    }

    public void emptyDataToRemove()
    {
        expensesToBeRemoved.clear();
    }

    public List<String> getIdsToRemove()
    {
        return expensesToBeRemoved;
    }

    public List<String> getSuccessfulIds()
    {
        return idsSentSuccessful;
    }
}
