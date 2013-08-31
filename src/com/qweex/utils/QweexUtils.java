/*
        DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                    Version 2, December 2004

 Copyright (C) 2013 Jon Petraglia <MrQweex@qweex.com>

 Everyone is permitted to copy and distribute verbatim or modified
 copies of this license document, and changing it is allowed as long
 as the name is changed.

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. You just DO WHAT THE FUCK YOU WANT TO.
 */
package com.qweex.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class QweexUtils
{
    public static String TAG()
    {
        StackTraceElement stacktrace = Thread.currentThread().getStackTrace()[3];
        return
                "NITRO:" +
                        stacktrace.getClassName().substring("com.qweex.callisto".length()+1) + ":" + stacktrace.getMethodName()
                        + "::" + stacktrace.getLineNumber();
    }

    //http://stackoverflow.com/a/9624844/1526210
    @TargetApi(4)
    public static boolean isTabletDevice(android.content.Context activityContext)
    {
        if(!androidAPIover(4))   //VERSION
            return false;   //If there is a tablet running 1.5.....GOD HELP YOU

        try {

        // Verifies if the Generalized Size of the device is XLARGE to be
        // considered a Tablet
        Configuration config = activityContext.getResources().getConfiguration();
            Log.i("screenlayout", Integer.parseInt(Configuration.class.getDeclaredField("SCREENLAYOUT_SIZE_LARGE").get(config).toString()) + "!!!");
        boolean xlarge =
                //activityContext.getResources().getConfiguration().screenLayout &
                Boolean.parseBoolean(config.getClass().getField("screenLayout").get(config).toString()) &
                getStaticInt(config,"SCREENLAYOUT_SIZE_MASK")
                        >=	 //Changed this from == to >= because my tablet was returning 8 instead of 4.
                Integer.parseInt(Configuration.class.getDeclaredField("SCREENLAYOUT_SIZE_LARGE").get(config).toString());
                getStaticInt(config, "SCREENLAYOUT_SIZE_MASK");


        // If XLarge, checks if the Generalized Density is at least MDPI (160dpi)
        if (xlarge)
        {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            Activity activity = (Activity) activityContext;
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            //This next block lets us get constants that are not available in lower APIs.
            // If they aren't available, it's safe to assume that the device is not a tablet.
            // If you have a tablet or TV running Android 1.5, what the fuck is wrong with you.
            int xhigh = -1, tv = -1;
            try {
                Field f = android.util.DisplayMetrics.class.getDeclaredField("DENSITY_XHIGH");
                xhigh = (Integer) f.get(null);
                f = android.util.DisplayMetrics.class.getDeclaredField("DENSITY_TV");
                xhigh = (Integer) f.get(null);
            }catch(Exception e){}

            // MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160, DENSITY_TV=213, DENSITY_XHIGH=320
            int densityDpi = Integer.parseInt(metrics.getClass().getDeclaredField("densityDpi").get(metrics).toString());
            if (
                    densityDpi == 240 || //DENSITY_HIGH
                    densityDpi == 160 || //DENSITY_MEDIUM
                    //densityDpi == android.util.DisplayMetrics.DENSITY_DEFAULT ||
                    //densityDpi == android.util.DisplayMetrics.DENSITY_HIGH ||
                    //densityDpi == android.util.DisplayMetrics.DENSITY_MEDIUM ||
                    densityDpi == tv ||
                    densityDpi == xhigh
                    )
            {
                return true;
            }
        }
        } catch(Exception e) {}
        return false;
    }

    public static boolean androidAPIover(int ver)
    {
        //if(android.os.Build.VERSION.SDK.equals("3"))
        try {
            return Build.VERSION.class.getDeclaredField("SDK_INT").getInt(null) >= ver;
            //return android.os.Build.VERSION.SDK_INT >= ver; //VERSION
        } catch(Exception nsfe) {
            return Integer.parseInt(android.os.Build.VERSION.SDK) > ver;
        }
    }

    public static void scrollToPosition(ListView v, int pos)
    {
        //v.smoothScrollToPosition(pos);
        try {
        Method m = v.getClass().getMethod("smoothScrollToPosition", new Class[] { int.class});
        m.invoke(v,pos);
        } catch(Exception e){}
    }

    public static void scrollByOffset(ListView v, int off)
    {
        //v.smoothScrollByOffset(pos);
        try {
            Method m = v.getClass().getMethod("smoothScrollByOffset", new Class[] { int.class});
            m.invoke(v,off);
        } catch(Exception e){}
    }

    public static Object getStaticVariable(Object obj, String str)
    {
        try {
        obj.getClass().getDeclaredField(str).get(obj);
        } catch(NoSuchFieldException e) {
        } catch(IllegalAccessException i) {
        }
        return null;
    }

    public static int getStaticInt(Object obj, String str)
    {
        return Integer.parseInt(getStaticVariable(obj,str).toString());
    }
}
