package com.example.rainmessage;

import java.io.File;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

	Button saveConfig;
	ToggleButton btnStartSMS;
	private EditText txtPhoneNo;
	private EditText txtMessage;
	private EditText txtTime;
	private EditText txtCity;
	private String phoneN;
	private String msg;

	private String city;
	private Intent smsIntent;
	private TimePicker my_timePicker;
	private int mHour;
	private int mMinute;
	private boolean serviceStarted;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d("onCreate","onCreate Called");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 SharedPreferences prefs = this.getSharedPreferences("RainMessageStored", MODE_PRIVATE);
		 	phoneN=prefs.getString("phoneN", "");
		    msg=prefs.getString("msg","");
		    city=prefs.getString("city", "");
		    mHour=prefs.getInt("mHour", 0);
		    mMinute= prefs.getInt("mMinute", 0);
		    serviceStarted=prefs.getBoolean("serviceStarted", false);
		
		Calendar c = Calendar.getInstance();
		if(mHour==0 && mMinute==0 && serviceStarted==false){
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
		}
		my_timePicker = (TimePicker) findViewById(R.id.my_TimePicker);
		my_timePicker.setIs24HourView(true);
		my_timePicker.setCurrentHour(mHour);
		my_timePicker.setCurrentMinute(mMinute);
		my_timePicker
				.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
					@Override
					public void onTimeChanged(TimePicker view, int hourOfDay,
							int minute) {

						mHour=hourOfDay;
						mMinute=minute;
						
						
					}
				});
		saveConfig = (Button) findViewById(R.id.saveConfig);
		btnStartSMS = (ToggleButton) findViewById(R.id.toggleBtnSendSMS);
		btnStartSMS.setChecked(serviceStarted);
		txtPhoneNo = (EditText) findViewById(R.id.txtPhoneNo);
		txtPhoneNo.setText(phoneN);
		txtMessage = (EditText) findViewById(R.id.txtMessage);
		txtMessage.setText(msg);
		txtCity = (EditText) findViewById(R.id.txtCity);
		txtCity.setText(city);
		if (txtMessage.getText() == null
				|| txtMessage.getText().toString().equals(""))
			txtMessage.setText(" 明天下雨，要记得带伞", TextView.BufferType.EDITABLE);

		btnStartSMS
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// The toggle is enabled
						} else {
							// The toggle is disabled
						}
					}
				});
		saveConfig.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveContent();
			}


		});
	}
	
	private boolean saveContent() {
		String phoneNo = txtPhoneNo.getText().toString();
		String message = txtMessage.getText().toString();
//		String hour = String.valueOf(mHour);
//		String min = String.valueOf(mMinute);
		String ct = txtCity.getText().toString();
		if (phoneNo.length() > 0 && message.length() > 0
				 && ct.length() > 0) {
			phoneN = phoneNo;
			msg = message;

			city = ct;
			Toast.makeText(getBaseContext(), "已保存",
					Toast.LENGTH_SHORT).show();
			return true;
		} else {
			Toast.makeText(getBaseContext(), "请输入完整信息",
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			
			if(!saveContent()){
				 ((ToggleButton) view).setChecked(false);
				return;
			}
			// Enable vibrate
			startRainMessageService();
		} else {
			// Disable vibrate
			stopRainMessageService();
		}
	}

	private void startRainMessageService() {
		if (smsIntent != null) {
			stopService(smsIntent);
		}
		
		smsIntent = new Intent(this, RainMessageService.class);
		smsIntent.putExtra("PhoneNumber", phoneN);
		smsIntent.putExtra("Message", msg);
		smsIntent.putExtra("Hour", mHour);
		smsIntent.putExtra("Min", mMinute);
		smsIntent.putExtra("City", city);
		startService(smsIntent);
		serviceStarted=true;
	}

	private void stopRainMessageService() {
		if (smsIntent != null) {
			stopService(smsIntent);
		}else{
			stopService(new Intent(this, RainMessageService.class));
		}
		serviceStarted=false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public void onResume(){
		Log.d("onResume","onResume Called");
		super.onResume();
	}
	@Override
	public void onRestart(){
		Log.d("onRestart","onRestart Called");
		super.onRestart();
	}
	
	@Override
	public void onPause() {
		Log.d("onPause","onPause Called");
	    super.onPause();
	    
	    SharedPreferences prefs = this.getSharedPreferences("RainMessageStored", MODE_PRIVATE);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.clear();
	    editor.putString("phoneN", phoneN.trim());
	    editor.putString("msg", msg.trim());
	    editor.putString("city", city.trim());
	    editor.putInt("mHour", mHour);
	    editor.putInt("mMinute", mMinute);
	    editor.putBoolean("serviceStarted", serviceStarted);
	    editor.commit();
	}
	
	@Override
	public void onStop(){
		Log.d("onStop","onStop Called");
		super.onStop();
	}
	
	@Override
	public void onDestroy(){
		Log.d("onDestroy","onDestroy Called");
		super.onDestroy();
	}
	
	@Override
	public void onStart(){
		Log.d("onStart","onStart Called");
		super.onStart();
	}
	
	@Override
	public void onBackPressed() {
	    moveTaskToBack(true);
	}
	
	
	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Toast.makeText(this,
		// String.valueOf(getListView().getCheckedItemCount()),
		// Toast.LENGTH_LONG).show();
		// fileList.add(item.toString());
		// for(String i:fileList){
		// System.out.println(i);
		// }

		switch (item.getItemId()) {

			
			case R.id.action_exit:
//				Editor editor =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//				editor.clear();
//				editor.commit();
				serviceStarted=false;
				finish();
		default:

		}
		return super.onOptionsItemSelected(item);
	}
}
