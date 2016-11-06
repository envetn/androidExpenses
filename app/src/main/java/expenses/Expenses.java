package expenses;

import java.io.Serializable;
import java.sql.Date;
import java.util.UUID;

/**
 * Created by olof on 2016-04-04.
 */
public class Expenses implements Serializable
{

    private final Double cost;
    private final String costType;
    private final String comment;
    private final Date date;
    private final String id;

    private boolean isOnlyLocal;
    private final boolean myIsRemoteData;

    private Expenses(ExpensesBuilder builder)
    {
        cost = builder.myCost;
        costType = builder.myCostType;
        comment = builder.myComment;
        date = builder.myDate;
        id = builder.myId;
        myIsRemoteData = builder.myIsRemoteData;
        isOnlyLocal = builder.isOnlyLocal;
    }

//    public void setIsOnlyLocal(boolean isLocal)
//    {
//        isOnlyLocal = isLocal;
//    }

    public boolean getIsOnlyLocal()
    {
        return isOnlyLocal;
    }

    public double getCost()
    {
        return cost;
    }

    public String getCostType()
    {
        return costType;
    }

    public String getComment()
    {
        return comment;
    }

    public Date getDate()
    {
        return date;
    }

    public String getUniqueId()
    {
        return id;
    }

    public boolean getIsRemoteData()
    {
        return myIsRemoteData;
    }

    public static class ExpensesBuilder

    {
        private Double myCost;
        private String myCostType;
        private Date myDate;
        private String myComment;
        private String myId;
        private boolean isOnlyLocal = true; // TODO: remove?
        private boolean myIsRemoteData;

        public ExpensesBuilder()
        {
            String uuid = UUID.randomUUID().toString();
            myId = uuid.replaceAll("-", "");

        }

        public ExpensesBuilder setCost(Double cost)
        {
            myCost = cost;
            return this;
        }

        public ExpensesBuilder setCostType(String type)
        {
            myCostType = type;
            return this;
        }

        public ExpensesBuilder setDate(Date date)
        {
            myDate = date;
            return this;
        }

        public ExpensesBuilder setComment(String comment)
        {
            myComment = comment;
            return this;
        }

        public ExpensesBuilder setUniqueId(String id)
        {
            myId = id;
            return this;
        }

        public ExpensesBuilder setIsOnlyLocal(int isLocal)
        {
            isOnlyLocal = isLocal == 1;
            return this;
        }

        public ExpensesBuilder setIsRemoteData(boolean isRemoteData)
        {
            myIsRemoteData = isRemoteData;
            return this;
        }

        public Expenses build()
        {
            return new Expenses(this);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Expenses expenses = (Expenses) o;

        if (isOnlyLocal != expenses.isOnlyLocal)
        {
            return false;
        }
        if (cost != null ? !cost.equals(expenses.cost) : expenses.cost != null)
        {
            return false;
        }
        if (costType != null ? !costType.equals(expenses.costType) : expenses.costType != null)
        {
            return false;
        }
        if (comment != null ? !comment.equals(expenses.comment) : expenses.comment != null)
        {
            return false;
        }
        return date != null ? date.equals(expenses.date) : expenses.date == null;

    }

    @Override
    public int hashCode()
    {
        int result = cost != null ? cost.hashCode() : 0;
        result = 31 * result + (costType != null ? costType.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (isOnlyLocal ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ Id: " + id);
        sb.append(", cost: " + cost);
        sb.append(", CostType: " + costType);
        sb.append(", Date: " + date);
        sb.append(", comment: " + comment);
        sb.append(" ]");

        return sb.toString();
    }
}
