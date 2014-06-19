package com.forsta.pm25;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.baidu.mapapi.map.ItemizedOverlay;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.forsta.database.CityInfoHelper;
import com.forsta.weibo.ShareResult;

public class MyOverlay extends ItemizedOverlay{
    private CityInfoHelper cityInfoHelper;
    private Context context;
	public MyOverlay(Drawable defaultMarker, MapView mapView,CityInfoHelper cityInfoHelper,Context context) {
		super(defaultMarker, mapView);
		this.cityInfoHelper=cityInfoHelper;
		this.context=context;
	}
	
	@Override
	public boolean onTap(int index){
		OverlayItem item = getItem(index);
		Log.i("tag", index+"");
        SQLiteDatabase db=cityInfoHelper.getReadableDatabase();
        Cursor cursor=db.rawQuery("select*from ranks as a left outer join city as b on a.area=b.area", null);
        Log.i("tag", cursor.getCount()+"");
       
        if(cursor.moveToPosition(index)){
        	Log.i("tag", cursor.getString(cursor.getColumnIndex("area"))+"(aqi:"+cursor.getInt(cursor.getColumnIndex("aqi"))+",pm2.5:"+cursor.getInt(cursor.getColumnIndex("pm2_5")));
            new ShareResult(context, cursor.getString(cursor.getColumnIndex("area"))+"(aqi:"+cursor.getInt(cursor.getColumnIndex("aqi"))+",pm2.5:"+cursor.getInt(cursor.getColumnIndex("pm2_5"))+")").execute();
            
        }
        db.close();
		return true;
	}
	
	@Override
	public boolean onTap(GeoPoint pt , MapView mMapView){
		
		return false;
	}
}
