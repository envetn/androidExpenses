package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import expenses.ExpenseUser;
import expenses.Expenses;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.MODE_PRIVATE;
import static database.DatabaseHandler.FLAG_ONLY_LOCAL;
import static utils.Utils.getCurrentDate;

/**
 * Created by olof on 2016-04-07.
 */
public class DatabaseConnection
{

    private static final String DB_TABLE = "Expenses";
    private static final String TAG = "DatabaseHandler";

    private final static String UUID_ROW = "uniqueId";
    private final static String COST_ROW = "cost";

    private Context myContext;
    private SQLiteDatabase mySqlDatabase;

    private boolean isOpen = false;
    private AtomicInteger counter = new AtomicInteger(0);

    public DatabaseConnection(Context context)
    {
        myContext = context;
        //        initDatabase();
    }

    public boolean isDatabaseOpen()
    {
        return isOpen;
    }

    public void openDatabase()
    {
        try
        {
            mySqlDatabase = myContext.openOrCreateDatabase(DB_TABLE, MODE_PRIVATE, null);

            //removeTable();
            mySqlDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                    "Expenses " +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "cost VARCHAR," +
                    "costType VARCHAR," +
                    "buyDate DATE," +
                    "comment VARCHAR," +
                    "uniqueId VARCHAR," +
                    "isOnlyLocal INTEGER)");

            mySqlDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                    "User " +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userName VARCHAR," +
                    "password VARCHAR)"); //TODO: hash later

            // To avoid duplicated
            mySqlDatabase.execSQL("CREATE TABLE IF NOT EXISTS " +
                    "Expenses_to_remove " +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uniqueId VARCHAR," +
                    "removeDate DATE)");

