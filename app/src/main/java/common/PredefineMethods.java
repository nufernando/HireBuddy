package common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.hirebuddy_application.MainActivity;
import com.example.user.hirebuddy_application.ProfileActivity;
import com.example.user.hirebuddy_application.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.support.v4.content.ContextCompat.startActivity;

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

    public static void viewToast(Context activity, String displayMessage){
        Toast.makeText(activity,displayMessage,Toast.LENGTH_SHORT).show();
    }


}

