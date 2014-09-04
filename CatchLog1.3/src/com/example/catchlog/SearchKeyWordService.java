package com.example.catchlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class SearchKeyWordService extends Service implements Runnable {
	private Bundle bundle=null;
	private Intent intent=null;
	private String keyWord=null;
	private boolean isCatchLog = false;
	private Thread thread=null;
	//private StringBuffer logContent = null;
	private ArrayList<String> logContentsArrayList = new ArrayList<String>();
	private BufferedReader bufferedReader=null;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		bundle = new Bundle();
		intent = new Intent();
		//logContent = new StringBuffer();
		startCatchlog();
	}
	
	public void startCatchlog() {
		isCatchLog = true;
		thread = new Thread(this);
		thread.start();
		//System.out.println("thread start!!");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Bundle getBundle = intent.getExtras();                   //获取关键字
		keyWord=getBundle.getString("keyWord");                         
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isCatchLog = false;
		thread.interrupt();
		System.out.println("SearchKeyWordService is Destroyed.");
	}
	private void sendLogContent(ArrayList<String> logContent) {
		bundle.putStringArrayList("log", logContent);
		intent.putExtras(bundle);
		intent.setAction(MainActivity.LOG_ACTION);
		sendBroadcast(intent);
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("SearchKeyWordService is running.");
		System.out.println("关键字是："+keyWord);
		Process pro = null;
		String line=null;
		try{
			pro= Runtime.getRuntime().exec(new String[] {"logcat","*:I"});
			bufferedReader= new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while(isCatchLog){
				while((line=bufferedReader.readLine())!=null){
					
					if(line.indexOf("I/")>=0 && line.indexOf(keyWord)>=0){                //过滤、关键字搜索
							logContentsArrayList.add(line);
							sendLogContent(logContentsArrayList);
							Thread.sleep(1000);
							//Thread.yield();
					}
				}/*else{
					System.out.println("readline() is null");
				}*/
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

}
