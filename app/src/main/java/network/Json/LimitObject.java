package network.Json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by olof on 2016-09-17.
 */
public class LimitObject
{
    private String periodToFetch;
    private boolean hasPeriod = false;
    private int lowerLimit;
    private int upperLimit;

    public LimitObject()
    {
    }

    public void setPeriodToFetch(String periodToFetch)
    {
        this.periodToFetch = periodToFetch;
    }

    public void setLimit(int lower, int upper)
    {
        this.lowerLimit = lower;
        this.upperLimit = upper;
    }

    public JSONObject build() throws JSONException
    {
        JSONObject limitObject = new JSONObject();

        JSONObject periodObject = new JSONObject();
        periodObject.put("periodToFetch", periodToFetch);
        if(periodToFetch != null)
        {
            hasPeriod = true;
        }
        periodObject.put("hasPeriod", hasPeriod); //true false


        JSONObject fetchEntityNumber = new JSONObject();
        fetchEntityNumber.put("lowerLimit", lowerLimit);
        fetchEntityNumber.put("upperLimit", upperLimit);

        limitObject.put("fetchperiod", periodObject);
        limitObject.put("requestLimit", fetchEntityNumber);

        return limitObject;

    }

}
