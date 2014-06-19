package com.forsta.database;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.forsta.model.City;
import com.forsta.model.CityAvgInfo;
import com.forsta.model.DetailInfo;
import com.forsta.model.RankInfo;

public class CityInfoHelper extends SQLiteOpenHelper{
	    private static final String DATABASE_NAME="city.db";
	    private static final String TABLENAME0="city";
	    private static final String TABLENAME1="details";
		private static final String TABLENAME2="ranks";
		private static final String TABLENAME3="avg";
		
		private static final String CREATE_TABLE0="CREATE TABLE IF NOT EXISTS "+TABLENAME0+"("
				+"_id integer primary key autoincrement,"
				+"area varchar,"
				+"lng double,"
				+"lat double"
				+ ")";
		
		private static final String CREATE_TABLE1="CREATE TABLE IF NOT EXISTS "+TABLENAME1+"("
				+"_id integer primary key autoincrement,"
				+"aqi integer,"
				+"area varchar,"
				+"co double,"
				+"co_24h double,"
				+"no2 integer,"
				+"no2_24h integer,"
				+"o3 integer,"
				+"o3_24h integer,"
				+"o3_8h integer,"
				+"o3_8h_24h integer,"
				+"pm10 integer,"
				+"pm10_24h integer,"
				+"pm2_5 integer,"
				+"pm2_5_24h integer,"
				+"primary_pollutant varchar,"
				+"position_name varchar,"
				+"quality varchar,"
				+"so2 integer,"
				+"so2_24h integer,"
				+"station_code varchar,"
				+"time_point varchar"
				+ ")";
		
		
		private static final String CREATE_TABLE2="CREATE TABLE IF NOT EXISTS "+TABLENAME2+"("
											+"_id integer,"
											+"aqi integer,"
											+"area varchar,"
											+"co double,"
											+"co_24h double,"
											+"no2 integer,"
											+"no2_24h integer,"
											+"o3 integer,"
											+"o3_24h integer,"
											+"o3_8h integer,"
											+"o3_8h_24h integer,"
											+"pm10 integer,"
											+"pm10_24h integer,"
											+"pm2_5 integer,"
											+"pm2_5_24h integer,"
											+"quality varchar,"
											+"level varchar, "
											+"so2 integer,"
											+"so2_24h integer,"
											+"primary_pollutant varchar,"
											+"time_point varchar"
											+ ")";
		
		private static final String CREATE_TABLE3="CREATE TABLE IF NOT EXISTS "+TABLENAME3+"("
				+"_id integer primary key autoincrement,"
				+"aqi integer,"
				+"area varchar,"
				+"co double,"
				+"co_24h double,"
				+"no2 integer,"
				+"no2_24h integer,"
				+"o3 integer,"
				+"o3_24h integer,"
				+"o3_8h integer,"
				+"o3_8h_24h integer,"
				+"pm10 integer,"
				+"pm10_24h integer,"
				+"pm2_5 integer,"
				+"pm2_5_24h integer,"
				+"primary_pollutant varchar,"
				+"quality varchar,"
				+"so2 integer,"
				+"so2_24h integer,"
				+"station_code varchar,"
				+"time_point varchar"
				+ ")";
		
		
		public CityInfoHelper(Context context) {
			super(context, DATABASE_NAME, null, 1);
		}
		public CityInfoHelper(Context context, String name, CursorFactory factory,
				int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			
			db.beginTransaction();
			db.execSQL(CREATE_TABLE0);
			db.execSQL(CREATE_TABLE1);
			db.execSQL(CREATE_TABLE2);
			db.execSQL(CREATE_TABLE3);
			db.setTransactionSuccessful();
			db.endTransaction();
			Log.i("Debug", "4表创建完毕");
		}
		
