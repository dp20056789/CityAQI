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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.forsta.database.CityInfoHelper;
import com.forsta.model.CityAvgInfo;
import com.forsta.util.HttpManager;
import com.forsta.util.JsonFormat;
import com.forsta.weibo.Constants;
import com.forsta.weibo.ShareWeibo;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler.Response;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

public class HomeActivity extends Activity implements OnTouchListener,OnClickListener,Response{
	private TextView city;//城市
	private TextView aqi;//aqi值
	private TextView advice;//温馨提示
	private TextView quality;//空气质量
	private TextView pm25;//pm25值
	private TextView pm10;//pm10值
	private TextView co;//co值
	private TextView no2;//no2值
	private TextView o3;//o3值
	private TextView so2;//so2值
	private TextView time;//时间
	private List<TextView> textGroup=new ArrayList<TextView>();
	List<String> infos=new ArrayList<String>();
	
	private CityInfoHelper cityInfoHelper;
	public static final String PREFS_NAME = "myprefs"; 
	public static final String TAG="Debug";
	private SharedPreferences settings=null;
	private Context context;
	private String cityname;
	
	private LinearLayout layout;
	private LinearLayout.LayoutParams layoutParams;
	private int  topMargin;
    private int start; //按下时Y坐标位置 
    private int lastY; //上一次Y坐标位置 防止抖动
	
    private int aqiValue;  
    private CustomProgressDialog progressDialog = null;
    
    private LinearLayout layout2;
	private View v1;  //用于指示空气质量指示表
	private View v2;
    
	private ImageButton addCity=null;
	private ImageButton share=null;
	/*微博分享*/
	private WeiboAuth mWeiboAuth;
	private Oauth2AccessToken mAccessToken;
	private IWeiboShareAPI  mWeiboShareAPI = null;
	private SsoHandler mSsoHandler;
	
	
	private String message;
	private String lat="0.0";
	private String lon="0.0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		city=(TextView) findViewById(R.id.city);
		aqi=(TextView) findViewById(R.id.aqi);
		quality=(TextView) findViewById(R.id.quality);
		advice=(TextView) findViewById(R.id.advice);
		pm25=(TextView) findViewById(R.id.pm25);
		pm10=(TextView) findViewById(R.id.pm10);
		co=(TextView) findViewById(R.id.co);
		no2=(TextView) findViewById(R.id.no2);
		o3=(TextView) findViewById(R.id.o3);
		so2=(TextView) findViewById(R.id.so2);
		time=(TextView) findViewById(R.id.time);
		
		layout=(LinearLayout) findViewById(R.id.linearLayout);
		layout.setOnTouchListener(this);
		layoutParams=(LayoutParams) layout.getLayoutParams();
		topMargin=layoutParams.topMargin;
		
		layout2=(LinearLayout) findViewById(R.id.qualityTable);
		v1=new View(this);
		v2=new View(this);
		v1.setBackgroundColor(Color.BLACK);
		v2.setBackgroundColor(Color.GRAY);
		
		
		addCity=(ImageButton) findViewById(R.id.addCity);
		addCity.setOnClickListener(this);
		
        share=(ImageButton) findViewById(R.id.share);
		share.setOnClickListener(this);
		addTextViewGroup();
		
		progressDialog = CustomProgressDialog.createDialog(this);
		
		settings=getSharedPreferences(PREFS_NAME, 1);
		cityname=settings.getString("city", null);
		context = this.getApplicationContext();
		cityInfoHelper=new CityInfoHelper(context);
		
		initData();
		
