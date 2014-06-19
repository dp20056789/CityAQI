package com.forsta.pm25;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;

import com.forsta.database.CityInfoHelper;
import com.forsta.model.City;

public class WelcomeActivity extends Activity {
	private LinearLayout line;
	private final int SPLASH_DISPLAY_LENGHT = 4000; 

	/*保存初始值*/
	public static final String PREFS_NAME = "myprefs"; 
	public static final String TAG="Debug";
	private SharedPreferences settings=null;
	private SharedPreferences.Editor editor=null;
	private CityInfoHelper cityInfoHelper;
	private Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		line=(LinearLayout) findViewById(R.id.pic);
		
		settings=getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		boolean initDB=settings.getBoolean("initDBFlag", false);
		context = this.getApplicationContext();
		cityInfoHelper=new CityInfoHelper(context);
		
		/*导入城市经纬度数据库*/
		if(initDB){
			Log.i(TAG, "城市经纬度数据库已经导入");
		}else{
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					Log.i(TAG, "城市位置信息未加载");
					Log.i(TAG, "开始加载城市位置数据库");
					init();
					initOver();
					Log.i(TAG, "城市位置信息加载完成");
					
				}
			}).start();
		
		}
		AnimationSet  animationset=new AnimationSet(true); 
		AlphaAnimation alphaAnimation=new AlphaAnimation(1.0f,1.0f);
	        alphaAnimation.setDuration(2000);
	        animationset.addAnimation(alphaAnimation);
	        line.startAnimation(animationset);
	        new Handler().postDelayed(new Runnable(){ 
	        	  
	            @Override 
	            public void run() { 
	                Intent mainIntent = new Intent(WelcomeActivity.this,MainActivity.class); 
	                WelcomeActivity.this.startActivity(mainIntent); 
	                WelcomeActivity.this.finish(); 
	            } 
	               
	           }, SPLASH_DISPLAY_LENGHT); 
	}
	
	
	 private void init(){
	    	Log.i(TAG, "正在加载数据库");
	    	String dbDirPath = "/data/data/"+context.getPackageName()+"/databases";
	        File dbDir = new File(dbDirPath);
	        if (!dbDir.exists() || !dbDir.isDirectory()) {
	         dbDir.mkdir();
	        }
			try {
				InputStream is=getAssets().open("city.db");
				FileOutputStream os = new FileOutputStream(dbDirPath+"/data.db");
				byte[] buffer = new byte[1024];
	            int count = 0;
	            while ((count = is.read(buffer)) > 0) {
	              os.write(buffer, 0, count);
	            }
	            is.close();
	            os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			List<City> list=new ArrayList<City>();
			SQLiteDatabase rdb=openOrCreateDatabase("data.db", MODE_PRIVATE, null);
			Cursor cursor=rdb.rawQuery("select*from city", null);
			while(cursor.moveToNext()){
				City c=new City();
				c.setArea(cursor.getString(cursor.getColumnIndex("area")));
				c.setLat(cursor.getDouble(cursor.getColumnIndex("lat")));
				c.setLng(cursor.getDouble(cursor.getColumnIndex("lng")));
				list.add(c);
			}
			cursor.close();
			rdb.close();
			cityInfoHelper.insertCity(list);
	    	
	    }
        /*数据库导入完毕，修改initFlag标记*/
	    private void initOver(){
	    	editor=settings.edit();
	    	editor.putBoolean("initDBFlag", true);
	    	editor.putString("city", "武汉");
	    	editor.commit();
	    }
}