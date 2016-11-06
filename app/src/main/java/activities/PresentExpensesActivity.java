package activities;

import activities.presentExpensesFragment.OneFragment;
import activities.presentExpensesFragment.TwoFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.*;
import java.util.concurrent.ExecutionException;

import android.util.Log;
import com.example.olof.myapplication2.R;
import database.DatabaseHandler;
import expenses.ExpenseUser;
import expenses.Expenses;
import network.AsyncThread.AsyncGetRequest;
import network.AsyncThread.AsyncRemoveRequest;
import network.NetworkHandler;
import network.Request.BaseRequest;
import network.Request.GetRequest;
import utils.Utils;

import static database.DatabaseHandler.ALL;
import static utils.Utils.getCurrentDate;

/**
 * Created by olof on 2016-09-10.
 */
public class PresentExpensesActivity extends AppCompatActivity
{

    private final static String TAG = "PresentExpensesActivity";

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    /**
     * Other
     */
    private Context myContext;
    private DatabaseHandler myDatabaseHandler;
    private List<Expenses> myExpenses = new ArrayList<>();
    private List<Expenses> myRemoteExpenses = new ArrayList<>();
    private Handler activityThreadHandler;
    private NetworkHandler myNetworkHandler;

    /**
     * Normal
     */
    private boolean shouldFetch = true;
    private boolean networkTaskDone = false;
    private List<String> myExpensesToRemove;
    private BaseRequest myGetRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.present_expenses_activity);

        myGetRequest = GetRequest.newBuilder()
                .setDate(getCurrentDate())
                .setId("Expenses")
                .setOrderBY("buyDate DESC")
                .setFetchPeriod("All")
                .build();

        myContext = this.getApplicationContext();
        myDatabaseHandler = new DatabaseHandler(this.getApplicationContext());
        myExpenses = myDatabaseHandler.requestDataFromDb(ALL);
        activityThreadHandler = new Handler();
        activityThreadHandler.postDelayed(fetchRemoteDataRunnable, 400);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    public boolean isNetworkTaskDone()
    {
        return networkTaskDone;
    }

    public void startRemoteNetworkThread()
    {
        activityThreadHandler.postDelayed(remoteRemoteData, 200);
    }

    private void setupViewPager(ViewPager viewPager)
    {
        OneFragment fragmentOne = new OneFragment();
        fragmentOne.setParentActivity(this);

        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager());
        adapter.addFrag(fragmentOne, "Kostnad");
        adapter.addFrag(new TwoFragment(), "TWO");

        viewPager.setAdapter(adapter);
    }

    public List<Expenses> getLoadedExpenses()
    {
        return myRemoteExpenses;
    }

    public List<Expenses> getLocalExpenses()
    {
        return myExpenses;
    }


    public DatabaseHandler getDatabaseHandler()
    {
        return myDatabaseHandler;
    }

    public void setExpensesToRemove(List<String> idList)
    {
        myExpensesToRemove = idList;
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

        @Override
        public void run()
        {
            if (!networkTaskDone)
            {
                //spin spin
                try
                {
                    myNetworkHandler = new NetworkHandler(myContext);
                    boolean isHomeWifi = myNetworkHandler.isHomeWifi();
                    if (isHomeWifi && shouldFetch)
                    {

                        Log.v(TAG, "Fetching remote data...");
                        ExpenseUser expensesUser = myDatabaseHandler.fetchUserName();
                        myNetworkHandler.addUser(expensesUser);
                        AsyncGetRequest fetchData = new AsyncGetRequest(myNetworkHandler, myGetRequest); // send in expense object
                        myRemoteExpenses = fetchData.execute().get();
                        networkTaskDone = fetchData.isDone();

//                        activityThreadHandler.post(fetchRemoteDataRunnable);
                    }
                    else
                    {
                        Log.v(TAG, "No network connection");
                        networkTaskDone = true;
                        myRemoteExpenses = Collections.emptyList();
                    }
                }
                catch (InterruptedException | ExecutionException e)
                {
                    Log.e(TAG, "Exception was thrown: " + " Type: " + e.getClass() + "\n" + e.toString());
                }
            }
            Log.v(TAG, "Appending remote data...");
            filterRemoteData();
        }
    };

    private Runnable remoteRemoteData = new Runnable()
    {
        boolean success;
        @Override
        public void run()
        {
            removeLocalData();
            if( myNetworkHandler.isOnline() && myNetworkHandler.isHomeWifi())
            {
                List<String> failedRemovedEntries = myDatabaseHandler.requestToFetchFailedEntries();
                if (!failedRemovedEntries.isEmpty())
                {
                    myExpensesToRemove.addAll(failedRemovedEntries);
                }

                try
                {
                    ExpenseUser expensesUser = myDatabaseHandler.fetchUserName();
                    myNetworkHandler.addUser(expensesUser);
                    AsyncRemoveRequest removeThread = new AsyncRemoveRequest(myExpensesToRemove, myNetworkHandler);
                    success = removeThread.execute().get();
                    //TODO: what happens if entry already removed in remote database?
                }
                catch (InterruptedException | ExecutionException e)
                {
                    Log.e(TAG, "Exception was thrown: " + " Type: " + e.getClass() + "\n" + e.toString());
                }
            }

            if (success)
            {
                Utils.createToast(myContext, "Successfully removed entries");
                myDatabaseHandler.requestToRemoveFailedEntry(myExpensesToRemove);
            }
            else
            {
                Log.v(TAG, "No wifi or failed to remove remote entries");
                myDatabaseHandler.requestToPutFailedEntries(myExpensesToRemove);
            }
            updateActivity();
        }
    };

    private void updateActivity()
    {
        //TODO: reload without fetching new data?
        //        shouldFetch = false;
        finish();
        startActivity(getIntent());
    }

    private void removeLocalData()
    {
        for (String uuid : myExpensesToRemove)
        {
            myDatabaseHandler.requestToRemoveEntry(uuid);
        }
    }

    private void filterRemoteData()
    {
        Set<Expenses> local = new HashSet<>(myExpenses);
        Set<Expenses> remote = new HashSet<>(myRemoteExpenses);

        for(Expenses expense : local )
        {
            myRemoteExpenses.remove(expense);
            if(myRemoteExpenses.isEmpty())
            {
                break;
            }
        }

        local.addAll(remote);
        myExpenses = new ArrayList<>(local);
    }

    private class ViewPageAdapter extends FragmentPagerAdapter
    {
        private final List<Fragment> myFragmentList = new ArrayList<>();
        private final List<String> myFragmentTitleList = new ArrayList<>();

        ViewPageAdapter(FragmentManager manager)
        {
            super(manager);
        }

        @Override
        public Fragment getItem(int position)
        {
            return myFragmentList.get(position);
        }

        @Override
        public int getCount()
        {
            return myFragmentList.size();
        }

        void addFrag(Fragment fragment, String title)
        {
            //could do it in a other way
            myFragmentList.add(fragment);
            myFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            return myFragmentTitleList.get(position);
        }
    }
}
