package activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.example.olof.myapplication2.R;
import expenses.Dropdown.CostTypeDropdown;
import expenses.Expenses;
import utils.Utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by olof on 2016-04-06.
 */
public class ExpensesActivity extends Activity
{
    private static final String TAG = "ExpensesActivity";

    private Spinner sp_expenseTypes;
    private TextView tv_costText, tv_costComment;
    private EditText et_costValue, et_costComment;
    private CheckBox cb_isExpense;
    private Button btn_saveExpenses;

    private String myCostType = "unselected";
    private Handler dropdownHandler;
    private CostTypeDropdown myDropdown;
    private Runnable dropdownRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            myDropdown.updateDropdown(cb_isExpense.isChecked());
            tv_costText.setText(myDropdown.getCostText());
        }
    };

    private Expenses myExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expenses_activity);
        dropdownHandler = new Handler();
        myDropdown = new CostTypeDropdown(this, "add");
        initGui();
        dropdownHandler.postDelayed(dropdownRunnable, 100);

        addListenerToBtn();
        addListenerToDropdown();
    }

    private void addListenerToDropdown()
    {

        sp_expenseTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                myCostType = parent.getItemAtPosition(position).toString();
                Log.v(TAG, "## Selected item ## " + myCostType);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
    }

    private void addListenerToBtn()
    {
        cb_isExpense.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                dropdownHandler.postDelayed(dropdownRunnable, 100);
            }
        });

        btn_saveExpenses.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (et_costValue.getText() != null && !et_costValue.getText().toString().equals(""))
                {
                    double costValue = getDoubleFromEditText(et_costValue);
                    Date currentDate = Utils.getCurrentDate();
                    String comment = getStringFromEditText(et_costComment);
                    if (costValue != 0.0)
                    {
                        myExpenses = new Expenses.ExpensesBuilder()
                                .setCost(costValue)
                                .setCostType(myCostType)
                                .setComment(comment)
                                .setDate(currentDate)
                                .setIsRemoteData(false)
                                .build();
                    }
                    saveAndQuit();
                }
            }
        });
    }

    private void saveAndQuit()
    {
        Intent startActivityIntent = new Intent(ExpensesActivity.this, StartActivity.class);
        if (myExpenses != null)
        {
            startActivityIntent.putExtra("Expense", myExpenses);
            Log.v(TAG, "Expense saved: " + myExpenses.toString());
        }
        ExpensesActivity.this.startActivity(startActivityIntent);
    }

    private String getStringFromEditText(EditText editText)
    {
        return editText.getText().toString();
    }

    private double getDoubleFromEditText(EditText editText)
    {
        String costValue = editText.getText().toString();
        return Double.valueOf(costValue);
    }

    private void initGui()
    {
        //RadioButton
        cb_isExpense = (CheckBox) findViewById(R.id.cb_isExpense);

        //TextView
        tv_costText = (TextView) findViewById(R.id.tv_costText);
        tv_costComment = (TextView) findViewById(R.id.tv_costComment);

        //EditText
        et_costValue = (EditText) findViewById(R.id.et_costValue);
        et_costComment = (EditText) findViewById(R.id.et_costComment);

        //Spinner
        sp_expenseTypes = (Spinner) findViewById(R.id.sp_expenseTypes);
        //Button
        btn_saveExpenses = (Button) findViewById(R.id.btn_saveExpenses);
    }

    @Override
    public void onBackPressed()
    {
        Intent startActivityIntent = new Intent(ExpensesActivity.this, StartActivity.class);
        ExpensesActivity.this.startActivity(startActivityIntent);
        //TODO: Or use finish(); ?
    }

}
