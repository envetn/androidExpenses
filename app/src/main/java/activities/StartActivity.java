package activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.example.olof.myapplication2.R;
import database.DatabaseHandler;
import expenses.ExpenseUser;
import expenses.Expenses;
import expenses.ExpensesAdapter;
import network.AsyncThread.AsyncPutRequest;
import network.AsyncThread.AsyncRemoveRequest;
import network.NetworkHandler;
import network.WifiChangedBroadcastReceiver;
import org.json.JSONException;
import utils.Utils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static database.DatabaseHandler.FLAG_ONLY_LOCAL;
import static java.util.Collections.singletonList;
import static utils.Utils.createToast;

public class StartActivity extends Activity
{
    private final static String MESSAGE_SUCCESS = "Successfully sent added Expense to server";
    private final static String MESSAGE_FAILED = "Failed to contact remote server";

    private final static String TAG = "StartActivity";
    private ImageButton ibtn_expense, ibtn_shopList;
    private Button btn_clearDatabase;

    private NetworkHandler myNetworkHandler;
    private static DatabaseHandler myDatabaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            myDatabaseHandler = new DatabaseHandler(this.getApplicationContext());
            myNetworkHandler = new NetworkHandler(getApplicationContext());
            newUserIfNeeded();

            initGui();
            addListenerToBtn();
            checkIntent();

            if (myNetworkHandler.isOnline() && myNetworkHandler.isHomeWifi())
            {
                sendOffData();
                sendDeleteIfNeeded();
            }

            new WifiChangedBroadcastReceiver(this, myNetworkHandler);
        }
        catch (InterruptedException | ExecutionException | IOException e)
        {
            Log.e(TAG, "Exception caught: " + e.toString());
            e.printStackTrace();
        }
    }

    private void newUserIfNeeded()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false))
        {
            startActivity(CreateUserActivity.class);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.apply();
        }
    }

    private void initGui()
    {
        ibtn_expense = (ImageButton) findViewById(R.id.ibtn_expense);
        ibtn_shopList = (ImageButton) findViewById(R.id.ibtn_shopList);

        btn_clearDatabase = (Button) findViewById(R.id.bt_clearDatabase);
        //set text
    }

    private void addListenerToBtn()
    {
        ibtn_expense.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(ExpensesActivity.class);
            }
        });

        ibtn_shopList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(PresentExpensesActivity.class);
            }
        });

        btn_clearDatabase.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                myDatabaseHandler.clearExpensesDatabase();
                createToast(getApplicationContext(), "Database cleared");
            }
        });
    }

    private void checkIntent() throws IOException
    {
        if (getIntent().getSerializableExtra("Expense") != null)
        {
            Expenses expense = (Expenses) getIntent().getSerializableExtra("Expense");
            if (myDatabaseHandler.requestToPushToTable(singletonList(expense)))
            {
                getIntent().removeExtra("Expense");
            }
            // Todo: save for later try
        }
    }

    /**
     * Either {@link StartActivity} calls this or {@link WifiChangedBroadcastReceiver}
     *
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */

    public void sendOffData() throws ExecutionException, InterruptedException
    {

        List<Expenses> listOfExpenses = myDatabaseHandler.requestDataFromDb(FLAG_ONLY_LOCAL);
        if (!listOfExpenses.isEmpty())
        {
            for (Expenses expense : listOfExpenses)
            {
                myNetworkHandler.appendData(expense);
            }
            ExpenseUser expensesUser = myDatabaseHandler.fetchUserName();
            myNetworkHandler.addUser(expensesUser);

            AsyncPutRequest putRequest = new AsyncPutRequest(myNetworkHandler);
            Boolean success = putRequest.execute().get();
            String response = success ? MESSAGE_SUCCESS : MESSAGE_FAILED;
            myNetworkHandler.clearData();

            createToast(this.getApplicationContext(), response);
            updateExpensesEntry();
        }
    }

    public void sendDeleteIfNeeded()
    {
        List<String> entries = myDatabaseHandler.requestToFetchFailedEntries();
        if (!entries.isEmpty())
        {
            ExpenseUser expensesUser = myDatabaseHandler.fetchUserName();
            myNetworkHandler.addUser(expensesUser);
            AsyncRemoveRequest removeThread = new AsyncRemoveRequest(entries, myNetworkHandler);
            try
            {
                boolean success = removeThread.execute().get();

                if (success)
                {
                    Utils.createToast(getApplicationContext(), "Successfully removed entries");
                    myDatabaseHandler.requestToRemoveFailedEntry(entries);
                }
            }
            catch (ExecutionException | InterruptedException e)
            {
                e.printStackTrace();
                Log.e(TAG, "Error: " + e.toString());
            }

        }
    }

    //Change localLocalOnly to false, which means the entry exists both locally and on DB
    private void updateExpensesEntry()
    {
        List<String> successfulIds = myNetworkHandler.getSuccessfulIds();
        for (String entryToUpdate : successfulIds)
        {
            myDatabaseHandler.requestToUpdateDbEntry(entryToUpdate);
        }
    }

    private void startActivity(Class<? extends Activity> classToStart)
    {
        Intent intent = new Intent(StartActivity.this, classToStart);
        StartActivity.this.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_HOME);
        main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(main);
    }
}
