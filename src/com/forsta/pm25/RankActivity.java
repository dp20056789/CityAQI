package com.forsta.pm25;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.forsta.database.CityInfoHelper;
import com.forsta.model.RankInfo;
import com.forsta.util.HttpManager;
import com.forsta.util.JsonFormat;

public class RankActivity extends Activity {
	
	
	 public static final String TAG="Debug";
	 private Context context;
	 private CityInfoHelper cityInfoHelper;
	 private LayoutInflater flater;
	 private LinearLayout mainlayout ;//activity布局
	 
	 private LinearLayout topLinearLayout;//最好10城市
	 private LinearLayout lastLinearLayout;//最差10城市
	 private TextView time;//更新时间
	 
	 private List<LinearLayout> tops=new ArrayList<LinearLayout>();
	 private List<LinearLayout> lasts=new ArrayList<LinearLayout>();
	 
	 private String t;//更新时间
	 
     private TextView refresh;
     private TextView map;
     private CustomProgressDialog progressDialog = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context=this.getApplicationContext();
		cityInfoHelper=new CityInfoHelper(context);
		flater = LayoutInflater.from(this);
		mainlayout=(LinearLayout) flater.inflate(R.layout.activity_rank, null);
		setContentView(mainlayout);
		progressDialog = CustomProgressDialog.createDialog(this);
		topLinearLayout=(LinearLayout) mainlayout.findViewById(R.id.topcontent);
        lastLinearLayout=(LinearLayout) mainlayout.findViewById(R.id.lastcontent);
        time=(TextView) mainlayout.findViewById(R.id.time);
        refresh=(TextView) mainlayout.findViewById(R.id.refresh);
		map=(TextView) mainlayout.findViewById(R.id.map);
		
		refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new RefreshData().execute(); 
				
			}
		});
		
		map.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(context,MapActivity.class);
				startActivity(intent);
			}
		});
        new Thread(new Runnable() {
			
			@Override
			public void run() {
				new RefreshUI().execute();
				
			}
		}).start();
        
	}
	//初始化数据
	private void initData(){
		SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
		Cursor cursor=db.query("ranks", null, null, null, null, null, null,"10");
		int i=1;
		while(cursor.moveToNext()){
			 LinearLayout linearLayout=(LinearLayout)flater.inflate(R.layout.rank, null);
			 TextView t1=(TextView) linearLayout.findViewById(R.id._id); 
			 TextView t2=(TextView) linearLayout.findViewById(R.id._area); 
			 TextView t3=(TextView) linearLayout.findViewById(R.id._aqi); 
			 TextView t4=(TextView) linearLayout.findViewById(R.id._quality);
			 t1.setText(i+"");
			 t2.setText(cursor.getString(cursor.getColumnIndex("area")));
			 t3.setText(cursor.getInt(cursor.getColumnIndex("aqi"))+"");
			 t4.setText(cursor.getString(cursor.getColumnIndex("quality")));
			 tops.add(linearLayout);
			 i++;
		}
		
		cursor=db.query("ranks", null, null, null, null, null, "_id desc","10");
		i=1;
		while(cursor.moveToNext()){
			 LinearLayout linearLayout=(LinearLayout)flater.inflate(R.layout.rank, null);
			 TextView t1=(TextView) linearLayout.findViewById(R.id._id); 
			 TextView t2=(TextView) linearLayout.findViewById(R.id._area); 
			 TextView t3=(TextView) linearLayout.findViewById(R.id._aqi); 
			 TextView t4=(TextView) linearLayout.findViewById(R.id._quality);
			 t1.setText(i+"");
			 t2.setText(cursor.getString(cursor.getColumnIndex("area")));
			 t3.setText(cursor.getInt(cursor.getColumnIndex("aqi"))+"");
			 t4.setText(cursor.getString(cursor.getColumnIndex("quality")));
			 lasts.add(linearLayout);
			 i++;
			 t="最新更新时间："+cursor.getString(cursor.getColumnIndex("time_point"));
		}
		db.close();
		
	}
	
	private class RefreshData extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			publishProgress();
			List<RankInfo> list=new ArrayList<RankInfo>();
			try {
				String response = HttpManager.openUrl("http://www.pm25.in/api/querys/aqi_ranking.json?token=zjdZuDxZZ7bsq6VAwPRY");
				JSONArray jsonArray=new JSONArray(response);
				int length=jsonArray.length();
				JSONObject obj=null;
				for(int i=0;i<length;i++)
				{
					obj=jsonArray.getJSONObject(i);
					RankInfo rank=JsonFormat.format(obj);
					System.out.println(i+"  "+rank.getArea());
					list.add(rank);	
				}
				cityInfoHelper.insert(list);
				Log.i(TAG, "排名数据更新完毕");
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.show();
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.i(TAG, "更新UI界面");
			new RefreshUI().execute();
		}
		
	}
	
	private class RefreshUI extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			initData();
			publishProgress();
			return null;
		}
		@Override
		protected void onProgressUpdate(Void... values) {
			topLinearLayout.removeAllViews();
			lastLinearLayout.removeAllViews();
			for(LinearLayout l:tops){
				topLinearLayout.addView(l);
			}
			tops.clear();
			
			for(LinearLayout l:lasts){
				lastLinearLayout.addView(l);
			}
			lasts.clear();
			
			time.setText(t);
			Log.i(TAG, "区域  UI更新完毕");
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(progressDialog!=null){
				progressDialog.dismiss();
			}
		}
		
	}

	
}
