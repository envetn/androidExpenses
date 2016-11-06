package expenses.Dropdown;

import android.app.Activity;
import android.content.res.Resources;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.olof.myapplication2.R;

/**
 * Created by olof on 2016-07-28.
 */
public class CostTypeDropdown
{
    private final Activity myActivity;
    private String costText;
    private int myId;
    private boolean isAdd;

    public CostTypeDropdown(Activity activity, String type)
    {
        myActivity = activity;

        if (type.equals("add"))
        {
            myId = R.id.sp_expenseTypes;
            isAdd = true;
        }
        else
        {
            myId = R.id.sp_expensesShowType;
            isAdd = false;
        }
    }

    public void updateDropdown(boolean isExpense)
    {
        Spinner spinner = (Spinner) myActivity.findViewById(myId);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(myActivity, R.layout.support_simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        Resources resource = myActivity.getResources();

        if (!isExpense)
        {
            costText = resource.getString(R.string.Income);

            spinnerAdapter.add(resource.getString(R.string.Paycheck));
            spinnerAdapter.add(resource.getString(R.string.Debt));
        }
        else
        {
            if (!isAdd)
            {
                spinnerAdapter.add(resource.getString(R.string.All));
            }

            costText = resource.getString(R.string.Cost);

            spinnerAdapter.add(resource.getString(R.string.Food));
            spinnerAdapter.add(resource.getString(R.string.Food_out));
            spinnerAdapter.add(resource.getString(R.string.Bills));
            spinnerAdapter.add(resource.getString(R.string.Travel));
            spinnerAdapter.add(resource.getString(R.string.Other));

        }

        spinnerAdapter.notifyDataSetChanged();
    }

    public String getCostText()
    {
        return costText;
    }
}
