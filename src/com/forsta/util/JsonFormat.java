package com.forsta.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.forsta.model.CityAvgInfo;
import com.forsta.model.DetailInfo;
import com.forsta.model.RankInfo;

public class JsonFormat {
	public static RankInfo format(JSONObject obj) throws JSONException{
		RankInfo r=new RankInfo();
		r.setAqi(obj.getInt("aqi"));
		r.setArea(obj.getString("area"));
		r.setCo(obj.getDouble("co"));
		r.setCo_24h(obj.getDouble("co_24h"));
		r.setNo2(obj.getInt("no2"));
		r.setNo2_24h(obj.getInt("no2_24h"));
		r.setO3(obj.getInt("o3"));
		r.setO3_24h(obj.getInt("o3_24h"));
		r.setO3_8h(obj.getInt("o3_8h"));
		r.setO3_8h_24h(obj.getInt("o3_8h_24h"));
		r.setPm10(obj.getInt("pm10"));
		r.setPm10_24h(obj.getInt("pm10_24h"));
		r.setPm2_5(obj.getInt("pm2_5"));
		r.setPm2_5_24h(obj.getInt("pm2_5_24h"));
		r.setQuality(obj.getString("quality"));
		r.setLevel(obj.getString("level"));
		r.setSo2(obj.getInt("so2"));
		r.setSo2_24h(obj.getInt("so2_24h"));
		r.setPrimary_pollutant(obj.getString("primary_pollutant"));
		r.setTime_point(obj.getString("time_point"));
		return r;
	}
	
	public static DetailInfo format2(JSONObject obj) throws JSONException{
		DetailInfo r=new DetailInfo();
		r.setAqi(obj.getInt("aqi"));
		r.setArea(obj.getString("area"));
		r.setCo(obj.getDouble("co"));
		r.setCo_24h(obj.getDouble("co_24h"));
		r.setNo2(obj.getInt("no2"));
		r.setNo2_24h(obj.getInt("no2_24h"));
		r.setO3(obj.getInt("o3"));
		r.setO3_24h(obj.getInt("o3_24h"));
		r.setO3_8h(obj.getInt("o3_8h"));
		r.setO3_8h_24h(obj.getInt("o3_8h_24h"));
		r.setPm10(obj.getInt("pm10"));
		r.setPm10_24h(obj.getInt("pm10_24h"));
		r.setPm2_5(obj.getInt("pm2_5"));
		r.setPm2_5_24h(obj.getInt("pm2_5_24h"));
		r.setQuality(obj.getString("quality"));
		r.setPosition_name(obj.getString("position_name"));
		r.setSo2(obj.getInt("so2"));
		r.setSo2_24h(obj.getInt("so2_24h"));
		r.setStation_code(obj.getString("station_code"));
		r.setPrimary_pollutant(obj.getString("primary_pollutant"));
		r.setTime_point(obj.getString("time_point"));
		return r;
	}
	
	public static CityAvgInfo format1(JSONObject obj) throws JSONException{
		CityAvgInfo r=new CityAvgInfo();
		r.setAqi(obj.getInt("aqi"));
		r.setArea(obj.getString("area"));
		r.setCo(obj.getDouble("co"));
		r.setCo_24h(obj.getDouble("co_24h"));
		r.setNo2(obj.getInt("no2"));
		r.setNo2_24h(obj.getInt("no2_24h"));
		r.setO3(obj.getInt("o3"));
		r.setO3_24h(obj.getInt("o3_24h"));
		r.setO3_8h(obj.getInt("o3_8h"));
		r.setO3_8h_24h(obj.getInt("o3_8h_24h"));
		r.setPm10(obj.getInt("pm10"));
		r.setPm10_24h(obj.getInt("pm10_24h"));
		r.setPm2_5(obj.getInt("pm2_5"));
		r.setPm2_5_24h(obj.getInt("pm2_5_24h"));
		r.setQuality(obj.getString("quality"));
		r.setSo2(obj.getInt("so2"));
		r.setSo2_24h(obj.getInt("so2_24h"));
		r.setPrimary_pollutant(obj.getString("primary_pollutant"));
		r.setTime_point(obj.getString("time_point"));
		return r;
	}
	
}
