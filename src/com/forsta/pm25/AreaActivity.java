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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.forsta.model.DetailInfo;
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

public class AreaActivity extends Activity implements OnClickListener,Response{
	
	 public static final String PREFS_NAME = "myprefs"; 
	 public static final String TAG="Debug";
	 private SharedPreferences settings=null;
	 private Context context;
	 private String cityname;
	 private CityInfoHelper cityInfoHelper;
	 
	 private LayoutInflater flater;
	 private LinearLayout mainlayout ;//activity布局
	 
	 private LinearLayout content;//显示内容布局
	 private TextView currentCity;//城市
	 private TextView time;//更新时间
	 private LinearLayout lay;
	 
	 private List<LinearLayout> list=new ArrayList<LinearLayout>();
	 
	 private String t;//保存时间
	 private String c;//保存城市名
	 
	 private LinearLayout.LayoutParams layoutParams;
	 private int  topMargin;
	 private int start; //按下时Y坐标位置 
	 private int lastY; //上一次Y坐标位置 防止抖动
	 
	 private CustomProgressDialog progressDialog = null;
	 private ImageButton addCity;
	 private ImageButton share;
	 
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
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//载入布局
		flater = LayoutInflater.from(this);
		mainlayout=(LinearLayout) flater.inflate(R.layout.activity_area, null);
		setContentView(mainlayout);
		
		content=(LinearLayout) mainlayout.findViewById(R.id.content);
		currentCity=(TextView) mainlayout.findViewById(R.id.city);
		time=(TextView)mainlayout.findViewById(R.id.time);
		
		lay=(LinearLayout) mainlayout.findViewById(R.id.lay);
		
		progressDialog = CustomProgressDialog.createDialog(this);
		addCity=(ImageButton) mainlayout.findViewById(R.id.addCity);
		addCity.setOnClickListener(this);
		share=(ImageButton) findViewById(R.id.share);
		share.setOnClickListener(this);
		lay.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				System.out.println("topMargin:"+topMargin);
				int x=(int)event.getY();
				switch(event.getAction()){
				case MotionEvent.ACTION_DOWN: 
				     LinearLayout.LayoutParams layoutParams=(LayoutParams) lay.getLayoutParams();
				     start=x-layoutParams.topMargin; 
				     break; 
				case MotionEvent.ACTION_MOVE: 
				     if(x>lastY){
				        layoutParams=(LayoutParams) lay.getLayoutParams();
				        layoutParams.topMargin=x-start;
				        lay.setLayoutParams(layoutParams);
				        lay.invalidate();
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
		});
		layoutParams=(LayoutParams) lay.getLayoutParams();
		topMargin=layoutParams.topMargin;
		System.out.println("topMargin:"+topMargin);
		context=this.getApplicationContext();
		cityInfoHelper=new CityInfoHelper(context);
	    settings=getSharedPreferences(PREFS_NAME, 1);
	    cityname=settings.getString("city", null);
	    
	    
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
	    
	    new Thread(new Runnable() {
			
			@Override
			public void run() {
				new RefreshUI().execute();
			}
		}).start();
	    
	    
	    
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
	Cursor cursor=db.query("details", null, "area=?", new String[]{cityname}, null, null, null);
	if(cursor.getCount()==0){
		Log.i(TAG, "暂无数据");
	}
	else{
		Log.i(TAG, "cursor："+cursor.getCount());
		StringBuffer sb=new StringBuffer();
		sb.append(cityname+":(");
		while(cursor.moveToNext()){
			sb.append(cursor.getString(cursor.getColumnIndex("position_name"))+"(pm2.5:"+cursor.getInt(cursor.getColumnIndex("pm2_5"))+",aqi:"+cursor.getInt(cursor.getColumnIndex("aqi"))+")");
		    
		}
		sb.append(")");
			db.close();
			return sb.toString();
	    }
		db.close();
		
	return null;
		
}
	
	private void initData(){
		SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
		Cursor cursor=db.query("details", null, "area=?", new String[]{cityname}, null, null, null);
		int i=1;
		while(cursor.moveToNext()){
			 LinearLayout linearLayout=(LinearLayout)flater.inflate(R.layout.area, null);
			 TextView t1=(TextView) linearLayout.findViewById(R.id._id); 
			 TextView t2=(TextView) linearLayout.findViewById(R.id._area); 
			 TextView t3=(TextView) linearLayout.findViewById(R.id._aqi); 
			 TextView t4=(TextView) linearLayout.findViewById(R.id._pm25); 
			 String position=cursor.getString(cursor.getColumnIndex("position_name"));
			 String aqi=cursor.getInt(cursor.getColumnIndex("aqi"))+"";
			 String pm25=cursor.getInt(cursor.getColumnIndex("pm2_5"))+"";
			 t1.setText(i+"");
			 t2.setText(position);
			 t3.setText(aqi);
			 t4.setText(pm25);
			 list.add(linearLayout);
			 i++;
			 t="最新更新时间："+cursor.getString(cursor.getColumnIndex("time_point"));
			
		}
		db.close();
		c=settings.getString("city", null);
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
			content.removeAllViews();
			for(LinearLayout l:list){
				content.addView(l);
			}
			list.clear();
			currentCity.setText(c);
			time.setText(t);
			Log.i(TAG, "区域  UI更新完毕");
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
				
				List<DetailInfo> list=new ArrayList<DetailInfo>();
				try {
					String response = HttpManager.openUrl("http://www.pm25.in/api/querys/aqi_details.json?city="+cityname+"&token=zjdZuDxZZ7bsq6VAwPRY&stations=yes");
					System.out.println(response);
					JSONArray jsonArray=new JSONArray(response);
					int length=jsonArray.length();
					JSONObject obj=null;
					for(int i=0;i<length-1;i++)
					{
						obj=jsonArray.getJSONObject(i);
						list.add(JsonFormat.format2(obj));
					}
					String city=obj.getString("area");
					cityInfoHelper.insert(list,city);
					new RefreshUI().execute();
					
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
				
				System.out.println(values[0]);
				layoutParams.topMargin=values[0];
				lay.setLayoutParams(layoutParams);
			}
			
			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				progressDialog.dismiss();
			}
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
			mSsoHandler = new SsoHandler(AreaActivity.this, mWeiboAuth); 
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
