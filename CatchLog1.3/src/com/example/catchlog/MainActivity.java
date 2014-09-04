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
	private Button startBtn = null; // ��ʼ
	private Button stopBtn = null; // ֹͣ
	private Button keyWordBtn = null; // �ؼ��ֲ�ѯ
	private Button clearBtn = null; // ���
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
		initial(); // ��ʼ���ؼ�
		onClickListener(); // �����ؼ��¼�

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

	public void onClickListener() { // Button������

		startBtn.setOnClickListener(new OnClickListener() { // ��ʼ��ť

			@Override
			public void onClick(View v) {
				startSearchAllLogService(); // ��������log��Ϣ
			}
		});
		stopBtn.setOnClickListener(new OnClickListener() { // ֹͣ��ť

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopLogService(); // ֹͣ����log
			}
		});
		keyWordBtn.setOnClickListener(new OnClickListener() { // ����ؼ���

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
	 * Ӧ�ú���
	 */
	private void startSearchAllLogService() { // ��������log��Ϣ
		logIntent = new Intent(MainActivity.this, CatchLogService.class);
		startService(logIntent);
		registerLogBroadcastReceiver(); // ע��㲥������
		System.out.println("start LogCatcher service!");
		startBtn.setEnabled(false);
		stopBtn.setEnabled(true);
		keyWordBtn.setEnabled(false);
		clearBtn.setEnabled(false);
		logTag = allLog; // ���ñ�ʶΪȫ������log��Ϣ
		TAG=1;
	}

	private void stopLogService() { // ֹͣlog����
		if (logTag == allLog) {
			this.stopService(logIntent); // ֹͣȫ������
		} else if (logTag == keyWordLog) {
			this.stopService(keyWordIntent); // ֹͣ�ؼ��ּ���
		} else if (logTag == sortLog) {
			this.stopService(sortIntent); // ֹͣ�������
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

	private void startSearchKeyWordLogService() { // �ؼ�������
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				MainActivity.this); // AlertDialog
		LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		final View view = inflater.inflate(R.layout.dialog_search, null);

		alertDialog.setTitle("������ؼ���");
		alertDialog.setView(view);
		alertDialog.setPositiveButton("����",
				new DialogInterface.OnClickListener() { // ���������¼�
					public void onClick(DialogInterface dialog, int whichButton) {

						EditText keyWordText = (EditText) view
								.findViewById(R.id.input_keyword_edittext);
						String keyWord = keyWordText.getText().toString(); // ��ȡ����Ĺؼ���
						Toast.makeText(MainActivity.this,
								"��ʼ�ؼ���" + keyWord + "����", Toast.LENGTH_SHORT)
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
						logTag = keyWordLog; // ���ñ�ʶΪ�ؼ�������log��Ϣ
						TAG=2;
					}
				});

		alertDialog.setNegativeButton("ȡ��",
				new DialogInterface.OnClickListener() { // ȡ����ť
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				});
		alertDialog.create().show();
	}

	private void sendSortType(String tag) { // ����log����service
		bundle = new Bundle();
		bundle.putString("tag", tag);
		sortIntent = new Intent();
		sortIntent.putExtras(bundle);
		sortIntent.setClass(MainActivity.this, SortLogService.class);
		startService(sortIntent);
	}

	private void startSortLogService() { // ��������log

		CharSequence[] sort = { "Verbose", "Debug", "Info", "Warning", "Error",
				"Fatal" };
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				MainActivity.this);
		alertDialog.setTitle("��ѡ��").setSingleChoiceItems(sort, -1, // Radioѡ��
				new DialogInterface.OnClickListener() { // ����¼�

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
						registerLogBroadcastReceiver(); // ע��㲥������
						dialog.dismiss(); // ��ѡ��Ի�����ʧ
						startBtn.setEnabled(false);
						stopBtn.setEnabled(true);
						keyWordBtn.setEnabled(false);
						clearBtn.setEnabled(false);
						logTag = sortLog; // ����logTag��ʶΪ��������
						TAG=3;
					}
				}).setNegativeButton("ȡ��", null).show();
		//isServiceRunning();
	}

	/**
	 * �㲥������
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
				ArrayList<String> logArrayList = bundle // �õ��㲥��������log��Ϣ
						.getStringArrayList("log");
				adapter = new ArrayAdapter<String>(MainActivity.this,
						android.R.layout.simple_list_item_1, logArrayList);
				logListView.setAdapter(adapter); // ��ʾlog��Ϣ

			}
		}
	}

	private void registerLogBroadcastReceiver() { // ע��log��Ϣ������
		logBroadcastReceiver = new LogBroadcastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(LOG_ACTION);
		registerReceiver(logBroadcastReceiver, filter);
		// System.out.println("register logBroadcastReceiver");
	}

	/**
	 * �����˵�
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.layout.menu_items, menu); // ��ȡ�˵���ʽ
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // �˵�ѡ��
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.menu_item_1: // ��ʼ
			startSearchAllLogService();
			break;
		case R.id.menu_item_2: // �ؼ�������log
			startSearchKeyWordLogService();
			break;
		case R.id.menu_item_3: // ��������
			startSortLogService();
			break;
		case R.id.menu_item_4:
			registerLogBroadcastReceiver();
			break;
		case R.id.menu_item_stop: // ֹͣ
			stopLogService();
			break;
		}
		return true;
	}
	/*private boolean isServiceRunning() {                 //��֤service�Ƿ�ֹͣ
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
