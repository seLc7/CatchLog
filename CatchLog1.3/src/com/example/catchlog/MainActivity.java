package com.example.catchlog;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button startBtn = null; // 开始
	private Button stopBtn = null; // 停止
	private Button keyWordBtn = null; // 关键字查询
	private Button clearBtn = null; // 清空
	private Intent logIntent = null;
	private Intent keyWordIntent = null;
	private Intent sortIntent = null;
	private ListView logListView = null;
	private LogBroadcastReceiver logBroadcastReceiver = null;
	// private StringBuffer logString = new StringBuffer();
	private Bundle bundle = null;
	// private int serviceFlag;
	private ArrayAdapter<String> adapter;

	public static String LOG_ACTION = "com.example.catchlog.LOG_ACTION";
	private String logTag = null;
	private final static String allLog = "Catch all the log.";
	private final static String keyWordLog = "Search the log with the keyword";
	private final static String sortLog = "Start sort log and show.";

	private int TAG;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initial(); // 初始化控件
		onClickListener(); // 监听控件事件

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void initial() {
		logListView = (ListView) findViewById(R.id.log_listview);
		startBtn = (Button) findViewById(R.id.start_btn);
		stopBtn = (Button) findViewById(R.id.stop_btn);
		keyWordBtn = (Button) findViewById(R.id.key_word_btn);
		clearBtn = (Button) findViewById(R.id.clear_btn);
	}

	public void onClickListener() { // Button监听器

		startBtn.setOnClickListener(new OnClickListener() { // 开始按钮

			@Override
			public void onClick(View v) {
				startSearchAllLogService(); // 检索所有log信息
			}
		});
		stopBtn.setOnClickListener(new OnClickListener() { // 停止按钮

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopLogService(); // 停止检索log
			}
		});
		keyWordBtn.setOnClickListener(new OnClickListener() { // 输入关键字

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						startSearchKeyWordLogService();
					}
				});
		clearBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				adapter.clear();
				//System.out.println("adapter"+adapter.toString());
				adapter.notifyDataSetChanged();
				clearBtn.setEnabled(false);
			}
		});
	}

	/**
	 * 应用函数
	 */
	private void startSearchAllLogService() { // 检索所有log信息
		logIntent = new Intent(MainActivity.this, CatchLogService.class);
		startService(logIntent);
		registerLogBroadcastReceiver(); // 注册广播接受器
		System.out.println("start LogCatcher service!");
		startBtn.setEnabled(false);
		stopBtn.setEnabled(true);
		keyWordBtn.setEnabled(false);
		clearBtn.setEnabled(false);
		logTag = allLog; // 设置标识为全局搜索log信息
		TAG=1;
	}

	private void stopLogService() { // 停止log检索
		if (logTag == allLog) {
			this.stopService(logIntent); // 停止全部检索
		} else if (logTag == keyWordLog) {
			this.stopService(keyWordIntent); // 停止关键字检索
		} else if (logTag == sortLog) {
			this.stopService(sortIntent); // 停止分类检索
		}
		/*switch(TAG){
		case 1:
			stopService(logIntent);
			break;
		case 2:
			stopService(keyWordIntent);
			break;
		case 3:
			stopService(sortIntent);
			break;
		}*/
		unregisterReceiver(logBroadcastReceiver);
		logTag=null;
		startBtn.setEnabled(true);
		keyWordBtn.setEnabled(true);
		stopBtn.setEnabled(false);
		clearBtn.setEnabled(true);
		//isServiceRunning();
	}

	private void startSearchKeyWordLogService() { // 关键字搜索
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				MainActivity.this); // AlertDialog
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		final View view = inflater.inflate(R.layout.dialog_search, null);

		alertDialog.setTitle("请输入关键字");
		alertDialog.setView(view);
		alertDialog.setPositiveButton("搜索",
				new DialogInterface.OnClickListener() { // 搜索按键事件
					public void onClick(DialogInterface dialog, int whichButton) {

						EditText keyWordText = (EditText) view
								.findViewById(R.id.input_keyword_edittext);
						String keyWord = keyWordText.getText().toString(); // 获取输入的关键字
						Toast.makeText(MainActivity.this,
								"开始关键字" + keyWord + "搜索", Toast.LENGTH_SHORT)
								.show();

						bundle = new Bundle();
						bundle.putString("keyWord", keyWord);
						keyWordIntent = new Intent();
						keyWordIntent.putExtras(bundle);
						keyWordIntent.setClass(MainActivity.this,
								SearchKeyWordService.class);
						startService(keyWordIntent);
						registerLogBroadcastReceiver();
						startBtn.setEnabled(false);
						keyWordBtn.setEnabled(false);
						stopBtn.setEnabled(true);
						clearBtn.setEnabled(false);
						logTag = keyWordLog; // 设置标识为关键字搜索log信息
						TAG=2;
					}
				});

		alertDialog.setNegativeButton("取消",
				new DialogInterface.OnClickListener() { // 取消按钮
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		alertDialog.create().show();
	}

	private void sendSortType(String tag) { // 发送log类别给service
		bundle = new Bundle();
		bundle.putString("tag", tag);
		sortIntent = new Intent();
		sortIntent.putExtras(bundle);
		sortIntent.setClass(MainActivity.this, SortLogService.class);
		startService(sortIntent);
	}

	private void startSortLogService() { // 分类搜索log

		CharSequence[] sort = { "Verbose", "Debug", "Info", "Warning", "Error",
				"Fatal" };
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				MainActivity.this);
		alertDialog.setTitle("请选择").setSingleChoiceItems(sort, -1, // Radio选择
				new DialogInterface.OnClickListener() { // 点击事件

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String tag = null;
						// int logSortTag=0;
						System.out.println(which);
						switch (which) {
						case 0:
							tag = "*:V";
							sendSortType(tag);
							break;
						case 1:
							tag = "*:D";
							sendSortType(tag);
							break;
						case 2:
							tag = "*:I";
							sendSortType(tag);
							break;
						case 3:
							tag = "*:W";
							sendSortType(tag);
							break;
						case 4:
							tag = "*:E";
							sendSortType(tag);
							break;
						case 5:
							tag = "*:F";
							sendSortType(tag);
							break;
						}
						registerLogBroadcastReceiver(); // 注册广播接收器
						dialog.dismiss(); // 点选后对话框消失
						startBtn.setEnabled(false);
						stopBtn.setEnabled(true);
						keyWordBtn.setEnabled(false);
						clearBtn.setEnabled(false);
						logTag = sortLog; // 设置logTag标识为分类搜索
						TAG=3;
					}
				}).setNegativeButton("取消", null).show();
		//isServiceRunning();
	}

	/**
	 * 广播接收者
	 * 
	 * @author Cheng
	 * 
	 */
	private class LogBroadcastReceiver extends BroadcastReceiver {
		private String action = null;
		private Bundle bundle = null;

		@Override
		public void onReceive(Context context, Intent intent) {
			action = intent.getAction();
			if (LOG_ACTION.equals(action)) {
				bundle = intent.getExtras();
				ArrayList<String> logArrayList = bundle // 得到广播传送来的log信息
						.getStringArrayList("log");
				adapter = new ArrayAdapter<String>(MainActivity.this,
						android.R.layout.simple_list_item_1, logArrayList);
				logListView.setAdapter(adapter); // 显示log信息

			}
		}
	}

	private void registerLogBroadcastReceiver() { // 注册log信息接收者
		logBroadcastReceiver = new LogBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(LOG_ACTION);
		registerReceiver(logBroadcastReceiver, filter);
		// System.out.println("register logBroadcastReceiver");
	}

	/**
	 * 弹出菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.layout.menu_items, menu); // 获取菜单样式
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // 菜单选项
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_item_1: // 开始
			startSearchAllLogService();
			break;
		case R.id.menu_item_2: // 关键字搜索log
			startSearchKeyWordLogService();
			break;
		case R.id.menu_item_3: // 分类搜索
			startSortLogService();
			break;
		case R.id.menu_item_4:
			registerLogBroadcastReceiver();
			break;
		case R.id.menu_item_stop: // 停止
			stopLogService();
			break;
		}
		return true;
	}
	/*private boolean isServiceRunning() {                 //验证service是否停止
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	System.out.println(service.service.getClassName().toString());
	        if ("com.example.catchlog.SortLogService".equals(service.service.getClassName())) {
	        	System.out.println("service is running.");
	            return true;
	        }
	    }
	    System.out.println("service is stopped.");
	    return false;
	}*/
}
