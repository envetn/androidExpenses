package activities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import com.example.olof.myapplication2.R;
import database.DatabaseHandler;
import expenses.Dropdown.CostTypeDropdown;
import expenses.Dropdown.SortTypeDropdown;
import expenses.ExpenseUser;
import expenses.Expenses;
import expenses.ExpensesAdapter;
import network.AsyncThread.AsyncGetRequest;
import network.AsyncThread.AsyncRemoveRequest;
import network.NetworkHandler;
import network.Request.BaseRequest;
import network.Request.GetRequest;
import org.json.JSONException;
import utils.Utils;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

;import static database.DatabaseHandler.ALL;
import static utils.Utils.getCurrentDate;
import static utils.Utils.getCurrentMonth;

/**
 * Created by olof on 2016-04-07.
 */
public class ListActivity extends Activity
{
    private final static String TAG = "ListActivity";
    private static NetworkHandler myNetworkHandler;
    private static DatabaseHandler myDatabaseHandler;

    private boolean shouldFetch = true;

    private ListView lv_listOfExpenses;
    private Spinner sp_expenseSortType;
    private Spinner sp_expensesShowType;
    private View progressOverlay;
    private Button bn_remove;

    private Context myContext;


    private List<Expenses> myExpenses = new ArrayList<>();

    private Handler activityThreadHandler;
    private boolean networkTaskDone = false;
    private String mySortType = "Date";
    private String myShowType = "All";

    private ExpensesAdapter myExpenseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        myContext = this.getApplicationContext();

        setContentView(R.layout.list_activity);
        myDatabaseHandler = new DatabaseHandler(this.getApplicationContext());

        myExpenses = myDatabaseHandler.requestDataFromDb(ALL);

        initGui();
        initButtons();

        myNetworkHandler = new NetworkHandler(myContext);
        //Dropdown
        CostTypeDropdown showTypeDropdown = new CostTypeDropdown(this, "show");
        showTypeDropdown.updateDropdown(true);

        SortTypeDropdown sortTypeDropdown = new SortTypeDropdown(this);
        sortTypeDropdown.updateDropdown();