            isOpen = true;

        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }

    public void clearTable()
    {
        Log.d("DatabaseHandler", "Cleared database");
        mySqlDatabase.execSQL("DELETE FROM Expenses");
        mySqlDatabase.execSQL("DELETE FROM Expenses_to_remove");
    }

    public void removeTable()
    {
        Log.d("DatabaseHandler", "Removed table from database");
        mySqlDatabase.execSQL("DROP TABLE IF EXISTS Expenses");
    }

    private Date stringToDate(String date)
    {
        return Date.valueOf(date);
    }

    /**
     * Push expense data to phones local database
     *
     * @param myExpenses
     * @return
     */
    public boolean pushToTable(Expenses myExpenses)
    {

        double cost = myExpenses.getCost();
        String costType = myExpenses.getCostType();
        Date costDate = myExpenses.getDate();
        String comment = myExpenses.getComment();
        String uniqueId = myExpenses.getUniqueId();
        int onlyLocal = myExpenses.getIsOnlyLocal() ? 1 : 0;

        ContentValues values = new ContentValues();
        values.put(COST_ROW, cost);
        values.put("costType", costType);
        values.put("buyDate", costDate.toString());
        values.put("comment", comment);
        values.put(UUID_ROW, uniqueId);
        values.put("isOnlyLocal", onlyLocal);

        try
        {
            mySqlDatabase.insert("Expenses", null, values);
            return true;

        }
        catch (Exception e)
        {
            Log.e(this.toString(), e.toString());
        }
        return false;
    }

    /**
     * Read from phones local database
     *
     * @return
     */
    public List<Expenses> readFromTable(String flag)
    {
        String sql = "SELECT * FROM Expenses";
        if (flag.equals(FLAG_ONLY_LOCAL))
        {
            sql += " WHERE isOnlyLocal = 1";

        }

        List<Expenses> listOfExpenses = new ArrayList<>();

        try
        {
            Cursor dbCursor = mySqlDatabase.rawQuery(sql, null);

            if (dbCursor.moveToFirst())
            {
                do
                {
                    Expenses expense = new Expenses.ExpensesBuilder()
                            .setCost(dbCursor.getDouble(1))
                            .setCostType(dbCursor.getString(2))
                            .setDate(stringToDate(dbCursor.getString(3)))
                            .setComment(dbCursor.getString(4))
                            .setUniqueId(dbCursor.getString(5))
                            .setIsOnlyLocal(dbCursor.getInt(6))
                            .setIsRemoteData(false)
                            .build();
                    listOfExpenses.add(expense);
                }
                while (dbCursor.moveToNext());
            }
            dbCursor.close();
        }
        catch (Exception e)
        {
            Log.e(this.toString(), e.toString());
        }
        return listOfExpenses;
    }

    /**
     * Get entries that where removed locally but not on remote
     * <p>
     * If error occurs while removing entries and connection with remote server was lost, <br/>
     * the entries uuid is saved so when connection is established again they can be removed
     *
     * @return
     */
    public List<String> getEntriesToRemoveFromRemote()
    {
        String sql = "SELECT * FROM Expenses_to_remove";
        List<String> listOfExpenses = new ArrayList<>();

        try
        {
            Cursor dbCursor = mySqlDatabase.rawQuery(sql, null);

            if (dbCursor.moveToFirst())
            {
                do
                {
                    listOfExpenses.add(dbCursor.getString(1));
                }
                while (dbCursor.moveToNext());
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
        return listOfExpenses;
    }

    /**
     * Put failed entries to remove into database
     *
     * @param entries
     */
    public void putFailedEntries(List<String> entries)
    {
        for (String entry : entries)
        {
            ContentValues values = new ContentValues();
            values.put("uniqueId", entry);
            values.put("removeDate", getCurrentDate().toString());

            mySqlDatabase.insert("Expenses_to_remove", null, values);
        }
    }

    /**
     * Remove failed entries
     */
    public boolean removeFailedEntries(List<String> entries)
    {
        int affectedRow = 0;
        for (String entry : entries)
        {
            mySqlDatabase.delete("Expenses_to_remove", "uniqueId" + "=" + '?', new String[] { entry });
        }
        if (affectedRow == entries.size())
        {
            Log.v(TAG, "Removed all failed entries");
        }
        else if (affectedRow > 0)
        {
            return true;
        }

        return false;
    }

    /**
     * Update entry isLocalOnly -> false
     *
     * @param entryToUpdate
     */
    public void updateEntryNoLongerLocalOnly(String entryToUpdate)
    {
        String contentKey = "isOnlyLocal";

        ContentValues values = new ContentValues();
        values.put(contentKey, "true");

        int affectedRow = mySqlDatabase.update(DB_TABLE, values, UUID_ROW + "=" + '?', new String[] { entryToUpdate });

        if (affectedRow == 1)
        {
            Log.d("DatabaseHandler", "Updating entry with id: " + entryToUpdate);
        }
        else
        {
            Log.d("DatabaseHandler", "Failed to Updating entry with id: " + entryToUpdate);
        }
    }

    /**
     * Remove local entry given the id
     *
     * @param id
     */
    public void removeSingleEntry(String id)
    {

        int affectedRow = mySqlDatabase.delete(DB_TABLE, UUID_ROW + "=" + '?', new String[] { id });
        if (affectedRow == 1)
        {
            Log.d("DatabaseHandler", "Removing entry with id: " + id);
        }
        else
        {
            Log.d("DatabaseHandler", "Failed to remove entry with id: " + id);
        }
    }

    public boolean createUser(String userName, String password)
    {
        if (fetchUser() == null)
        {
            ContentValues values = new ContentValues();
            values.put("userName", userName);
            values.put("password", password);

            try
            {
                mySqlDatabase.insert("User", null, values);
                return true;

            }
            catch (Exception e)
            {
                Log.e(this.toString(), e.toString());
            }
        }
        return false;
    }

    public ExpenseUser fetchUser()
    {
        String sql = "SELECT * FROM User";
        try
        {
            Cursor dbCursor = mySqlDatabase.rawQuery(sql, null);

            if (dbCursor.moveToFirst())
            {
                String userName = dbCursor.getString(1);
                String password = dbCursor.getString(2);

                return new ExpenseUser(userName, password);
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }

        return null;
    }

    /**
     * 06-27 22:00:29.067 868-877/com.example.olof.android_expense W/SQLiteConnectionPool: A SQLiteConnection object for database '/data/data/com.example.olof.android_expense/databases/Expenses' was leaked!
     * Please fix your application to end transactions in progress properly and to close the database when it is no longer needed.
     */
    public void closeDatabaseConnection()
    {
        mySqlDatabase.close();
        isOpen = false;
    }
}
