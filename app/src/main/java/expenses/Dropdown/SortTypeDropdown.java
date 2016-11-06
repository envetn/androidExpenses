package expenses.Dropdown;

import android.content.res.Resources;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import activities.ListActivity;
import com.example.olof.myapplication2.R;

/**
 * Created by olof on 2016-08-16.
 */
public class SortTypeDropdown
{
    private final ListActivity myActivity;
    private String mySortType;

    public SortTypeDropdown(ListActivity activity)
    {

        myActivity = activity;
    }

    public void updateDropdown()
    {
        Spinner spinner = (Spinner) myActivity.findViewById(R.id.sp_expenseSortType);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(myActivity, R.layout.support_simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        Resources resource = myActivity.getResources();

        spinnerAdapter.add(resource.getString(R.string.Cost));
        spinnerAdapter.add(resource.getString(R.string.Type));
        spinnerAdapter.add(resource.getString(R.string.Date));
    }

    public String getSortType()
    {
        return mySortType;
    }

}
