package com.forsta.pm25;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.forsta.database.CityInfoHelper;
import com.forsta.util.BitmapFormat;
import com.forsta.weibo.Constants;
import com.forsta.weibo.ShareResult;
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

public class MapActivity extends Activity implements Response{
	
	public static final String TAG="Debug";
	private MapView mMapView = null;
	private MapController mMapController = null;
	MKMapViewListener mMapListener = null;
	
	private Context context;
    private CityInfoHelper cityInfoHelper;
    
    private int[]  r={R.drawable.icon_1,R.drawable.icon_2,R.drawable.icon_3,R.drawable.icon_4,R.drawable.icon_5,R.drawable.icon_6};
    private MyOverlay mOverlay = null;
    private OverlayItem mCurItem = null;
    private ArrayList<OverlayItem>  mItems = null; 
    
    private TextView back;
    private TextView share;
    private String sharePic=null;
    
    
    /*微博分享*/
	private WeiboAuth mWeiboAuth;
	private Oauth2AccessToken mAccessToken;
	private IWeiboShareAPI  mWeiboShareAPI = null;
	private SsoHandler mSsoHandler;
	
	
	private String message;
	private String lat="0.0";
	private String lon="0.0";
	
	public static final String PREFS_NAME = "myprefs"; 
	private SharedPreferences settings=null;
	private String cityname;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		context = this.getApplicationContext();
		cityInfoHelper=new CityInfoHelper(context);
		settings=getSharedPreferences(PREFS_NAME, 1);
		cityname=settings.getString("city", null);
		back=(TextView) findViewById(R.id.back);
		share=(TextView) findViewById(R.id.share);
		
