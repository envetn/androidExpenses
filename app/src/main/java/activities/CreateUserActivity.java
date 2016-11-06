package activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.olof.myapplication2.R;
import database.DatabaseConnection;
import database.DatabaseHandler;

import static utils.Utils.createToast;

/**
 * Created by olof on 2016-10-29.
 */
public class CreateUserActivity extends Activity
{
    private Button btn_create;
    private EditText et_username;
    private EditText et_password;
    private DatabaseHandler myDatabaseHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_user_layout);
        myDatabaseHandler = new DatabaseHandler(getApplicationContext());

        initGui();
        addListner();
    }

    private void addListner()
    {
        btn_create.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String username = et_username.getText().toString();
                String password = et_password.getText().toString();
                if (username.isEmpty() || password.isEmpty())
                {
                    createToast(getApplicationContext(), "You need to populate the fields!");
                }
                else
                {
                    myDatabaseHandler.requestToCreateUser(username, password);
                    finish();
                }
            }
        });
    }

    private void initGui()
    {
        btn_create = (Button) findViewById(R.id.btn_createUser);
        et_username = (EditText) findViewById(R.id.et_userName);
        et_password = (EditText) findViewById(R.id.et_password);
    }
}
