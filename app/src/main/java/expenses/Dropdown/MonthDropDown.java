package expenses.Dropdown;

import activities.ListActivity;
import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.example.olof.myapplication2.R;
import utils.Utils;

/**
 * Created by olof on 2016-10-09.
 */
public class MonthDropDown
{
    private final View myView;
    public MonthDropDown(View activity)
    {
        myView = activity;
    }

    public void updateDropdown()
    {
        String currentMonth = Utils.getCurrentMonth();
        Spinner spinner = (Spinner) myView.findViewById(R.id.sp_SelectMonth);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(myView.getContext(), R.layout.support_simple_spinner_dropdown_item);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        Resources resource = myView.getResources();

        spinnerAdapter.add(resource.getString(R.string.Jan));
        spinnerAdapter.add(resource.getString(R.string.Feb));
        spinnerAdapter.add(resource.getString(R.string.Mar));
        spinnerAdapter.add(resource.getString(R.string.Apr));
        spinnerAdapter.add(resource.getString(R.string.May));
        spinnerAdapter.add(resource.getString(R.string.Jun));
        spinnerAdapter.add(resource.getString(R.string.Jul));
        spinnerAdapter.add(resource.getString(R.string.Aug));
        spinnerAdapter.add(resource.getString(R.string.Sep));
        spinnerAdapter.add(resource.getString(R.string.Oct));
        spinnerAdapter.add(resource.getString(R.string.Nov));
        spinnerAdapter.add(resource.getString(R.string.Dec));

        int currentMonthPosition = spinnerAdapter.getPosition(currentMonth);
        spinner.setSelection(currentMonthPosition);

        spinnerAdapter.notifyDataSetChanged();
    }
}
