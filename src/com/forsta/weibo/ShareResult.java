package com.forsta.weibo;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class ShareResult extends AsyncTask<Void, Void, Void>{
	private Context context;
	private String msg;
	public ShareResult(Context context,String msg) {
		this.context=context;
		this.msg=msg;
	}
	@Override
	protected Void doInBackground(Void... params) {
		publishProgress();
		return null;
	}
	@Override
	protected void onProgressUpdate(Void... values) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		super.onProgressUpdate(values);
	}

}
