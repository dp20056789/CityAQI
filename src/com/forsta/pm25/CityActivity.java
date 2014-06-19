package com.forsta.pm25;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.forsta.database.CityInfoHelper;

public class CityActivity extends Activity {
	
	private ListView listView=null;;
	private TextView currentCity=null;
	private CityInfoHelper cityInfoHelper;
	public static final String PREFS_NAME = "myprefs"; 
	public static final String TAG="Debug";
	private SharedPreferences settings=null;
	private Context context;
	private String cityname;
	private SharedPreferences.Editor editor;
	private List<Map<String, String>> list=new ArrayList<Map<String, String>>();
	
	private TextView back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		listView=(ListView) findViewById(R.id.listview);
		currentCity=(TextView) findViewById(R.id.currentCity);
		
		settings=getSharedPreferences(PREFS_NAME, 1);
		editor=settings.edit();
		cityname=settings.getString("city", null);
		context = this.getApplicationContext();
		cityInfoHelper=new CityInfoHelper(context);
		
		back=(TextView) findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				new RefreshUI().execute();
				
			}
		}).start();
		ListAdapter adapter=new SimpleAdapter(CityActivity.this, getData(), R.layout.city, new String[]{"area"},new int[]{R.id.cityName} );
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			 @Override  
	            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
	                    long arg3) {  
	                Toast.makeText(context, "选择"+""+list.get(arg2).get("area"), Toast.LENGTH_LONG).show();
	                editor.putString("city", list.get(arg2).get("area"));
	                editor.commit();
	                new RefreshUI().execute();
	            }  
		});
	}
	
	private class RefreshUI extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			publishProgress();
			return null;
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			cityname=settings.getString("city", null);
			currentCity.setText(cityname);
			super.onProgressUpdate(values);
		}
		
	}
	
	//获取所有城市
	private List<Map<String, String>> getData(){
		
		SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
		Cursor cursor=db.query("city", null, null, null, null, null, null, null);
		while(cursor.moveToNext()){
			Map map=new HashMap<String, String>();
			map.put("area", cursor.getString(cursor.getColumnIndex("area")));
			list.add(map);
		}
		db.close();
		return list;
	}
}
