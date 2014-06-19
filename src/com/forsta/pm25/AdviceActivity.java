package com.forsta.pm25;

import com.forsta.pm25.HomeActivity.AuthListener;
import com.forsta.weibo.Constants;
import com.forsta.weibo.ShareWeibo;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler.Response;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AdviceActivity extends Activity implements Response{
	private TextView back;
	private EditText message;
	private Button   send;
	
	/*微博分享*/
	private WeiboAuth mWeiboAuth;
	private Oauth2AccessToken mAccessToken;
	private IWeiboShareAPI  mWeiboShareAPI = null;
	private SsoHandler mSsoHandler;
	private Context context;
	
	private String info;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advice);
		back=(TextView) findViewById(R.id.back);
		message=(EditText) findViewById(R.id.message);
		send=(Button) findViewById(R.id.send);
		context=this.getApplicationContext();
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
		
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if("".equals(message.getText().toString())){
					Toast.makeText(getApplicationContext(), "请输入内容", Toast.LENGTH_LONG).show();	
				}else{
					mSsoHandler = new SsoHandler(AdviceActivity.this, mWeiboAuth); 
					mSsoHandler.authorize(new AuthListener()); 
					Toast.makeText(getApplicationContext(), "留言发送中，请稍等……", Toast.LENGTH_LONG).show();
				}
			}
		});
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
							String m="@Forsta "+message.getText().toString();
							Log.i("tag", m);
							if(m!=null){
								Log.i("tag", "发送微博");
							ShareWeibo.sendTextWeibo(mAccessToken.getToken(),m,context);
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
