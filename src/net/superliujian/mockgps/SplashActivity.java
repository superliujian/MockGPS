package net.superliujian.mockgps;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import net.superliujian.mockgps.model.Preferences;
import net.superliujian.mockgps.utils.ActivityUtils;
import net.superliujian.mockgps.utils.HttpHelper;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;





public class SplashActivity extends Activity{
	
	boolean noAuth=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_splash);
		
		
		String uuid=this.getUniqueId();
		String regCode=Preferences.getRegKey();
		String name=Preferences.getRegName();
		
		EditText tv_name=(EditText)this.findViewById(R.id.et_name);
		tv_name.setText(name);
		
		EditText tv_uuid=(EditText)this.findViewById(R.id.et_uuid);
		tv_uuid.setText(uuid);
		tv_uuid.setEnabled(false);
		
		EditText tv_reg_code=(EditText)this.findViewById(R.id.et_reg_code);
		tv_reg_code.setText(regCode);
		
		
		Button btn_login=(Button)this.findViewById(R.id.splash_btn_login);
		btn_login.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name=((EditText)findViewById(R.id.et_name)).getText().toString().trim();
				String regCode=((EditText)findViewById(R.id.et_reg_code)).getText().toString().trim();
				String uuid=((EditText)findViewById(R.id.et_uuid)).getText().toString().trim();
				if(authentication(name,uuid,regCode)){
					Preferences.putRegKey(regCode);
					Preferences.putRegName(name);
					Intent intent=new Intent(SplashActivity.this,MockGPSActivity.class);
					SplashActivity.this.startActivity(intent);
					finish();
				}else{
					Toast.makeText(SplashActivity.this, "登陆失败", 0).show();
				}
			}
		});
		Button btn_exit=(Button)this.findViewById(R.id.splash_btn_exit);
		btn_exit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ActivityUtils.exit();
			}
		});
		ActivityUtils.addActivity(this);
		if(noAuth){
			Intent intent=new Intent(SplashActivity.this,MockGPSActivity.class);
			SplashActivity.this.startActivity(intent);
			finish();
		}else{
			if(regCode!=""&name!=""&&authentication(name,uuid,regCode))
			{
				Intent intent=new Intent(SplashActivity.this,MockGPSActivity.class);
				SplashActivity.this.startActivity(intent);
				finish();
			}
		}
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub

		/*
		try {
			Date   curDate   =   new   Date(System.currentTimeMillis());//��ȡ��ǰʱ��   
			Log.v("Date", curDate.toLocaleString());
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss"); 
			Date expiresDate = df.parse("2012/10/20 12:00:00");
			Log.v("Date", expiresDate.toLocaleString());
			if(curDate.compareTo(expiresDate)>0){
				Log.v("Date", "exit");
				ActivityUtils.exit();
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		super.onStart();
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		ActivityUtils.removeActivity(this);
		super.onDestroy();
	}
	
	public boolean authentication(String name,String uuid,String regCode){
		String url=this.getResources().getString(R.string.validation_url);
		//url=url+"/uuid/"+uuid+"/regkey/"+regCode;
		url="http://mockgps.weiboyueta.com/Auth.aspx?name="+name+"&uuid="+uuid+"&key="+regCode;
		String error="";
		boolean success=false;
		HttpHelper helper=new HttpHelper();
		try{
		
			String rsp=helper.performGet(url);
			String jsonString = rsp.substring(rsp.indexOf("{"),rsp.lastIndexOf("}")+1);
			JSONObject obj=new JSONObject(jsonString);
			success=obj.getBoolean("success");
			error=obj.getString("error");
		}catch(Exception e){
			success=false;
			error=e.getMessage();
		}
		if(!success)
		{
			Toast.makeText(this, error, 0).show();
		}
		return success;
		
	}
	public  String getUniqueId(){
		final TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() <<32) | tmSerial.hashCode());
		String uniqueId = deviceUuid.toString().toUpperCase();
		uniqueId=uniqueId.substring(5, 7)+uniqueId.substring(9, 11)+uniqueId.substring(30, 36);
		return uniqueId;

	}
	
}