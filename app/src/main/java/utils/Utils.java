package utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.widget.Toast;
import expenses.Expenses;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by olof on 2016-05-06.
 */
public final class Utils
{
    private Utils()
    {
    }

    public static void createToast(Context context, String message)
    {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static Date getTimestamp(String date) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date utilDate = format.parse(date);

        Date sqlDate = new java.sql.Date(utilDate.getTime());
        return sqlDate;
    }

    public static Date getCurrentDate()
    {
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = Date.valueOf(dateFormat.format(calender.getTime()));

        return currentDate;
    }

    public static String getCurrentMonth()
    {
        Calendar calender = Calendar.getInstance();
        String month = calender.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        if(month.equalsIgnoreCase("Oct"))
        {
            month = "Okt"; //TODO: fix to swedish
        }

        return month;
    }
    public static Map<String, Integer> getApplicationDimensions(Activity activity)
    {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        size.x = (size.x - (size.x/9));

        Map<String, Integer> dimensions = new HashMap<>();
        dimensions.put("width", size.x);
        dimensions.put("height", size.y);
        return dimensions;
    }

    public static Map<String, Date> getFirstDayOf(String givenMonth)
    {
        DateFormatSymbols dfs = new DateFormatSymbols();

        String[] months = dfs.getShortMonths();

        for(int i=0; i<months.length; i++)
        {
            String month = months[i].replace(".", "");
            if(month.equalsIgnoreCase(givenMonth))
            {
                return stuff(i);
            }
        }

        return null;
    }

    private static Map<String, Date> stuff(int month)
    {
        Map<String, Date> minMax = new HashMap<>();

        int[] daysInAMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        int days = daysInAMonth[month];

        GregorianCalendar calendarLastDay = new GregorianCalendar(2016, month, days);
        GregorianCalendar calendarFirstDay = new GregorianCalendar(2016, month, 1);
        java.util.Date monthEndDate = new java.util.Date(calendarLastDay.getTime().getTime());
        java.util.Date monthStartDay = new java.util.Date(calendarFirstDay.getTime().getTime());
        minMax.put("first", new Date(monthStartDay.getTime()));
        minMax.put("last", new Date(monthEndDate.getTime()));
        return minMax;
    }

    public static List<Expenses> filterExpensesTo(List<Expenses> myExpenses, String selectedMonth)
    {

        List<Expenses> filtereredList = new ArrayList<>();

        Map<String, Date> firstLastDate = getFirstDayOf(selectedMonth);
        Long firstDay = firstLastDate.get("first").getTime();
        Long lastDay = firstLastDate.get("last").getTime();

        for(Expenses expense : myExpenses)
        {
            Long time = expense.getDate().getTime();
            if(time >= firstDay && time <= lastDay)
            {
                filtereredList.add(expense);
            }
//            else
//            {
//                myExpenses.remove(expense);
//            }
        }
        return filtereredList;
    }

}