        populateScrollView();
        activityThreadHandler = new Handler();
//        activityThreadHandler.post(dropdownRunnable);
        activityThreadHandler.postDelayed(fetchRemoteDataRunnable, 500);

    }

    /**
     * If home wifi
     * - Select local data with localOnly=true
     * - Fetch from database.database
     * - Remove duplicates (Shouldn't be any)
     * <p>
     * else (Not home wifi, or not able to connect to remote db)
     * - Print all local data
     */
    private Runnable fetchRemoteDataRunnable = new Runnable()
    {
        List<Expenses> remoteExpenses = new ArrayList<>();

        @Override
        public void run()
        {
            if (!networkTaskDone)
            {
                //spin spin
                try
                {
                    boolean isHomeWifi = myNetworkHandler.isHomeWifi();
                    if (isHomeWifi && shouldFetch)
                    {

                        ExpenseUser expensesUser = myDatabaseHandler.fetchUserName();
                        myNetworkHandler.addUser(expensesUser);

                        BaseRequest myGetRequest = GetRequest.newBuilder()
                                .setDate(getCurrentDate())
                                .setId("Expenses")
                                .setOrderBY("buyDate DESC")
                                .setLowerLimit(0)
                                .setUpperLimit(20)
                                .setFetchPeriod(getCurrentMonth())
                                .build();
                        Log.v(TAG, "Fetching remote data...");
                        AsyncGetRequest fetchData = new AsyncGetRequest(myNetworkHandler, myGetRequest);
                        remoteExpenses = fetchData.execute().get(); //.get(timeout, TimeUnit.MILLISECONDS);
                        networkTaskDone = fetchData.isDone();

                        activityThreadHandler.postDelayed(fetchRemoteDataRunnable, 200);
                    }
                    else
                    {
                        Log.v(TAG, "No network connection");
                        //No network connection
                        networkTaskDone = true;
                        remoteExpenses = Collections.emptyList();
                    }
                }
                catch (InterruptedException | ExecutionException  e)
                {
                    Log.e(TAG, "Exception was thrown: " + " Type: " + e.getClass() + "\n" + e.toString());
                }
            }
            else
            {
//                Utils.animateView(progressOverlay, View.GONE, 0, 200);

                appendRemoteData(remoteExpenses);
            }
        }
    };

    private Runnable dropdownRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            populateScrollView();
        }
    };

    private void appendRemoteData(List<Expenses> remoteExpenses)
    {
        //if local expenses and remote expenses are the same size they contain the same data
        if (!remoteExpenses.isEmpty() && myExpenses.size() != remoteExpenses.size())
        {
            for (Expenses expense : myExpenses)
            {
                if (!expense.getIsOnlyLocal() && remoteExpenses.contains(expense))
                {
                    remoteExpenses.remove(expense);
                }
            }

            appendToAdapter(remoteExpenses);
//            populateScrollView(); // repopulate ?
        }
    }


    private void appendToAdapter(List<Expenses> expenses)
    {
        if(myExpenseAdapter == null || myExpenseAdapter.isEmpty())
        {
            populateScrollView();
        }
        else
        {
            myExpenseAdapter.appendDataAndNotifyChange(expenses);
        }
    }
    private void populateScrollView()
    {
        final Context myContext = this.getApplicationContext();

//        final ExpensesAdapter adapter = new ExpensesAdapter(myContext, myExpenses, mySortType, myShowType);
//        lv_listOfExpenses.setAdapter(adapter);

        myExpenseAdapter = new ExpensesAdapter(myContext, myExpenses, mySortType, myShowType);
        lv_listOfExpenses.setAdapter(myExpenseAdapter);


        bn_remove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                List<String> entryToRemove = myExpenseAdapter.getItemsToRemove();
                if (!entryToRemove.isEmpty())
                {
                    removeEntryFromDatabase(entryToRemove);
                }

            }
        });
        //Todo: Display single item when clicked
    }

    private void initGui()
    {
        lv_listOfExpenses = (ListView) findViewById(R.id.lv_listOfExpenses);
        bn_remove = (Button) findViewById(R.id.bn_remove);
        //pb_networkProgress = (ProgressBar) findViewById(R.id.pb_networkProgress);
        progressOverlay = findViewById(R.id.progress_overlay);
        sp_expenseSortType = (Spinner) findViewById(R.id.sp_expenseSortType);
        sp_expensesShowType = (Spinner) findViewById(R.id.sp_expensesShowType);
    }

    private void initButtons()
    {

        sp_expensesShowType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                myShowType = parent.getItemAtPosition(position).toString();
                Log.v(TAG, "## Selected item type to show ## " + myShowType);
                activityThreadHandler.postDelayed(dropdownRunnable, 200);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        sp_expenseSortType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                mySortType = parent.getItemAtPosition(position).toString();
                Log.v(TAG, "## Selected item to sort after ## " + mySortType);
                activityThreadHandler.postDelayed(dropdownRunnable, 200);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
    }

    private void removeEntryFromDatabase(List<String> itemsToRemove)
    {
        if( myNetworkHandler.isOnline() && myNetworkHandler.isHomeWifi())
        {
            if(!myDatabaseHandler.requestToFetchFailedEntries().isEmpty())
            {
                itemsToRemove.addAll(myDatabaseHandler.requestToFetchFailedEntries());
            }

            try
            {
                ExpenseUser expensesUser = myDatabaseHandler.fetchUserName();
                myNetworkHandler.addUser(expensesUser);
                AsyncRemoveRequest removeThread = new AsyncRemoveRequest(itemsToRemove, myNetworkHandler);
                boolean success = removeThread.execute().get();
                //TODO: what happens if entry already removed in remote database?

                if (!success)
                {
                    myDatabaseHandler.requestToPutFailedEntries(itemsToRemove);
                }
                else
                {
                    Utils.createToast(myContext, "Successfully removed entries");
                    myDatabaseHandler.requestToRemoveFailedEntry(itemsToRemove);
                }

                shouldFetch = true;

            }
            catch (InterruptedException | ExecutionException e)
            {
                Log.e(TAG, "Exception was thrown: " + " Type: " + e.getClass() + "\n" + e.toString());
            }
        }
        else
        {
            Log.v(TAG, "No wifi");
            myDatabaseHandler.requestToPutFailedEntries(itemsToRemove);
        }

        for (String uuid : itemsToRemove)
        {
            myDatabaseHandler.requestToRemoveEntry(uuid);
        }

        updateActivity();
        //populateScrollView();

    }

    private void updateActivity()
    {
        //TODO: reload without fetching new data?
//        shouldFetch = false;
        finish();
        startActivity(getIntent());
    }

}
