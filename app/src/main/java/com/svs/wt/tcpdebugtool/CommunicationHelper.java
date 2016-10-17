package com.svs.wt.tcpdebugtool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.svs.wt.tcpdebugtool.helper.ByteHelper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Created by wt on 2016/10/12.
 */

public class CommunicationHelper {
    private Socket socket;
    private boolean isRunning =false;
    private final int bufferSize =8192;
    private byte[] buffer=new byte[bufferSize];
    private Handler uiHandler=null;
    private ReceiveMsgThread revThread=null;

    public static final int CONNECTED=101;
    public static final int DISCONNECTED=102;
    public static final int SENDMSG=103;
    public static final int REVMSG=104;
    public static final int ERROR=110;

    private BlockingQueue<OpItem> items =new ArrayBlockingQueue<>(1024);
    private CHState state=CHState.Init;
    private enum CHState{
        Init,
        Connecting,
        Connected,
        Disconnecting,
        Disconnected
    }

    public CommunicationHelper(Handler uihandler) {
        uiHandler=uihandler;
    }

    public boolean IsConnect(){
        return state==CHState.Connected;
    }

    public boolean Connect(final String host, final int port){
        if(state==CHState.Init||state==CHState.Disconnected){
            try {
                OpItem item=new OpItem(OpType.Connect);
                item.Host=host;
                item.Port=port;
                items.put(item);
                new CommunicateThread().start();
                return true;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean SendMsg(final byte[]  msg){
        if(state==CHState.Connected)
        try {
            items.put(new OpItem(OpType.Send,msg));
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            //      _sendError(e,"发送信息失败");
        }
        return false;
    }

    public boolean Disconnect()  {
        if(state==CHState.Connected)
        {
            try {
                items.put(new OpItem(OpType.Disconnect));
                return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void _sendError(Exception e,String reason) {
        Message msg=new Message();
        msg.what=ERROR;
        msg.obj=e;
        Bundle bundle=new Bundle();
        bundle.putString("reason",reason);
        msg.setData(bundle);
        uiHandler.sendMessage(msg);
    }

    private class CommunicateThread extends Thread{
        @Override
        public void run() {
            boolean isRun=true;
            while(isRun){

                OpItem item= null;
                try {
                    item = items.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(item.Optype==OpType.Send) {
                        if(state==CHState.Connected&&socket!=null&&socket.isConnected()&&!socket.isClosed()&&!socket.isOutputShutdown()) {
                            try {
                                OutputStream writer = socket.getOutputStream();
                                byte[] bMsg = item.Msg;
                                writer.write(bMsg, 0, bMsg.length);
                                writer.flush();
                                Message msg = new Message();
                                msg.what = SENDMSG;
                                uiHandler.sendMessage(msg);
                                Log.d("Send", "Send Success");
                            } catch (IOException e) {
                                e.printStackTrace();
                                _sendError(e,"SendMsgError");
                                try {
                                    items.put(new OpItem(OpType.Disconnect));
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        else{
                            Log.d("Send", "Socket Can't Send Msg");
                            try {
                                items.put(new OpItem(OpType.Disconnect));
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }else if(item.Optype==OpType.Disconnect){
                        state=CHState.Disconnecting;
                        isRun=false;
                        if (socket!=null&&!socket.isClosed()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        socket=null;
                        isRunning=false;
                        if(revThread!=null) {
                            revThread.interrupt();
                            revThread=null;
                        }
                        items.clear();
                        state=CHState.Disconnected;
                        Message msg=new Message();
                        msg.what=DISCONNECTED;
                        Log.d("Disconnect", "Disconnected");
                        uiHandler.sendMessage(msg);
                        state=CHState.Disconnected;
                    }else if(item.Optype==OpType.Connect){
                        state=CHState.Connecting;
                        try {
                            socket = new Socket(item.Host, item.Port);
                            state=CHState.Connected;
                            isRunning =true;
                            revThread=new ReceiveMsgThread();
                            revThread.start();
                            state=CHState.Connected;
                            Message msg=new Message();
                            msg.what=CONNECTED;
                            uiHandler.sendMessage(msg);
                            Log.d("CommunicationHelper", "Connect success");
                        } catch (IOException e) {
                            e.printStackTrace();
                            _sendError(e,"连接失败");
                            socket=null;
                            state=CHState.Init;
                        }

                    }


            }
        }
    }

    private class ReceiveMsgThread extends Thread {
        @Override
        public void run() {
            while(isRunning){
                if(state==CHState.Connected&&socket!=null&&socket.isConnected()&&!socket.isClosed()&&!socket.isInputShutdown())
                {
                    try {

                        int size= socket.getInputStream().read(buffer,0,bufferSize);
                        if(size>0){
                            ProcessRecMsg(buffer,size);
                        }
                        else if(size<=0){
                            Log.d("ReceiveMsgThread", "ServerDisconnect");
                            isRunning=false;
                            try {
                                items.put(new OpItem(OpType.Disconnect));
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }

                        }

                    } catch (IOException e) {
                        isRunning=false;
                        if(!e.getMessage().toString().equals("Socket closed"))
                        {
                            e.printStackTrace();
                            //    _sendError(e,"接收信息失败！");
                            isRunning=false;
                            if(state!=CHState.Disconnecting&&state!=CHState.Disconnected)
                            {
                                try {
                                    items.put(new OpItem(OpType.Disconnect));
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                }
                else{
                    isRunning=false;
                    try {
                        items.put(new OpItem(OpType.Disconnect));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    protected void ProcessRecMsg(byte[] buffer,int size){
        String recMsg= ByteHelper.BytesToHexString(buffer,size,true);
        Log.d("receive", recMsg);
        Message msg=new Message();
        msg.what=REVMSG;
        byte[] dis=new byte[size];
        System.arraycopy(buffer,0,dis,0,size);
        msg.obj=dis;
        uiHandler.sendMessage(msg);
    }

    private class OpItem{
        public OpItem(OpType type){
            Optype=type;
        }

        public OpItem(OpType type,byte[] msg){
            Optype=type;
            Msg=msg;
        }
        OpType Optype;
        byte[] Msg;
        String Host;
        int Port;
    }
    private enum OpType{
        Connect,
        Disconnect,
        Send
    }
}
