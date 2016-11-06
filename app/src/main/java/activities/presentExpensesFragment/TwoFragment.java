package activities.presentExpensesFragment;

import activities.PresentExpensesActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.olof.myapplication2.R;
/**
 * Created by olof on 2016-09-10.
 */
public class TwoFragment extends Fragment
{

    public TwoFragment()
    {
        //DLÖÖÖÖ
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_two, container, false);
    }
}
