package common;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PredefineMethods {
    public static final int NIGHT_HOUR = 18;
    public static final int MORNING_HOUR = 06;
    public static Boolean mapEnable = false;

    public PredefineMethods(Boolean mapEnable){
            this.mapEnable = mapEnable;
    }
    public static Integer getCurrentSystemHour(){
        int hour;
        //Get Current System Time
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateformat = new SimpleDateFormat("HH");
        hour = Integer.parseInt(dateformat.format(calendar.getTime()));

        return hour;
    }


}

