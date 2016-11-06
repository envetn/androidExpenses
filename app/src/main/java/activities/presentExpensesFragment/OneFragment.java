package activities.presentExpensesFragment;

import activities.PresentExpensesActivity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.olof.myapplication2.R;
import database.DatabaseHandler;
import expenses.Dropdown.MonthDropDown;
import expenses.Expenses;
import expenses.ExpensesAdapter;
import network.NetworkHandler;
import utils.Utils;

import java.util.*;

import static utils.Utils.filterExpensesTo;
import static utils.Utils.getCurrentMonth;

/**
 * Created by olof on 2016-09-10.
 */
public class OneFragment extends Fragment
{
    private static final String TAG = "OneFragment";
    /**
     * Other
     */
    private Context myContext;
    private DatabaseHandler myDatabaseHandler;
    private List<Expenses> myExpenses = new ArrayList<>();
    private Handler activityThreadHandler;
    private NetworkHandler myNetworkHandler;
    private ExpensesAdapter expenseAdapter;


    /**
     * Normal
     */
    private boolean shouldFetch = true;
    private boolean networkTaskDone = false;
    private String mySortType = "Date";
    private String mySelectedMonth = getCurrentMonth();

    /**
     * GUI
     */
    private TextView tv_foodCost, tv_foodOutCost, tv_billCost, tv_travelCost, tv_otherCost, tv_l_monthShown;
    private Button btn_test;
    private LayoutInflater myLayoutInflater;
    private Resources myResources;
    private boolean createViewDone = false;
    private PresentExpensesActivity parentActivity;
    private Spinner sp_month;
    private View myView;

    public OneFragment()
    {
    }

    public void setParentActivity(PresentExpensesActivity presentExpensesActivity)
    {
        parentActivity = presentExpensesActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        activityThreadHandler = new Handler();
        activityThreadHandler.postDelayed(fetchRemoteDataRunnable, 500); // 0.5sec
    }

    private Runnable fetchRemoteDataRunnable = new Runnable()
    {
        List<Expenses> remoteExpenses = new ArrayList<>();
        int maxWait = 6;
        int wait = 0;

        @Override
        public void run()
        {
            if (parentActivity.isNetworkTaskDone() && createViewDone)
            {//TODO: add has changed in remote db, the list of received expenses can get quite big after a few month

                remoteExpenses = parentActivity.getLoadedExpenses();
                Log.v(TAG, "setting textView data from network-task");
                if(!remoteExpenses.isEmpty())
                {
                    if(!parentActivity.getDatabaseHandler().requestToPushToTable(remoteExpenses))
                    {
                        Log.v(TAG, "Failed to store remote data in db");
                    }
                    myExpenses.addAll(remoteExpenses);
                    appendTextView();
                }

            }
            else if (wait > maxWait)
            {
                Log.v(TAG, "Network or viewTask not done... waiting...");
                wait++;
                activityThreadHandler.postDelayed(fetchRemoteDataRunnable, 100); // 0.1sec
            }
            else
            {
                Log.e(TAG, "Failed to load data..");
            }
        }
    };

