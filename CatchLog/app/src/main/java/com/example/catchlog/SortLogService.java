package com.example.catchlog;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SortLogService extends Service implements Runnable{
	private Bundle bundle=null;
	private Intent intent=null;
	private Thread thread=null;
	private String tag=null;
	private boolean isCatchLog = false;
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
		startCatchlog();
	}
	public void startCatchlog() {
		isCatchLog = true;
		thread = new Thread(this);
		thread.start();
		
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Bundle getBundle = intent.getExtras();                   //获取tag
		tag=getBundle.getString("tag");                         
		return super.onStartCommand(intent, flags, startId);
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		isCatchLog = false;
		thread.interrupt();
		System.out.println("SortLogService is destroyed.");
	}

	private void sendLogContent(ArrayList<String> logContent) {
		bundle.putStringArrayList("log", logContent);
		intent.putExtras(bundle);
		intent.setAction(MainActivity.LOG_ACTION);
		sendBroadcast(intent);
	}
	/**
	 * 启动分类搜索后，再进行关键字搜索就会出现冗余项，没有解决。
	 * (验证过是service没有停止。明明程序里进行停止操作了。。。。)验证可能不是。。。(到底是不是！！！）。。。
	 * 
	 * @author Cheng
	 * 解决：终止service后不会终止thread，需要手动终止，thread.interrupt()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Process pro = null;
		String line=null;
		System.out.println("分类："+tag);
		System.out.println("SortLogService is running.");
		try{
			pro= Runtime.getRuntime().exec(new String[] {"logcat",tag});    //怎样只过滤一个优先级，而不是更高优先级也显示
			bufferedReader= new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while(isCatchLog){
				while((line=bufferedReader.readLine())!=null){
					logContentsArrayList.add(line);
					sendLogContent(logContentsArrayList);
					//Thread.yield();
					Thread.sleep(1000);
					/*if(line.indexOf(tag)>=0 ){                //分类过滤
							
					}*/
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
