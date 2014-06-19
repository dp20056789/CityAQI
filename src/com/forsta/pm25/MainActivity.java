package com.forsta.pm25;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabWidget;

public class MainActivity extends TabActivity {

	//单选点击按钮组
	private RadioGroup main_radio; 
	//单选按钮
	private RadioButton rb1,rb2,rb3,rb4; 
	//TabHost
	private TabHost tabHost=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tabHost = getTabHost();
		// 如果没有继承TabActivity时，通过该种方法加载启动tabHost        
	    TabWidget tw=tabHost.getTabWidget();
	    //去掉白线
		tw.setStripEnabled(false);
		tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("第1个标签")
				.setContent(new Intent(this,HomeActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("第2个标签")
				.setContent(new Intent(this,AreaActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("第3个标签")
				.setContent(new Intent(this,RankActivity.class)));
		tabHost.addTab(tabHost.newTabSpec("tab4").setIndicator("第4个标签")
				.setContent(new Intent(this,SettingActivity.class)));
		main_radio = (RadioGroup) findViewById(R.id.main_radio); 
        rb1=(RadioButton) findViewById(R.id.radioButton1);
        rb2=(RadioButton) findViewById(R.id.radioButton2);
        rb3=(RadioButton) findViewById(R.id.radioButton3);
        rb4=(RadioButton) findViewById(R.id.radioButton4);
        rb1.setTextColor(getResources().getColor(R.color.radio_checked_button));
        //添加切换事件 
        main_radio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId==rb1.getId()){
					rb1.setTextColor(getResources().getColor(R.color.radio_checked_button));
					rb2.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb3.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb4.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					tabHost.setCurrentTab(0);
					
				}else if(checkedId==rb2.getId()){
					rb1.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb2.setTextColor(getResources().getColor(R.color.radio_checked_button));
					rb3.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb4.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					tabHost.setCurrentTab(1);
					
				}else if(checkedId==rb3.getId()){
					rb1.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb2.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb3.setTextColor(getResources().getColor(R.color.radio_checked_button));
					rb4.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					tabHost.setCurrentTab(2);
					
				}else if(checkedId==rb4.getId()){
					rb1.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb2.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb3.setTextColor(getResources().getColor(R.color.radio_unchecked_button));
					rb4.setTextColor(getResources().getColor(R.color.radio_checked_button));
					tabHost.setCurrentTab(3);
					
				}
				
			}
		});
	}

	

}