    private void appendTextView()
    {
        List<Expenses> filtered = filterExpensesTo(myExpenses, mySelectedMonth);

        double foodCost = 0.0;
        double foodOutCost = 0.0;
        double billCost = 0.0;
        double travelCost = 0.0;
        double otherCost = 0.0;

        for (Expenses expense : filtered)
        {
            switch (expense.getCostType())
            {
                case "Mat":
                    foodCost += expense.getCost();
                    break;

                case "Mat ute":
                    foodOutCost += expense.getCost();
                    break;

                case "Räkning":
                    billCost += expense.getCost();
                    break;

                case "Resa":
                    travelCost += expense.getCost();
                    break;

                case "Övrigt":
                    otherCost += expense.getCost();
                    break;
            }
        }

        //hm
        tv_foodCost.setText(createSpannable(foodCost, "Mat", filtered), TextView.BufferType.SPANNABLE);
        tv_foodCost.setMovementMethod(LinkMovementMethod.getInstance());
        tv_foodOutCost.setText(createSpannable(foodOutCost, "Mat ute", filtered), TextView.BufferType.SPANNABLE);
        tv_foodOutCost.setMovementMethod(LinkMovementMethod.getInstance());
        tv_billCost.setText(createSpannable(billCost, "Räkning", filtered), TextView.BufferType.SPANNABLE);
        tv_billCost.setMovementMethod(LinkMovementMethod.getInstance());
        tv_travelCost.setText(createSpannable(travelCost, "Resa", filtered), TextView.BufferType.SPANNABLE);
        tv_travelCost.setMovementMethod(LinkMovementMethod.getInstance());
        tv_otherCost.setText(createSpannable(otherCost, "Övrigt", filtered), TextView.BufferType.SPANNABLE);
        tv_otherCost.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private SpannableStringBuilder createSpannable(double value, final String action, final List<Expenses> filtered)
    {
        String currency = myResources.getString(R.string.Currency_kr);
        final String text = value + " " + currency;

        SpannableStringBuilder fancySpannable = new SpannableStringBuilder(text);
        fancySpannable.setSpan(new ClickableSpan()
        {
            @Override public void onClick(View widget)
            {
                Log.v(TAG, action + " was clicked");
                PopulatePopupWith(action, filtered);
            }
        }, 0, text.length(), 0);

        return fancySpannable;
    }

    private void PopulatePopupWith(String action, List<Expenses> filtered)
    {
        List<Expenses> filterByAction = new ArrayList<>();

        for (Expenses expense : filtered)
        {
            if (expense.getCostType().equals(action))
            {
                filterByAction.add(expense);
            }
        }

        if (myLayoutInflater != null)
        {
            Map<String, Integer> dimension = Utils.getApplicationDimensions(parentActivity);
            View popupLayout = myLayoutInflater.inflate(R.layout.fragment_one_popup, null);
            popupLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

            Log.v(TAG, "Creating popup with: width: " + (dimension.get("width")) + " and height: " + dimension.get("height"));

            final PopupWindow popupWindow = new PopupWindow(popupLayout, dimension.get("width")/* + addedWidth*/, dimension.get("height")-150, true); //width height
            popupWindow.showAtLocation(popupLayout, Gravity.CENTER, 0, 0);


            expenseAdapter = new ExpensesAdapter(popupLayout.getContext(), filterByAction, mySortType, "Alla");

            ListView lv_listView = (ListView) popupLayout.findViewById(R.id.popup_listView);
            lv_listView.setAdapter(expenseAdapter);

            Button btn_close = (Button) popupLayout.findViewById(R.id.btn_close_popup_one);
            btn_close.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    popupWindow.dismiss();
                }
            });

            Button btn_remove = (Button) popupLayout.findViewById(R.id.btn_remove_expense);
            btn_remove.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    List<String> toRemove = expenseAdapter.getItemsToRemove();
                    if(!toRemove.isEmpty())
                    {
                        parentActivity.setExpensesToRemove(toRemove);
                        parentActivity.startRemoteNetworkThread();
                    }
                }
            });
        }
        else
        {
            //display error?
        }
        //Todo: Create and display popup
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState)
    {
        myView = inflater.inflate(R.layout.fragment_one, container, false);
        myResources = myView.getResources();
        myLayoutInflater = inflater;

        tv_foodCost = (TextView) myView.findViewById(R.id.tv_one_foodCost);
        tv_foodOutCost = (TextView) myView.findViewById(R.id.tv_one_foodOutCost);
        tv_billCost = (TextView) myView.findViewById(R.id.tv_one_billCost);
        tv_travelCost = (TextView) myView.findViewById(R.id.tv_one_travelCost);
        tv_otherCost = (TextView) myView.findViewById(R.id.tv_one_otherCost);
        sp_month = (Spinner) myView.findViewById(R.id.sp_SelectMonth);


        //todo: write this better
        MonthDropDown sortTypeDropdown = new MonthDropDown(myView);
        sortTypeDropdown.updateDropdown();
        sp_month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (parentActivity.isNetworkTaskDone() && createViewDone)
                {
                    mySelectedMonth = parent.getItemAtPosition(position).toString();
                    Log.v(TAG, "## Selected month ## " + mySelectedMonth);
                    appendTextView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        createViewDone = true;
        Log.v(TAG, "Create view done");
        myExpenses = parentActivity.getLocalExpenses();

        appendTextView();

        return myView;
    }
}
