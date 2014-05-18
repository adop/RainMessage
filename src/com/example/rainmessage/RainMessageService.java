package com.example.rainmessage;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.rainmessage.helper.NoWeatherResultException;
import com.example.rainmessage.helper.SinaWeatherXmlParser;

public class RainMessageService extends IntentService {
	private static final String TAG = "RainMessageService";
	private static Timer timer = new Timer();
	private String phoneN;
	private String msg;
	private String city;
	private int minute;
	private int hour;
	private MainTask mt;
	private Intent thisIntent;
	private static boolean isStopped = false;
	//
	// @Override
	// public void onCreate() {
	//
	//
	// super.onCreate();
	// }

	/**
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public RainMessageService() {

		super("RainMessageService");
		// android.os.Debug.waitForDebugger();
	}

	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns,
	 * IntentService stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		thisIntent = intent;
		 //android.os.Debug.waitForDebugger();
		Bundle extras = intent.getExtras();
		if (extras == null)
			Log.d("Service", "null");
		else {
			Log.d("Service", "not null");
			minute = (Integer) extras.get("Min");
			hour = (Integer) extras.get("Hour");
			long interval = 1000 * 60 * 60 * 24;
			long plan = 1000 * 60 * (minute + 60 * hour);
			Calendar c = Calendar.getInstance();
			long current = 1000 * 60 * (60 * c.get(Calendar.HOUR_OF_DAY) + c
					.get(Calendar.MINUTE));
			mt = new MainTask();
			if(isStopped) return;
			if (plan >= current) {
				timer.scheduleAtFixedRate(mt, (plan - current), interval);
			} else {
				timer.scheduleAtFixedRate(mt, (plan + interval - current),
						interval);
			}
			while(!isStopped){
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// timer.scheduleAtFixedRate(new mainTask(), 0, interval);
		}

	}

	private boolean checkRain() {
		Bundle extras = thisIntent.getExtras();
		phoneN = (String) extras.get("PhoneNumber");
		msg = (String) extras.get("Message");
		
		city = (String) extras.get("City");
		boolean rain = false;
		try {
			rain = isCityRainyTomorrow(city);
		} catch (NoWeatherResultException e) {

			Log.d("Null result", "没有气象预报");
		}
		return rain;
	}

	private boolean isCityRainyTomorrow(String targetCity)
			throws NoWeatherResultException {

		return SinaWeatherXmlParser.isRain(targetCity);

	}

	private class MainTask extends TimerTask {

		public void run() {
			if (checkRain()) {
				sendSMS(phoneN, msg);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "短信服务启动", Toast.LENGTH_SHORT).show();
		return super.onStartCommand(intent, flags, startId);
	}

	// ---sends an SMS message to target phone---
	@SuppressLint("NewApi")
	private void sendSMS(final String phoneNumber, final String message) {
		String SENT = "SMS_SENT";
		// String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);
		//
		// PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
		// new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					ContentValues values = new ContentValues();

					values.put("address", phoneNumber);
					values.put("body", message);

					getContentResolver().insert(
							Uri.parse("content://sms/sent"), values);
					Toast.makeText(getBaseContext(), "短信已发送",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Toast.makeText(getBaseContext(), "发送失败", Toast.LENGTH_SHORT)
							.show();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Toast.makeText(getBaseContext(), "没有服务", Toast.LENGTH_SHORT)
							.show();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Toast.makeText(getBaseContext(), "协议数据单元为空",
							Toast.LENGTH_SHORT).show();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Toast.makeText(getBaseContext(), "广播关闭状态",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		// registerReceiver(new BroadcastReceiver() {
		// @Override
		// public void onReceive(Context arg0, Intent arg1) {
		// switch (getResultCode()) {
		// case Activity.RESULT_OK:
		// Toast.makeText(getBaseContext(), "SMS delivered",
		// Toast.LENGTH_SHORT).show();
		// break;
		// case Activity.RESULT_CANCELED:
		// Toast.makeText(getBaseContext(), "SMS not delivered",
		// Toast.LENGTH_SHORT).show();
		// break;
		// }
		// }
		// }, new IntentFilter(DELIVERED));

		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
		// sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

	}

	@Override
	public void onDestroy() {
		Log.d("onDestroy", "Service onDestroy Called");
		super.onDestroy();

		mt.cancel();
		timer.cancel();
		isStopped=true;
	}

	// public boolean restoreSms(Sms obj) {
	// boolean ret = false;
	// try {
	// ContentValues values = new ContentValues();
	// values.put("address", obj.getAddress());
	// values.put("body", obj.getMsg());
	// values.put("read", obj.getReadState());
	// values.put("date", obj.getTime());
	// getApplicationContext().getContentResolver().insert(
	//
	// Uri.parse("content://sms/sent"), values);
	// //Uri.parse("content://sms/inbox", values);
	// ret = true;
	// } catch (Exception ex) {
	// ret = false;
	// }
	// return ret;
	// }
}
