package network.Json;

import android.util.Log;
import expenses.Expenses;
import network.Request.BaseRequest;
import network.Request.GetRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import utils.Utils;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by olof on 2016-04-09.
 */
public class JsonHandler
{
    private static final String TAG = "JsonHandler";


    private JsonHandler()
    {
    }

    @Deprecated
    /**
     * Use {@link JsonHandler#createJsonHeaderNew}
     */
    private static JSONObject createJsonHeader(String requestType, Date time) throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("requestType", requestType);
        jsonObject.put("RequestId", "Expenses");
        jsonObject.put("date", time);

        return jsonObject;
    }

    private static JSONObject createJsonHeaderNew(String requestType, Date time) throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        JSONObject requestIdObject = new JSONObject().put("requestId", "Expenses");

        jsonObject.put("id", requestIdObject);
        jsonObject.put("requestType", requestType);
        jsonObject.put("requestDate", time);

        return jsonObject;
    }

    public static JSONObject createJsonPutRequest(Expenses expenseData) throws JSONException
    {
        JSONObject jsonObject = createJsonHeaderNew("Put", expenseData.getDate());

        JSONObject contentObject = new JSONObject();
        contentObject.put("cost", expenseData.getCost());
        contentObject.put("costType", expenseData.getCostType());
        contentObject.put("comment", expenseData.getComment());
        contentObject.put("uuid", expenseData.getUniqueId());

        jsonObject.put("content", contentObject);

        return jsonObject;
    }

    public static JSONObject createJsonGetRequest(int lowerLimit, int upperLimit, String orderBy) throws JSONException
    {
        JSONObject jsonObject = createJsonHeader("Get", Utils.getCurrentDate());

        jsonObject.put("lowerLimit", lowerLimit);
        jsonObject.put("upperLimit", upperLimit);
        jsonObject.put("orderBy", orderBy);

        return jsonObject;
    }

    public static JSONObject createJsonGetRequest(BaseRequest request) throws JSONException
    {
        GetRequest getRequest = (GetRequest) request;

        JSONObject jsonObject = createJsonHeaderNew("Get", Utils.getCurrentDate());

        LimitObject limit = new LimitObject();
        limit.setPeriodToFetch(getRequest.getFetchPeriod());
        limit.setLimit(getRequest.getLowerLimit(), getRequest.getUpperLimit());

        JSONObject order = new JSONObject();
        order.put("orderBy", getRequest.getOrderBy());
        order.put("isAscending", getRequest.isAscending());

        jsonObject.put("limit", limit.build());
        jsonObject.put("order", order);

        return jsonObject;
    }

    public static JSONObject createJsonRemoveRequest(List<String> itemsToRemove) throws JSONException
    {
        JSONObject jsonObject = createJsonHeaderNew("Delete", Utils.getCurrentDate());
        JSONArray jsonArray = new JSONArray();
        for (String item : itemsToRemove)
        {
            jsonArray.put(item);
        }
        jsonObject.put("remove-Data", jsonArray);
        return jsonObject;
    }

    public static List<Expenses> createExpensesObject(String response) throws JSONException, ParseException
    {
        JSONObject jsonObject = new JSONObject(response);

        JSONArray jsonArray = (JSONArray) jsonObject.get("Get-Data");
        List<Expenses> expenses = new ArrayList<>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject jsonobject = jsonArray.getJSONObject(i);
            Expenses expense = new Expenses.ExpensesBuilder()
                    .setCost(jsonobject.getDouble("cost"))
                    .setCostType(jsonobject.getString("costType"))
                    .setComment(jsonobject.getString("comment"))
                    .setIsOnlyLocal(0)
                    .setDate(Utils.getTimestamp(jsonobject.getString("buyDate")))
                    .setUniqueId(jsonobject.getString("uuid")) // only set when fetching
                    .setIsRemoteData(true)
                    .build();
            expenses.add(expense);
        }
        return expenses;
    }


    public static boolean isSuccessFul(String response)
    {
        boolean isSuccess = false;
        try
        {
            JSONObject jsonObject = new JSONObject(response);
            isSuccess = jsonObject.get("Response").equals("Success");
            Log.v(TAG, "Message response " + jsonObject.get("Response"));
        }
        catch (JSONException e)
        {
            Log.e(TAG, "NetworkTask failed:\nException in response message: " + e.toString());
        }

        return isSuccess;
    }
}
