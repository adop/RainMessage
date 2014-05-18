package com.example.rainmessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class RainMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences prefs = context.getSharedPreferences(
				"RainMessageStored", Context.MODE_PRIVATE);
		String phoneN = prefs.getString("phoneN", "");
		String msg = prefs.getString("msg", "");
		String city = prefs.getString("city", "");
		int mHour = prefs.getInt("mHour", 0);
		int mMinute = prefs.getInt("mMinute", 0);
		boolean needStartService = prefs.getBoolean("serviceStarted", false);
		if (needStartService == false)
			return;
		Intent smsIntent = new Intent(context, RainMessageService.class);
		smsIntent.putExtra("PhoneNumber", phoneN);
		smsIntent.putExtra("Message", msg);
		smsIntent.putExtra("Hour", mHour);
		smsIntent.putExtra("Min", mMinute);
		smsIntent.putExtra("City", city);

		context.startService(smsIntent);
	}
}
