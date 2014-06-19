package com.forsta.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;
/**
 * 
 * @author Forsta
 * Http请求 返回字符串
 */
public class HttpManager {
	private static HttpClient getHttpClient(){
		return new DefaultHttpClient();
	}
	public static String openUrl(String url) throws ClientProtocolException, IOException{
		StringBuffer json=new StringBuffer();
		HttpClient client=getHttpClient();
		HttpGet get=new HttpGet(url);
		HttpResponse response=client.execute(get);
		StatusLine statusLine=response.getStatusLine();
		if(statusLine.getStatusCode()==200){
			Log.i("tag", "http request successfully");
			HttpEntity httpEntity=response.getEntity();
			BufferedReader br=new BufferedReader(new InputStreamReader(httpEntity.getContent()));
			String s=null;
			
			while((s=br.readLine())!=null){
				json.append(s);
			}
		}
		return json.toString();
	}
}
