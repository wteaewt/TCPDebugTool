package com.svs.wt.tcpdebugtool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.svs.wt.tcpdebugtool.helper.ByteHelper;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private CommunicationHelper ch;

    private EditText ipEdx, msgEdx, revEdx;
    private Button ConnectBtn, DisconnectBtn, SendBtn;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ConnectBtn = (Button) findViewById(R.id.connectBtn);
        ConnectBtn.setOnClickListener(this);
        DisconnectBtn = (Button) findViewById(R.id.disconnectBtn);
        DisconnectBtn.setOnClickListener(this);
        SendBtn = (Button) findViewById(R.id.sendMsgBtn);
        SendBtn.setOnClickListener(this);

        ipEdx = (EditText) findViewById(R.id.ip_address_edx);
        msgEdx = (EditText) findViewById(R.id.send_msg_edx);
        revEdx = (EditText) findViewById(R.id.recEdx);
        ch = new CommunicationHelper(msgHandler);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connectBtn) {
            if (!ch.Connect(ipEdx.getText().toString(), 15862))
                Toast.makeText(MainActivity.this, "服务器处于已连接状态，不能重复连接", Toast.LENGTH_SHORT).show();

        } else if (v.getId() == R.id.disconnectBtn) {
            if (!ch.Disconnect())
                Toast.makeText(MainActivity.this, "服务器没有处于已连接状态，不能断开", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.sendMsgBtn) {
            byte[] msg;
            try{
                msg=ByteHelper.HexStringToBytes(msgEdx.getText().toString());
                if (!ch.SendMsg(msg))
                    Toast.makeText(MainActivity.this, "服务器没有处于已连接状态，不能发送", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Toast.makeText(MainActivity.this, "发送的字符串不是16进制字符，不能发送", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private Handler msgHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CommunicationHelper.CONNECTED) {
                Toast.makeText(MainActivity.this, "服务器已连接", Toast.LENGTH_SHORT).show();
                ConnectBtn.setEnabled(false);
                DisconnectBtn.setEnabled(true);
                SendBtn.setEnabled(true);
            } else if (msg.what == CommunicationHelper.DISCONNECTED) {
                Toast.makeText(MainActivity.this, "服务器已断开", Toast.LENGTH_SHORT).show();
                ConnectBtn.setEnabled(true);
                DisconnectBtn.setEnabled(false);
                SendBtn.setEnabled(false);
            } else if (msg.what == CommunicationHelper.SENDMSG) {
                Toast.makeText(MainActivity.this, "信息已发送", Toast.LENGTH_SHORT).show();
            } else if (msg.what == CommunicationHelper.ERROR) {
                Toast.makeText(MainActivity.this, msg.getData().getString("reason"), Toast.LENGTH_SHORT).show();
            } else if (msg.what == CommunicationHelper.REVMSG) {
                byte[] cmsg = (byte[]) msg.obj;
                String str = ByteHelper.BytesToHexString(cmsg, cmsg.length, true);
                revEdx.append(str);
            }
        }
    };

    @Override
    protected void onDestroy() {
        ch.Disconnect();
        super.onDestroy();
    }


}
