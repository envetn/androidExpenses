package expenses;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.example.olof.myapplication2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom ArrayAdapter for ListView.
 * <p>
 * Converts Expenses Object into {@link View} using custom layout (list_row_layout.xml)
 * <p>
 * Created by olof on 2016-04-08.
 */
public class ExpensesAdapter extends BaseAdapter
{
    private static LayoutInflater myLayoutInflater;
    private final static String TAG = "ExpenseAdapter";
    private String myShowType;
    private Context myContext;
    private List<Expenses> myData;
    private List<String> idsToRemove = new ArrayList<>();

    public ExpensesAdapter(Context context, List<Expenses> data, String sortType, String showType)
    {
        myContext = context;
        myShowType = showType;
        myData = filterData(data);

        sortAccordingTo(sortType);
        myLayoutInflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void appendDataAndNotifyChange(List<Expenses> expenses)
    {
        myData.addAll(expenses);
        this.notifyDataSetChanged();
    }

    private List<Expenses> filterData(List<Expenses> data)
    {

        if (myShowType.equals("Alla")) // TODO: inglisch?, from R.String....
        {
            return data;
        }

        List<Expenses> filteredData = new ArrayList<>();
        for (Expenses expense : data)
        {
            if (expense.getCostType().equals(myShowType))
            {
                filteredData.add(expense);
            }
        }
        return filteredData;
    }

    @Override
    public int getCount()
    {
        return myData.size();
    }

    @Override
    public Object getItem(int position)
    {

        Expenses selectedData = myData.get(position);

        Log.d(TAG, "GetItem: " + selectedData.toString());
        return myData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        Log.d(TAG, "getItemId: " + position);
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder = new ViewHolder(position);

        View myView = myLayoutInflater.inflate(R.layout.list_row_layout, null);

        viewHolder.tv_cost = (TextView) myView.findViewById(R.id.tv_lv_cost);
//        viewHolder.tv_costType = (TextView) myView.findViewById(R.id.tv_lv_costType);
        viewHolder.tv_costDate = (TextView) myView.findViewById(R.id.tv_lv_date);
        viewHolder.tv_comment = (TextView) myView.findViewById(R.id.tv_lv_comment);
        viewHolder.cb_remove = (CheckBox) myView.findViewById(R.id.cb_remove);

        Expenses expense = myData.get(position);
        viewHolder.tv_cost.append(" :" + expense.getCost());
        viewHolder.tv_costDate.append(" :" + expense.getDate().toString());
//        viewHolder.tv_costType.append(" :" + expense.getCostType());
        viewHolder.tv_comment.append(" :" + expense.getComment());


        int backgroundColor = expense.getIsRemoteData() ? Color.DKGRAY : Color.LTGRAY; // colors...

        myView.setBackgroundColor(backgroundColor);

        myView.setTag(viewHolder);
        setListenerViewHolder(viewHolder);

        return myView;
    }

    public List<String> getItemsToRemove()
    {
        return idsToRemove;
    }

    private void sortAccordingTo(final String sortType)
    {
        for (int i = 0; i < myData.size(); i++)
        {
            for (int j = 0; j < myData.size(); j++)
            {
                if (sortType.equals("Kostnad")) // english
                {
                    if (myData.get(i).getCost() > myData.get(j).getCost())
                    {
                        swap(i, j);
                    }
                }
                else if (sortType.equals("Typ")) // english
                {
                    int sort = myData.get(i).getCostType().compareTo(myData.get(j).getCostType());
                    if (sort == 1) // left is bigger
                    {
                        swap(i, j);
                    }
                }
                else
                {
                    int sort = myData.get(i).getDate().compareTo(myData.get(j).getDate());
                    if (sort == 1) // left is bigger
                    {
                        swap(i, j);
                    }
                }

            }
        }
    }

    private void swap(int placeA, int placeB)
    {
        Expenses tmp = myData.get(placeA);
        myData.set(placeA, myData.get(placeB));
        myData.set(placeB, tmp);
    }

    private void setListenerViewHolder(final ViewHolder viewHolder)
    {
        viewHolder.cb_remove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (viewHolder.cb_remove.isChecked())
                {
                    addIdToRemove(viewHolder.myPosition);
                }
            }
        });
    }

    private void addIdToRemove(int position)
    {
        Expenses expense = myData.get(position);
        idsToRemove.add(expense.getUniqueId());
    }


    /**
     * Each item in the list
     */
    private class ViewHolder
    {
        private TextView tv_cost, tv_costType, tv_costDate, tv_comment;
        private CheckBox cb_remove;
        private final int myPosition;

        private ViewHolder(int position)
        {
            myPosition = position;
        }
    }
}
