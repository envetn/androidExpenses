package expenses;

/**
 * Created by olof on 2016-10-29.
 */
public class ExpenseUser
{
    private final String myUsername;
    private final String myPassword;


    public String getUsername()
    {
        return myUsername;
    }

    public String getPassword()
    {
        return myPassword;
    }

    public ExpenseUser(String userName, String password)
    {
        myUsername = userName;
        myPassword = password;
    }
}