		/*app初始化时 导入城市经纬度数据*/
		public synchronized void insertCity(List<City> list){
			SQLiteDatabase db=getWritableDatabase();
			db.beginTransaction();
			for(City info:list){
				ContentValues values=new ContentValues();
				values.put("area", info.getArea());
				values.put("lng", info.getLng());
				values.put("lat", info.getLat());
				db.insert("city", "_id", values);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			Log.i("Debug", "经纬度数据导入完毕");
		}
		
		/**
		 * 更新城市平均空气数据
		 * @param list
		 * @param city
		 * @param isAvg
		 */
		public synchronized void insert(List<CityAvgInfo> list,String city,boolean isAvg){
			SQLiteDatabase db=getWritableDatabase();
			db.beginTransaction();
			db.delete(TABLENAME3, "area=?", new String[]{city});
			for(CityAvgInfo info:list){
				ContentValues values=new ContentValues();
				values.put("aqi", info.getAqi());
				values.put("area", info.getArea());
				values.put("co", info.getCo());
				values.put("co_24h", info.getCo_24h());
				values.put("no2", info.getNo2());
				values.put("no2_24h", info.getNo2_24h());
				values.put("o3", info.getO3());
				values.put("o3_24h", info.getO3_24h());
				values.put("o3_8h", info.getO3_8h());
				values.put("o3_8h_24h", info.getO3_8h_24h());
				values.put("pm10", info.getPm10());
				values.put("pm10_24h", info.getPm10_24h());
				values.put("pm2_5", info.getPm2_5());
				values.put("pm2_5_24h", info.getPm2_5_24h());
				values.put("quality", info.getQuality());
				values.put("so2", info.getSo2());
				values.put("so2_24h", info.getSo2_24h());
				values.put("primary_pollutant", info.getPrimary_pollutant());
				values.put("time_point", info.getTime_point());
				db.insert(TABLENAME3, "_id", values);
				
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
		}
		
		//插入城市区域数据
		public synchronized void insert(List<DetailInfo> list,String city){
			SQLiteDatabase db=getWritableDatabase();
			db.beginTransaction();
			db.delete(TABLENAME1, "area=?", new String[]{city});
			for(DetailInfo info:list){
				ContentValues values=new ContentValues();
				values.put("aqi", info.getAqi());
				values.put("area", info.getArea());
				values.put("co", info.getCo());
				values.put("co_24h", info.getCo_24h());
				values.put("no2", info.getNo2());
				values.put("no2_24h", info.getNo2_24h());
				values.put("o3", info.getO3());
				values.put("o3_24h", info.getO3_24h());
				values.put("o3_8h", info.getO3_8h());
				values.put("o3_8h_24h", info.getO3_8h_24h());
				values.put("pm10", info.getPm10());
				values.put("pm10_24h", info.getPm10_24h());
				values.put("pm2_5", info.getPm2_5());
				values.put("pm2_5_24h", info.getPm2_5_24h());
				values.put("quality", info.getQuality());
				values.put("position_name", info.getPosition_name());
				values.put("so2", info.getSo2());
				values.put("so2_24h", info.getSo2_24h());
				values.put("station_code", info.getStation_code());
				values.put("primary_pollutant", info.getPrimary_pollutant());
				values.put("time_point", info.getTime_point());
				db.insert(TABLENAME1, "_id", values);
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
		}
		//插入排名数据
		public synchronized void insert(List<RankInfo> list){
			SQLiteDatabase db=getWritableDatabase();
			db.beginTransaction();
			db.delete(TABLENAME2, null, null);
			int i=1;
			for(RankInfo info:list){
				ContentValues values=new ContentValues();
				values.put("_id", i);
				values.put("aqi", info.getAqi());
				values.put("area", info.getArea());
				values.put("co", info.getCo());
				values.put("co_24h", info.getCo_24h());
				values.put("no2", info.getNo2());
				values.put("no2_24h", info.getNo2_24h());
				values.put("o3", info.getO3());
				values.put("o3_24h", info.getO3_24h());
				values.put("o3_8h", info.getO3_8h());
				values.put("o3_8h_24h", info.getO3_8h_24h());
				values.put("pm10", info.getPm10());
				values.put("pm10_24h", info.getPm10_24h());
				values.put("pm2_5", info.getPm2_5());
				values.put("pm2_5_24h", info.getPm2_5_24h());
				values.put("quality", info.getQuality());
				values.put("level", info.getLevel());
				values.put("so2", info.getSo2());
				values.put("so2_24h", info.getSo2_24h());
				values.put("primary_pollutant", info.getPrimary_pollutant());
				values.put("time_point", info.getTime_point());
				db.insert(TABLENAME2, null, values);
				i++;
			}
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
			
		}

}
