package net.superliujian.mockgps.service;

import net.superliujian.mockgps.MockGPSActivity;
import net.superliujian.mockgps.constants.Constants;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class StartMockGPSReceiver extends BroadcastReceiver {
	public static Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		String name = intent.getStringExtra(Constants.EXTRA_NAME);
		Double lat = intent.getDoubleExtra(Constants.EXTRA_LAT, 0);
		Double lng = intent.getDoubleExtra(Constants.EXTRA_LNG, 0);
		float speed = intent.getFloatExtra(Constants.EXTRA_SPEED, 0f);
		float bearing = intent.getFloatExtra(Constants.EXTRA_BEARING, 0f);
		float accuracy = intent.getFloatExtra(Constants.EXTRA_ACCURACY, 0f);
		
		MockGPSActivity.startTestGPS(context, name, lat, lng, speed, accuracy, bearing, false);

	}
}