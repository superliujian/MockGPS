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
     * ��ÿ��Activity��onCreate�е��ã�������¼���˵�activity 
     */  
    public static void addActivity(Activity act) {  
        sActivityList.add(act);  
    }  
      
    /*** 
     * ��ÿ��Activity��onDestroy�е��� 
     */  
    public static void removeActivity(Activity act) {  
        sActivityList.remove(act);  
    }  
      
    /*** 
     * �������е�activity�����رճ���Ľ��� 
     */  
    public static void exit() {  
        finishAll();  
        System.exit(0);  
    }  
      
    /*** 
     * �������е�activity��������رճ���Ľ��� 
     */  
    public static void finishAll() {  
        for (Activity act : sActivityList) {  
            act.finish();  
        }     
        sActivityList.clear();  
    }  
      
    private ActivityUtils() {} // no instance  
}  