		/*初始化微博组件*/
		mWeiboAuth = new WeiboAuth(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE); 
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY); // 创建微博分享接口实例
        // 如果未安装微博客户端，设置下载微博对应的回调
        if (!mWeiboShareAPI.isWeiboAppInstalled()) {
           mWeiboShareAPI.registerWeiboDownloadListener(new IWeiboDownloadListener() {
               @Override
               public void onCancel() {
                   Toast.makeText(getApplicationContext(), 
                           "未安装微博客户端", 
                           Toast.LENGTH_SHORT).show();
               }
           });
       }
        if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }
		
	}
	
	class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
            	Log.i("tag", mAccessToken.getToken());
            	Log.i("tag", mAccessToken.getUid());
            	Log.i("tag", mAccessToken.getExpiresTime()+"");
            	

            	new Thread(new Runnable() {
					
					@Override
					public void run() {
		               
						try {
							String m=getInfoDate();
							Log.i(TAG, m);
							if(m!=null){
								Log.i(TAG, "lat:"+lat+",lng:"+lon);
							ShareWeibo.sendWeibo(mAccessToken.getToken(),m,lat,lon,context);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
            	
                
            } else {
                // 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
            	Log.i("tag", "失败");
                
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(getApplicationContext(), 
                    "取消", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(getApplicationContext(), 
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }
	
	private String getInfoDate(){
		cityname=settings.getString("city", null);
		SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
		
		
		Cursor cursor0=db.rawQuery("select*from city where area='"+cityname+"'", null);
		if(cursor0.moveToFirst()){
		lat=cursor0.getFloat(cursor0.getColumnIndex("lat"))+"";
		lon=cursor0.getFloat(cursor0.getColumnIndex("lng"))+"";
		}
		Cursor cursor=db.query("avg", null, "area=?", new String[]{cityname}, null, null, null);
		if(cursor.getCount()==0){
			Log.i(TAG, "暂无数据");
		}
		else{
			StringBuffer sb=new StringBuffer();
			if(cursor.moveToFirst()){
				int aqiIndex=cursor.getInt(cursor.getColumnIndex("aqi"));
				sb.append(cityname+":");
				sb.append("空气指数："+aqiIndex+",");
				sb.append(""+cursor.getString(cursor.getColumnIndex("quality"))+",");
				sb.append("pm2.5:"+cursor.getInt(cursor.getColumnIndex("pm2_5"))+",");
				sb.append("pm10:"+cursor.getInt(cursor.getColumnIndex("pm10"))+",");
				sb.append("co:"+cursor.getFloat(cursor.getColumnIndex("co"))+",");
				sb.append("o3:"+cursor.getInt(cursor.getColumnIndex("o3"))+",");
				sb.append("no2:"+cursor.getInt(cursor.getColumnIndex("no2"))+",");
				sb.append("so2:"+cursor.getInt(cursor.getColumnIndex("so2"))+",");
				sb.append("更新时间:"+cursor.getString(cursor.getColumnIndex("time_point")));
				db.close();
				return sb.toString();
		    }
			db.close();
		}
		return null;
			
	}
	//初始化数据  从数据库中读取上次空气质量信息  
	private void initData(){
		cityname=settings.getString("city", null);
		SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
		Cursor cursor=db.query("avg", null, "area=?", new String[]{cityname}, null, null, null);
		if(cursor.getCount()==0){
			infos.add(cityname);
			infos.add("无信息");
			infos.add("无信息 ");
			infos.add("无信息 ");
			infos.add("—");
			infos.add("—");
			infos.add("—");
			infos.add("—");
			infos.add("—");
			infos.add("—");
			infos.add(" ");
			aqiValue=0;
		}else{
			if(cursor.moveToFirst()){
			int aqiIndex=cursor.getInt(cursor.getColumnIndex("aqi"));
			infos.add(cityname);
			infos.add(aqiIndex+"");
			infos.add(cursor.getString(cursor.getColumnIndex("quality")));
			infos.add("温馨提示："+selectAdvice(aqiIndex));
			infos.add(cursor.getInt(cursor.getColumnIndex("pm2_5"))+"");
			infos.add(cursor.getInt(cursor.getColumnIndex("pm10"))+"");
			infos.add(cursor.getFloat(cursor.getColumnIndex("co"))+"");
			infos.add(cursor.getInt(cursor.getColumnIndex("no2"))+"");
			infos.add(cursor.getInt(cursor.getColumnIndex("o3"))+"");
			infos.add(cursor.getInt(cursor.getColumnIndex("so2"))+"");
			infos.add("最新更新时间："+cursor.getString(cursor.getColumnIndex("time_point"))+"");
			aqiValue=aqiIndex;
			}
		}
		new RefreshUI().execute();
		new RefreshTable(aqiValue).execute();
	}
	private String selectAdvice(int aqi){
		String [] advices={"空气质量令人满意，基本无空气污染，各类人群可正常活动",
				           "空气质量可接受，但某些污染物可能对极少数异常敏感人群健康有较弱影响，建议极少数异常敏感人群应减少户外活动",
				           "易感人群症状有轻度加剧，健康人群出现刺激症状。建议儿童、老年人及心脏病、呼吸系统疾病患者应减少长时间、高强度的户外锻炼",
				           "进一步加剧易感人群症状，可能对健康人群心脏、呼吸系统有影响，建议疾病患者避免长时间、高强度的户外锻练，一般人群适量减少户外运动",
				           "心脏病和肺病患者症状显著加剧，运动耐受力降低，健康人群普遍出现症状，建议儿童、老年人和心脏病、肺病患者应停留在室内，停止户外运动，一般人群减少户外运动",
				           "健康人群运动耐受力降低，有明显强烈症状，提前出现某些疾病，建议儿童、老年人和病人应当留在室内，避免体力消耗，一般人群应避免户外活动"};
		int index=aqi/50;
		if(index<4){
			return advices[index];
		}else if(index>6){
			return advices[5];
		}else{
			return advices[4];
		}
	}
    //添加到控件到List
	private void addTextViewGroup(){
		textGroup.add(city);
		textGroup.add(aqi);
		textGroup.add(quality);
		textGroup.add(advice);
		textGroup.add(pm25);
		textGroup.add(pm10);
		textGroup.add(co);
		textGroup.add(no2);
		textGroup.add(o3);
		textGroup.add(so2);
		textGroup.add(time);
	}
	
	private class RefreshTable extends AsyncTask<Void,Float, Void>{
		private int aqi;
        public RefreshTable(int aqi) {
			this.aqi=aqi;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			float weight=0.0f;
		    float w=aqi/50.0f;
		    if(w<=4){
		    	weight=aqi/(300.0f-aqi);
		    	Log.i(TAG, "前4级  " +weight);
		    }else if(w>4 &&w<6){
		    	weight=(100.0f+aqi/2.0f)/(200.0f-aqi/2f);
		    	Log.i(TAG, "5级  "+weight);
		    }else{
		    	weight=(175.0f+aqi/4.0f)/(125.0f-aqi/4f);
		    	Log.i(TAG, "6级  "+weight);
		    	
		    }
			
			float current=0;
			while(current<=weight){
				current=current+0.05f;
				publishProgress(current);
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Float... values) {
			
			 layout2.removeAllViews();
			 LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
					    10, LayoutParams.MATCH_PARENT);
			 params1.weight=values[0];
		     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					    10, LayoutParams.MATCH_PARENT);
			 params.weight=1.0f;
			 v1.setLayoutParams(params1);
		   	 v2.setLayoutParams(params);
			 layout2.addView(v1);
		     layout2.addView(v2);
		  	 layout2.invalidate();
		}
		
	}
	
	//更新数据
	private class RefreshData extends AsyncTask<Void, Integer, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			topMargin=layoutParams.topMargin;
			while(true){
				topMargin=topMargin-20;
				if(topMargin<0){
					topMargin=0;
					break;
				}
				publishProgress(topMargin);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			lastY=0;
			
			List<CityAvgInfo> list=new ArrayList<CityAvgInfo>();
			try {
				String response=HttpManager.openUrl("http://www.pm25.in/api/querys/aqi_details.json?city="+cityname+"&token=zjdZuDxZZ7bsq6VAwPRY&stations=no");
			    System.out.println(response);
			    JSONArray jsonArray=new JSONArray(response);
			    int length=jsonArray.length();
			    if(length>0){
			    JSONObject obj=null;
			    obj=jsonArray.getJSONObject(0);
				list.add(JsonFormat.format1(obj));
				String city=obj.getString("area");
				cityInfoHelper.insert(list, city, true);
				initData();
			    }
				
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
			
			layoutParams.topMargin=values[0];
			layout.setLayoutParams(layoutParams);
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
		}
		
	}
	//更新界面
	private class RefreshUI extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			Log.i(TAG, "开始更新界面");
			publishProgress(null);
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			int i=0;
			for(TextView textView:textGroup){
				textView.setText(infos.get(i));
				i++;
			}
			infos.clear();
			Log.i(TAG,"UI更新完毕");
			super.onProgressUpdate(values);
		}
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int x=(int)event.getY();
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN: 
		     LinearLayout.LayoutParams layoutParams=(LayoutParams) layout.getLayoutParams();
		     start=x-layoutParams.topMargin; 
		     break; 
		case MotionEvent.ACTION_MOVE: 
		     if(x>lastY){
		        layoutParams=(LayoutParams) layout.getLayoutParams();
		        layoutParams.topMargin=x-start;
		        layout.setLayoutParams(layoutParams);
		        layout.invalidate();
		     }
		     lastY=x;
		     break;
		case MotionEvent.ACTION_UP:
			cityname=settings.getString("city", null);
			new RefreshData().execute();
			progressDialog.show();
			break;
		}
		return true;
		
	}
	@Override
	public void onClick(View v) {
		int viewid=v.getId();
		switch (viewid) {
		case R.id.addCity:
			Intent intent=new Intent(getApplicationContext(), CityActivity.class);
			startActivity(intent);
			break;
		case R.id.share:
			mSsoHandler = new SsoHandler(HomeActivity.this, mWeiboAuth); 
			mSsoHandler.authorize(new AuthListener()); 
			break;
		default:
			break;
		}
		
		
	
		
	}
	   @Override
			public void onResponse(BaseResponse baseResp) {
			    switch (baseResp.errCode) {
			    case WBConstants.ErrorCode.ERR_OK:
			    	Log.i("tag","ok");
			        Toast.makeText(this, "ok", Toast.LENGTH_LONG).show();
			        break;
			    case WBConstants.ErrorCode.ERR_CANCEL:
			    	Log.i("tag", "cancle");
			        Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();
			        break;
			    case WBConstants.ErrorCode.ERR_FAIL:
			    	Log.i("tag", "errorCode"+baseResp.errCode);
			    	Log.i("tag", "   "+baseResp.reqPackageName);
			    	Log.i("tag", baseResp.errMsg);
			        Toast.makeText(this, 
			                "失败" + "Error Message: " + baseResp.errMsg, 
			                Toast.LENGTH_LONG).show();
			        break;
			    }
			}
	   @Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				// TODO Auto-generated method stub
				mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
				
		}
	
	
}
