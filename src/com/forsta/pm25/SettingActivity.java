package com.forsta.pm25;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class SettingActivity extends Activity implements OnClickListener{
	private LinearLayout help;
	private LinearLayout about;
	private LinearLayout exit;
	private LinearLayout advice;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		help=(LinearLayout) findViewById(R.id.helplayout);
		about=(LinearLayout) findViewById(R.id.aboutlayout);
		exit=(LinearLayout) findViewById(R.id.exitayout);
		advice=(LinearLayout) findViewById(R.id.advicelayout);
		help.setOnClickListener(this);
		about.setOnClickListener(this);
		exit.setOnClickListener(this);
		advice.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		Intent intent = null;
		int viewid=v.getId();
		switch (viewid) {
		case R.id.advicelayout:
			intent=new Intent(getApplicationContext(), AdviceActivity.class);
			break;
		case R.id.helplayout:
			intent=new Intent(getApplicationContext(), HelpActivity.class);
			break;
		case R.id.aboutlayout:
			intent=new Intent(getApplicationContext(), AboutActivity.class);
			break;
		case R.id.exitayout:
			
			finish();
			break;
		default:
			break;
		}
		startActivity(intent);
	}
}
