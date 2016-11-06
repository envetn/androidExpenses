package network.Request;

import java.sql.Date;

/**
 * Created by olof on 2016-09-11.
 */
public class GetRequest implements BaseRequest
{
    private final String myId;
    private final Date myRequestDate;
    private final int myLowerLimit;
    private final int myUpperLimit;
    private final String myRequestType;
    private final String myOrderBy;
    private final String myFetchPeriod;
    private final boolean myIsAscending;

    private GetRequest(Builder builder)
    {
        myId = builder.myId;
        myRequestDate = builder.requestDate;
        myLowerLimit = builder.myLowerLimit;
        myUpperLimit = builder.myUpperLimit;
        myRequestType = builder.myRequestType;
        myOrderBy = builder.myOrderBy;
        myFetchPeriod = builder.myFetchPeriod;
        myIsAscending = builder.myIsAscending;
    }

    public boolean isAscending()
    {
        return myIsAscending;
    }
    public Date getRequestDate()
    {
        return myRequestDate;
    }

    public int getLowerLimit()
    {
        return myLowerLimit;
    }

    public int getUpperLimit()
    {
        return myUpperLimit;
    }

//    public String getRequestType()
//    {
//        return myRequestType;
//    }

    public String getOrderBy()
    {
        return myOrderBy;
    }

    public String getFetchPeriod()
    {
        return myFetchPeriod;
    }

    @Override
    public String getRequestId()
    {
        return myId;
    }

    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final String myRequestType = "GET";
        private Date requestDate;
        private String myId;
        private int myLowerLimit;
        private int myUpperLimit;
        private String myOrderBy;
        private String myFetchPeriod;
        private boolean myIsAscending;

        public Builder setDate(Date date)
        {
            requestDate = date;
            return this;
        }

        public Builder setFetchPeriod(String period)
        {
            myFetchPeriod = period;
            return this;
        }

        public Builder setId(String id)
        {
            myId = id;
            return this;
        }
        public Builder setLowerLimit(int lowerLimit)
        {
            myLowerLimit = lowerLimit;
            return this;
        }
        public Builder setUpperLimit(int upperLimit)
        {
            myUpperLimit = upperLimit;
            return this;
        }
        public Builder setOrderBY(String orderBy)
        {
            myOrderBy = orderBy;
            return this;
        }

        public Builder setIsAscending(boolean isAscending)
        {
            myIsAscending = isAscending;
            return this;
        }

        public GetRequest build()
        {
            return new GetRequest(this);
        }


    }
}