		cityname=settings.getString("city", null);
		SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
		
		
		Cursor cursor0=db.rawQuery("select*from city where area='"+cityname+"'", null);
		if(cursor0.moveToFirst()){
		lat=cursor0.getFloat(cursor0.getColumnIndex("lat"))+"";
		lon=cursor0.getFloat(cursor0.getColumnIndex("lng"))+"";
		}
		db.close();
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Log.i(TAG, "地图返回");
				finish();
			}
		});
		share.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSsoHandler = new SsoHandler(MapActivity.this, mWeiboAuth); 
				mSsoHandler.authorize(new AuthListener()); 
				
			}
		});
		
        ForstaApplication app=ForstaApplication.getInstance();
        if(app.mBMapManager==null){
        	app.mBMapManager=new BMapManager(getApplicationContext());
        	app.initEngineManager(getApplicationContext());
        }
		mMapView = (MapView)findViewById(R.id.bmapView);
		mMapView.regMapViewListener(app.mBMapManager, new MKMapViewListener() {
			
			@Override
			public void onMapMoveFinish() {
				
				
			}
			
			@Override
			public void onMapLoadFinish() {
				
				
			}
			
			@Override
			public void onMapAnimationFinish() {
				
				
			}
			
			@Override
			public void onGetCurrentMap(Bitmap bitmap) {
				 new ShareResult(context, "正在获取图片……").execute();
				File file1 = new File("/mnt/sdcard/DCIM/Forsta");
				if(!file1.exists())
				{
					file1.mkdir();
				}
				SimpleDateFormat format=new SimpleDateFormat("yyMMddHHss");
				String name=format.format(new Date());
				sharePic="/mnt/sdcard/DCIM/Forsta/"+name+".png";
				File file= new File(sharePic);
                FileOutputStream out;
                bitmap=BitmapFormat.format(bitmap);
                try{
                    out = new FileOutputStream(file);
                    if(bitmap.compress(Bitmap.CompressFormat.PNG, 0, out)) 
                    {
                        out.flush();
                        out.close();
                    }
                    
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory()))); 
            
                    new Thread(new Runnable() {
						
						@Override
						public void run() {
						    new ShareResult(context, "正在发送中，请稍等……").execute();
		                	String m=getInfoDate();
							Log.i(TAG, m);
							if(m!=null){
								Log.i(TAG, "lat:"+lat+",lng:"+lon);
							try {
								ShareWeibo.sendPostWeibo(mAccessToken.getToken(),m,lat,lon,sharePic,context);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							}
							
						}
					}).start();
                } 
                catch (FileNotFoundException e) 
                {
                    e.printStackTrace();
                } 
                catch (IOException e) 
                {
                    e.printStackTrace(); 
                }
				
			}
			
			@Override
			public void onClickMapPoi(MapPoi arg0) {
				
				
			}
		});
		
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
		
		
		mOverlay=new MyOverlay(getResources().getDrawable(R.drawable.ic_launcher), mMapView,cityInfoHelper,context);
	    mMapView.getOverlays().add(mOverlay);
		
        mMapController = mMapView.getController();
        mMapController.enableClick(true);
        mMapController.setZoom(12);
        mMapView.setBuiltInZoomControls(true);
        /**
         * 将地图移动至指定点
         * 使用百度经纬度坐标，可以通过http://api.map.baidu.com/lbsapi/getpoint/index.html查询地理坐标
         * 如果需要在百度地图上显示使用其他坐标系统的位置，请发邮件至mapapi@baidu.com申请坐标转换接口
         */
        GeoPoint p ;
        double cLat = 30.51667 ;
        double cLon = 114.31667 ;
        Intent  intent = getIntent();
        if ( intent.hasExtra("x") && intent.hasExtra("y") ){
        	//当用intent参数时，设置中心点为指定点
        	Bundle b = intent.getExtras();
        	p = new GeoPoint(b.getInt("y"), b.getInt("x"));
        }else{
        	 p = new GeoPoint((int)(Float.parseFloat(lat) * 1E6), (int)(Float.parseFloat(lon) * 1E6));
        }
        
        mMapController.setCenter(p);
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
						mMapView.getCurrentMap();
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
		
		
//		Cursor cursor0=db.rawQuery("select*from city where area='"+cityname+"'", null);
//		if(cursor0.moveToFirst()){
//		lat=cursor0.getFloat(cursor0.getColumnIndex("lat"))+"";
//		lon=cursor0.getFloat(cursor0.getColumnIndex("lng"))+"";
//		}
		StringBuffer sb=new StringBuffer();
		sb.append(cityname+" ");
		Cursor cursor=db.query("ranks", null, "area=?", new String[]{cityname}, null, null, null);
		if(cursor.moveToFirst()){
			sb.append("(");
			sb.append("aqi:"+cursor.getInt(cursor.getColumnIndex("aqi"))+",");
			sb.append("pm2.5:"+cursor.getInt(cursor.getColumnIndex("pm2_5")));
			sb.append(")");
			int id=cursor.getInt(cursor.getColumnIndex("_id"));
			
			long citynum=190;
			Cursor cursor2=db.rawQuery("select count(*) from ranks", null);
			cursor2.moveToFirst();
			citynum=cursor2.getLong(0);
			Log.i(TAG, "id:"+id+",citynum:"+citynum);
			sb.append("空气质量击败全国"+(1-(id-1)/(citynum+0.0f))*100+"%的城市!!");
			db.close();
			return sb.toString();
		}
		db.close();
		
		return null;
			
	}
    
	private class RefreshUI extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
			Cursor cursor=db.rawQuery("select*from ranks as a left outer join city as b on a.area=b.area", null);
			while(cursor.moveToNext()){
				GeoPoint p1 = new GeoPoint ((int)(cursor.getFloat(cursor.getColumnIndex("lat"))*1E6),(int)(cursor.getFloat(cursor.getColumnIndex("lng"))*1E6));
		          OverlayItem item1 = new OverlayItem(p1,"覆盖物1","");
		          System.out.println(cursor.getString(cursor.getColumnIndex("area")));
		         int aqi=cursor.getInt(cursor.getColumnIndex("aqi"));
		         int index=aqi/50;
		         if(index<4)
		          item1.setMarker(context.getResources().getDrawable(r[aqi/50]));
		         else if(index<6)
		        	 item1.setMarker(context.getResources().getDrawable(r[4]));
		         else{
		        	 item1.setMarker(context.getResources().getDrawable(r[5]));
		         }
		          mOverlay.addItem(item1);
			}
			
			db.close();
			publishProgress(null);
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			mMapView.refresh();
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
