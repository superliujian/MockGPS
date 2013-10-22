package net.superliujian.mockgps.utils;

import java.util.LinkedList;

import android.app.Activity;


public class ActivityUtils {  
    private static final LinkedList<Activity> sActivityList = new LinkedList<Activity>();  
      
    private static boolean sProcessKilled = true;  
      
    public static boolean isProcessKilled() {  
        return sProcessKilled;  
    }  
  
    public static void setProcessStarted() {  
        sProcessKilled = false;  
    }  
      
    /*** 
     * 在每个Activity的onCreate中调用，用来记录打开了的activity 
     */  
    public static void addActivity(Activity act) {  
        sActivityList.add(act);  
    }  
      
    /*** 
     * 在每个Activity的onDestroy中调用 
     */  
    public static void removeActivity(Activity act) {  
        sActivityList.remove(act);  
    }  
      
    /*** 
     * 结束所有的activity，并关闭程序的进程 
     */  
    public static void exit() {  
        finishAll();  
        System.exit(0);  
    }  
      
    /*** 
     * 结束所有的activity，但不会关闭程序的进程 
     */  
    public static void finishAll() {  
        for (Activity act : sActivityList) {  
            act.finish();  
        }     
        sActivityList.clear();  
    }  
      
    private ActivityUtils() {} // no instance  
}  