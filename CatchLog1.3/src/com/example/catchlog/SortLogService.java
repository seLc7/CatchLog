package com.example.catchlog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

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
		Bundle getBundle = intent.getExtras();                   //��ȡtag
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
	 * ���������������ٽ��йؼ��������ͻ���������û�н����
	 * (��֤����serviceû��ֹͣ���������������ֹͣ�����ˡ�������)��֤���ܲ��ǡ�����(�����ǲ��ǣ�������������
	 * 
	 * @author Cheng
	 * �������ֹservice�󲻻���ֹthread����Ҫ�ֶ���ֹ��thread.interrupt()
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Process pro = null;
		String line=null;
		System.out.println("���ࣺ"+tag);
		System.out.println("SortLogService is running.");
		try{
			pro= Runtime.getRuntime().exec(new String[] {"logcat",tag});    //����ֻ����һ�����ȼ��������Ǹ������ȼ�Ҳ��ʾ
			bufferedReader= new BufferedReader(new InputStreamReader(pro.getInputStream()));
			while(isCatchLog){
				while((line=bufferedReader.readLine())!=null){
					logContentsArrayList.add(line);
					sendLogContent(logContentsArrayList);
					//Thread.yield();
					Thread.sleep(1000);
					/*if(line.indexOf(tag)>=0 ){                //�������
							
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
