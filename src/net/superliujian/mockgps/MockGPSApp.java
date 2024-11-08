package net.superliujian.mockgps;

import java.util.LinkedList;

import net.superliujian.mockgps.constants.Constants;
import net.superliujian.mockgps.model.Preferences;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKEvent;
import com.baidu.mapapi.MKGeneralListener;



public class MockGPSApp extends Application {
	private static final String LOG_TAG = "MockGPSApp";
	static MockGPSApp mMockGPSApp;
	//百度MapAPI的管理类
	BMapManager mBMapMan = null;
	// 授权Key
	// 申请地址：http://dev.baidu.com/wiki/static/imap/key/
	String mStrKey = "73510AE3F17637B443BEEDAD8A06377B70BCFDA1";
	boolean m_bKeyRight = true;	// 授权Key正确，验证通过
	public static LinkedList<Activity> sAllActivitys = new LinkedList<Activity>();  

	public static void finishAll() {  
        for(Activity activity : sAllActivitys) {  
            activity.finish();  
        }  
          
        sAllActivitys.clear();  
    }  
  
    public static void exit() {  
        finishAll();  
        System.exit(0);  
    }  
	
	
 // 常用事件监听，用来处理通常的网络错误，授权验证错误等
 	static class MyGeneralListener implements MKGeneralListener {
 		@Override
 		public void onGetNetworkState(int iError) {
 			Log.d("MyGeneralListener", "onGetNetworkState error is "+ iError);
 			Toast.makeText(MockGPSApp.mMockGPSApp.getApplicationContext(), "您的网络出错啦！",
 					Toast.LENGTH_LONG).show();
 		}

 		@Override
 		public void onGetPermissionState(int iError) {
 			Log.d("MyGeneralListener", "onGetPermissionState error is "+ iError);
 			if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
 				// 授权Key错误：
 				Toast.makeText(MockGPSApp.mMockGPSApp.getApplicationContext(), 
 						"请在BMapApiDemoApp.java文件输入正确的授权Key！",
 						Toast.LENGTH_LONG).show();
 				MockGPSApp.mMockGPSApp.m_bKeyRight = false;
 			}
 		}
 	}


 	@Override
 	//建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
 	public void onTerminate() {
 		// TODO Auto-generated method stub
 		if (mBMapMan != null) {
 			mBMapMan.destroy();
 			mBMapMan = null;
 		}
 		super.onTerminate();
 	}

	@Override
	public void onCreate() {
		
		Preferences.init(this);
		try {
			boolean exist = Preferences.getShortCut();
			final Intent shortCut = new Intent(this, SplashActivity.class);
			final ContentResolver cr = this.getContentResolver();
			cr
					.notifyChange(
							Uri
									.parse("content://com.android.launcher.settings/favorites?notify=true"),
							null);
			if (!exist) {
				String scName = getString(R.string.app_name);

				Intent addShortcut = new Intent(Constants.ACTION_ADD_SHORTCUT);

				shortCut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				shortCut.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				shortCut.addCategory(Intent.CATEGORY_LAUNCHER);
				shortCut.setAction(Intent.ACTION_MAIN);
				addShortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, scName);
				addShortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortCut);
				addShortcut.putExtra("duplicate", false);
				addShortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
						Intent.ShortcutIconResource.fromContext(
								this,
								R.drawable.icon));

				sendBroadcast(addShortcut);
				Preferences.putShortCut(true);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "" + e.getMessage());
		}
		
		
		Log.v("BMapApiDemoApp", "onCreate");
		mMockGPSApp = this;
		mBMapMan = new BMapManager(this);
		mBMapMan.init(this.mStrKey, new MyGeneralListener());
		mBMapMan.getLocationManager().setNotifyInternal(10, 5);
		super.onCreate();
	}

}
