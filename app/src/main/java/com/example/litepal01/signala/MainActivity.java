package com.example.litepal01.signala;

import android.content.OperationApplicationException;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.zsoft.signala.hubs.HubConnection;
import com.zsoft.signala.hubs.HubOnDataCallback;
import com.zsoft.signala.hubs.IHubProxy;
import com.zsoft.signala.transport.StateBase;
import com.zsoft.signala.transport.longpolling.LongPollingTransport;



import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    String url="id=50E7CEE6-A5C0-42E5-B4D5-FC5DC98BC875&liveid=3eb6e98c-feac-4034-b12c-573a21a8e4c8&name=xxt&key=c1a2t3c4h5e6r";

    private static final String TAG = "MainActivity";
//    private com.zsoft.signala.hubs.IHubProxy hub = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn=findViewById(R.id.connection);
        btn.setOnClickListener(v->{
//            beginConnect();
            Connect(Uri.parse("http://192.168.1.133:8080/"));

        });
        Button btnAdd=findViewById(R.id.add);
        btnAdd.setOnClickListener(v->{
            List<String> args = new ArrayList<String>();
//                    args.add(Utils.getUSER_KEY(MyServices.this));
            //方法参数有几个就添加几个，按照方法参数从左到右的顺序
            args.add("3eb6e98c-feac-4034-b12c-573a21a8e4c8");
            args.add("app");
            args.add("客户端请求");
            //网服务器传参数
                        hub.Invoke("SendLiveMsg", args, new com.zsoft.signala.hubs.HubInvokeCallback() {
                            @Override
                            public void OnResult(boolean succeeded, String response) {
//                            Utils.log("是否成功:" + succeeded + " 返回结果:" + response);
                                Log.d(TAG, "OnResult: "+ succeeded + " 返回结果:" + response);
                            }

                            @Override
                            public void OnError(Exception ex) {
                                Log.d(TAG, "OnError: "+ex);
                            }
                        });
        });
    }
//    public void startSignalA() {
//        if (conn != null)
//            conn.Start();
//    }
//
//    public void stopSignalA() {
//        if (conn != null)
//            conn.Stop();
//    }


    protected HubConnection con = null;
    protected IHubProxy hub = null;

    List<String>msg=new ArrayList<>();

    public void Connect(Uri address) {

        con = new HubConnection(address.toString(), this, new LongPollingTransport())
        {
            @Override
            public void OnStateChanged(StateBase oldState, StateBase newState) {
                //tvStatus.setText(oldState.getState() + " -> " + newState.getState());

                switch(newState.getState())
                {
                    case Disconnected:
                        Log.v("syk", "未连接！");
                        break;
                    case Connecting:
                        Log.v("syk", "正在连接!");
                        break;
                    case Connected:
                        Log.v("syk", "连接成功!");

                        break;
                    case Reconnecting:
                        Log.v("syk", "重新连接！");
                        break;
                    case Disconnecting:
                        Log.v("syk", "断开连接！");
                        break;
                }
            }

            @Override
            public void OnError(Exception exception) {
                Toast.makeText(MainActivity.this, "On error: 连接失败" + exception.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(TAG, "OnError: "+ "On error: " + exception.getMessage());
                if (con!=null)
                con.Stop();
            }

        };

        try {
            hub = con.CreateHubProxy("Chart");
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        hub.On("broadcastMessage", new HubOnDataCallback()
        {
            @Override
            public void OnReceived(org.json.JSONArray args) {
                for (int i=0;i<args.length();i++){
                    try {
                        JSONObject jsonObject=JSONObject.parseObject(args.getString(i));//;
                        msg.add(jsonObject.getString("content"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "OnReceived: 发送信息后接受："+msg.toString());
                for (int i=0;i<args.length();i++){
                    try {
                        ChatBean chatBean=JSONObject.parseObject(args.getString(i),ChatBean.class);
                        Log.d(TAG, "OnReceived: "+chatBean.getContent());
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

//            @Override
//            public void OnReceived(JSONArray args) {
//
//                String jsons=args.toString();

//
//                for(int i=0; i<args.length(); i++)
//                {
//                    Toast.makeText(MainActivity.this, "New message\n" + args.opt(i).toString(), Toast.LENGTH_SHORT).show();
//                }
//                if (args.opt(0).toString().equals("1")) {
//
//                    Log.d(TAG, "OnReceived: "+args.opt(1).toString());
//
//       } else {
//                    Log.d(TAG, "OnReceived: "+args.opt(1).toString());
//                }

//            }
        });


        con.Start();
    }




}
