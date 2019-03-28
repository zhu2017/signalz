package com.zsoft.signala.transport.longpolling;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Map;

import android.util.Log;

import com.turbomanage.httpclient.AsyncCallback;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.zsoft.signala.ConnectionBase;
import com.zsoft.signala.ConnectionState;
import com.zsoft.signala.SignalAUtils;
import com.zsoft.signala.transport.StateBase;
import com.zsoft.signala.transport.TransportHelper;
import com.zsoft.signala.SendCallback;
import com.zsoft.parallelhttpclient.ParallelHttpClient;

public class DisconnectingState extends StateBase {

	protected static final String TAG = "DisconnectingState";

	public DisconnectingState(ConnectionBase connection) {
		super(connection);
	}

	@Override
	public ConnectionState getState() {
		return ConnectionState.Disconnecting;
	}

	@Override
	public void Start() {
	}

	@Override
	public void Stop() {
	}

	@Override
	public void Send(CharSequence text, SendCallback callback) {
		callback.OnError(new Exception("Not connected"));
	}
	
	@Override
	protected void OnRun() {
	    String url = SignalAUtils.EnsureEndsWith(mConnection.getUrl(), "/");
		try {
			url += "abort?transport=LongPolling&connectionToken=" + URLEncoder.encode(mConnection.getConnectionToken(), "utf-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Unsupported message encoding error, when encoding connectionToken.");
		}
		TransportHelper.AppendCustomQueryString(mConnection, url);

		AsyncCallback cb = new AsyncCallback() {
			
			@Override
			public void onComplete(HttpResponse httpResponse) {
				
				if(httpResponse.getStatus() != 200 || httpResponse.getBodyAsString() == null || httpResponse.getBodyAsString().isEmpty())
				{
					Log.e(TAG, "Clean disconnect failed. " + httpResponse.getStatus());
				}
				
				mConnection.SetNewState(new DisconnectedState(mConnection));
			}

	        @Override
	        public void onError(Exception ex) {
				mConnection.setError(ex);
				mConnection.SetNewState(new DisconnectedState(mConnection));
	        }
		};

		ParallelHttpClient httpClient = new ParallelHttpClient();
		httpClient.setMaxRetries(1);
        for (Map.Entry<String, String> entry : mConnection.getHeaders().entrySet())
        {
            httpClient.addHeader(entry.getKey(), entry.getValue());
        }
		ParameterMap params = httpClient.newParams();

		try {
			Class clazz = Class.forName("com.example.litepal01.signala.ChatInfo");
//			Class clazz = Class.forName("com.iflysse.studyapp.bean.ChatInfo");
			Method method=clazz.getMethod("getUrls");
			Object o=clazz.newInstance();
			url+=(String) method.invoke(o);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		httpClient.post(url, params, cb);
	}

}
