package com.example.catchlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class CatchLogService extends Service implements Runnable {
	// private String TAG = "LogObserverService";
	private boolean isCatchLog = false;
	public StringBuffer logContent = null;
	private Bundle bundle = null;
	private Intent intent = null;
	private Thread thread=null;
	private ArrayList<String> logContentsArrayList = new ArrayList<String>();
	private BufferedReader bufferedReader=null;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		System.out.println("service onCreate.");
		super.onCreate();
		bundle = new Bundle();
		intent = new Intent();
		logContent = new StringBuffer();
		startCatchlog();
	}

	public void startCatchlog() {           //log��Ϣ������̿�ʼ
		isCatchLog = true;
		thread = new Thread(this);
		thread.start();
		//System.out.println("thread start!!");
	}
	private void sendLogContent(ArrayList<String> logContent) {    //����log��Ϣ
		bundle.putStringArrayList("log", logContent);
		intent.putExtras(bundle);
		intent.setAction(MainActivity.LOG_ACTION);
		sendBroadcast(intent);
		//System.out.println("send log content!!");
	}

	/*private void sendLogContent(String logContent) {
		bundle.putString("log", logContent);
		intent.putExtras(bundle);
		intent.setAction(MainActivity.LOG_ACTION);
		sendBroadcast(intent);
		System.out.println("send broadcast!!!");
	}
*/
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("CatchLogService is running.");
		Process pro = null;
		String line=null;
		try{
			//Runtime.getRuntime().exec("logcat -d");
			pro= Runtime.getRuntime().exec(new String[] {"logcat","*:I"});            //ִ��log����
			bufferedReader= new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while(isCatchLog){
				if((line=bufferedReader.readLine())!=null){
					logContentsArrayList.add(line);
					sendLogContent(logContentsArrayList);
					//Thread.yield();
					Thread.sleep(1000);
				}else{
					System.out.println("readline() is null");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				bufferedReader.close();
				//pro.destroy();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isCatchLog = false;
		thread.interrupt();
		System.out.println("CatchLogService is destroyed.");
	}
}
