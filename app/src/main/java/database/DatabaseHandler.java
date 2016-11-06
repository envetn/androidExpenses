package database;

import android.content.Context;
import android.util.Log;
import expenses.ExpenseUser;
import expenses.Expenses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by olof on 2016-04-07.
 */
public class DatabaseHandler
{
    private static final String TAG ="DatabaseHandler";

    /* flags -> enum?*/
    public final static String FLAG_ONLY_LOCAL = "OnlyLocal";
    public final static String ALL = "all";

    private DatabaseConnection myDatabaseConnection;

    public DatabaseHandler(Context context)
    {
        myDatabaseConnection = new DatabaseConnection(context);
    }


    public void clearExpensesDatabase()
    {
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            myDatabaseConnection.clearTable();
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToUpdateDbEntry");
            //wait
        }
    }
    /**
     * Push expense data to phones local database
     *
     * @param expenses
     * @return
     */
    public boolean requestToPushToTable(List<Expenses> expenses)
    {

        boolean success = false;
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            for(Expenses expense : expenses)
            {
                success = myDatabaseConnection.pushToTable(expense);
                if(!success)
                {
                    return false;
                }
            }
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToPushToTable");
            //wait
        }
        return success;
    }

    /**
     * Read from phones local database
     *
     * @return
     */
    public List<Expenses> requestDataFromDb(String flag)
    {
        List<Expenses> listOfExpenses = new ArrayList<>();
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            listOfExpenses = myDatabaseConnection.readFromTable(flag);
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestDataFromDb");
            //wait
        }

        return listOfExpenses;
    }

    /**
     * Update entry isLocalOnly -> false
     *
     * @param entryToUpdate
     */
    public void requestToUpdateDbEntry(String entryToUpdate)
    {
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            myDatabaseConnection.updateEntryNoLongerLocalOnly(entryToUpdate);
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToUpdateDbEntry");
            //wait
        }
    }

    /**
     * Remove local entry given the id
     *
     * @param id
     */
    public void requestToRemoveEntry(String id)
    {
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            myDatabaseConnection.removeSingleEntry(id);
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToRemoveEntry");
            //wait
        }
    }

    /**
     * Remove failed entry given the id
     *
     * @param entries
     */
    public void requestToRemoveFailedEntry(List<String> entries)
    {
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            myDatabaseConnection.removeFailedEntries(entries);
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToRemoveFailedEntry");
            //wait
        }
    }

    /**
     * Remove failed entry given the id
     *
     * @param entries
     */
    public void requestToPutFailedEntries(List<String> entries)
    {
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            myDatabaseConnection.putFailedEntries(entries);
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToPutFailedEntries");
            //wait
        }
    }

    /**
     * Fetch failed entries to remove
     */
    public List<String> requestToFetchFailedEntries()
    {
        List<String> entries = Collections.emptyList();
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            entries = myDatabaseConnection.getEntriesToRemoveFromRemote();
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToFetchFailedEntries");
            //wait
        }
        return entries;
    }

    /**
     * Fetch failed entries to remove
     */
    public boolean requestToCreateUser(String userName, String password)
    {
        boolean success = false;
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            success = myDatabaseConnection.createUser(userName, password);
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToFetchFailedEntries");
            //wait
        }
        return success;
    }

    /**
     * Fetch failed entries to remove
     */
    public ExpenseUser fetchUserName()
    {
        ExpenseUser userName = null;
        if (!myDatabaseConnection.isDatabaseOpen())
        {
            myDatabaseConnection.openDatabase();
            userName= myDatabaseConnection.fetchUser();
            myDatabaseConnection.closeDatabaseConnection();
        }
        else
        {
            Log.v(TAG, "Request towards db failed: requestToFetchFailedEntries");
            //wait
        }
        return userName;
    }

}
