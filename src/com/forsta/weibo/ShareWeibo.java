package com.forsta.weibo;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

public class ShareWeibo {
	public static void sendWeibo(String access_token,String status,String lat,String lon,Context context) throws Exception{
		HttpClient client=new DefaultHttpClient();
		HttpPost post=new HttpPost("https://api.weibo.com/2/statuses/update.json");
		  //添加所需要的post内容
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("access_token", access_token));
        nvps.add(new BasicNameValuePair("status", status));
        nvps.add(new BasicNameValuePair("lat", lat));
        nvps.add(new BasicNameValuePair("long", lon));
        // 设置字符集
        HttpEntity entity = new UrlEncodedFormEntity(nvps, "UTF-8");
        post.setEntity(entity);
        HttpResponse httpResponse = client.execute(post);
        // HttpStatus.SC_OK表示连接成功
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
        {
             String strResult = EntityUtils.toString(httpResponse.getEntity());
             Log.i("tag",strResult);
             new ShareResult(context,"分享完成").execute();
        } 
        else 
        {
    	     Log.i("tag","请求错误:"+httpResponse.getStatusLine().getStatusCode());
    	     new ShareResult(context,"错误："+httpResponse.getStatusLine().getStatusCode()).execute();
        }
	}
	
	
	public static void sendTextWeibo(String access_token,String status,Context context) throws Exception{
		HttpClient client=new DefaultHttpClient();
		HttpPost post=new HttpPost("https://api.weibo.com/2/statuses/update.json");
		  //添加所需要的post内容
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("access_token", access_token));
        nvps.add(new BasicNameValuePair("status", status));

        // 设置字符集
        HttpEntity entity = new UrlEncodedFormEntity(nvps, "UTF-8");
        post.setEntity(entity);
        HttpResponse httpResponse = client.execute(post);
        // HttpStatus.SC_OK表示连接成功
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
        {
             String strResult = EntityUtils.toString(httpResponse.getEntity());
             Log.i("tag",strResult);
             new ShareResult(context,"分享完成").execute();
        } 
        else 
        {
    	     Log.i("tag","请求错误:"+httpResponse.getStatusLine().getStatusCode());
    	     new ShareResult(context,"错误："+httpResponse.getStatusLine().getStatusCode()).execute();
        }
	}
	
	public static void sendPostWeibo(String access_token,String status,String lat,String lon,String pic,Context context) throws Exception{
		HttpClient client=new DefaultHttpClient();
		HttpPost post=new HttpPost("https://upload.api.weibo.com/2/statuses/upload.json");
		  //添加所需要的post内容
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("access_token", access_token));
        nvps.add(new BasicNameValuePair("status", status));
        nvps.add(new BasicNameValuePair("lat", lat));
        nvps.add(new BasicNameValuePair("long", lon));
        // 设置字符集
        MultipartEntity multipartEntity = new MultipartEntity();
        StringBody stringBody; 
        FormBodyPart fbp; 
        for (NameValuePair queryParam : nvps) { 
            stringBody = new StringBody(queryParam.getValue(), Charset.forName("UTF-8")); 
            fbp = new FormBodyPart(queryParam.getName(), stringBody); 
            multipartEntity.addPart(fbp); 
        } 
        File file=new File(pic); 
        FileBody fb=new FileBody(file,"image/png", "UTF-8"); 
        fbp = new FormBodyPart("pic", fb); 
        multipartEntity.addPart(fbp); 
        
        
       
        post.setEntity(multipartEntity);

        HttpResponse httpResponse = client.execute(post);
        // HttpStatus.SC_OK表示连接成功
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
        {
             String strResult = EntityUtils.toString(httpResponse.getEntity());
             Log.i("tag",strResult);
             new ShareResult(context,"分享完成").execute();
        } 
        else 
        {
    	     Log.i("tag","请求错误:"+httpResponse.getStatusLine().getStatusCode());
    	     new ShareResult(context,"错误："+httpResponse.getStatusLine().getStatusCode()).execute();
        }
	}
	
}
