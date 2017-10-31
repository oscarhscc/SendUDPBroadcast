package com.example.sendudpbroadcasttest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

public class MainActivity extends Activity {

    private static String TAG = "WifiBroadcastActivity";
    private boolean start = true;
    private EditText IPAddress;
    private String address;
    public static final int DEFAULT_PORT = 43708;
    private static final int MAX_DATA_PACKET_LENGTH = 40;
    private byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];
    EditText input;
    Button startButton;
    Button stopButton;
    Button sendButton;
    TextView label;
    TextView result;
    String receiveIP;
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                result.append("server:" + msg.obj + "\n");
            } else if (msg.what == 2) {
                sendButton.setEnabled(true);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IPAddress = (EditText) this.findViewById(R.id.address);
        startButton = (Button) this.findViewById(R.id.start);
        stopButton = (Button) this.findViewById(R.id.stop);
        sendButton = (Button) this.findViewById(R.id.send);
        input = (EditText) this.findViewById(R.id.input);
        label = (TextView) this.findViewById(R.id.label);
        result = (TextView) this.findViewById(R.id.content);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        sendButton.setEnabled(false);
        new Thread(new TcpReceive()).start();
        address = getLocalIPAddress();
        if (address != null) {
            IPAddress.setText(address);
        } else {
            IPAddress.setText("Can not get IP address");
            return;
        }
        startButton.setOnClickListener(listener);
        stopButton.setOnClickListener(listener);
        sendButton.setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            label.setText("");
            if (v == startButton) {
                start = true;

                new BroadCastUdp(IPAddress.getText().toString()).start();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            } else if (v == stopButton) {
                start = false;
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                sendButton.setEnabled(false);
            } else if (v == sendButton) {
                //发送消息到服务器
                String inputContent = input.getText().toString();
                result.append("client:" + inputContent + "\n");
                //启动线程 向服务器发送和接收信息
                new MyThread(inputContent).start();
            }
        }
    };

    private String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }

    public class BroadCastUdp extends Thread {
        private String dataString;
        private DatagramSocket udpSocket;

        public BroadCastUdp(String dataString) {
            this.dataString = dataString;
        }

        public void run() {
            DatagramPacket dataPacket = null;

            try {
                udpSocket = new DatagramSocket(DEFAULT_PORT);

                dataPacket = new DatagramPacket(buffer, MAX_DATA_PACKET_LENGTH);
                byte[] data = dataString.getBytes();
                dataPacket.setData(data);
                dataPacket.setLength(data.length);
                dataPacket.setPort(DEFAULT_PORT);

                InetAddress broadcastAddr;

                broadcastAddr = InetAddress.getByName("255.255.255.255");
                dataPacket.setAddress(broadcastAddr);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            // while( start ){
            try {
                udpSocket.send(dataPacket);
                sleep(10);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            // }

            udpSocket.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private class TcpReceive implements Runnable {
        public void run() {
            while (true) {
                Socket socket = null;
                ServerSocket ss = null;
                BufferedReader in = null;
                try {
                    Log.i("TcpReceive", "ServerSocket +++++++");
                    ss = new ServerSocket(8080);

                    socket = ss.accept();

                    Log.i("TcpReceive", "connect +++++++");
                    if (socket != null) {
                        in = new BufferedReader(new InputStreamReader(
                                socket.getInputStream()));

                        StringBuilder sb = new StringBuilder();
                        sb.append(socket.getInetAddress().getHostAddress());
                        String line = null;
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                        }
                        Log.i("TcpReceive", "connect :" + sb.toString());

                        final String ipString = sb.toString().trim();
                        label.post(new Runnable() {

                            @Override
                            public void run() {
                                label.append("收到：" + ipString + "\n");
                                Log.i(TAG, "收到：" + ipString);
                                receiveIP = ipString;
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null)
                            in.close();
                        if (socket != null)
                            socket.close();
                        if (ss != null)
                            ss.close();
                        Message message = new Message();
                        message.what = 2;
                        handler.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class MyThread extends Thread {
        public String content;

        public MyThread(String str) {
            content = str;
        }

        @Override
        public void run() {
            //定义消息
            Message msg = new Message();
            msg.what = 1;
            try {
                //连接服务器 并设置连接超时为5秒
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(receiveIP, 30000), 1000);

                //获取输入输出流
                OutputStream ou = socket.getOutputStream();
                //获取输出输出流
                BufferedReader bff = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                //向服务器发送信息
                ou.write(content.getBytes("utf-8"));
                ou.flush();

                //读取发来服务器信息
                String result = "";
                String buffer = "";
                while ((buffer = bff.readLine()) != null) {
                    result = result + buffer;
                }
                msg.obj = result.toString();
                //发送消息 修改UI线程中的组件
                handler.sendMessage(msg);
                //关闭各种输入输出流
                bff.close();
                ou.close();
                socket.close();
            } catch (SocketTimeoutException aa) {
                //连接超时 在UI界面显示消息
                msg.obj = "服务器连接失败！请检查网络是否打开";
                //发送消息 修改UI线程中的组件
                handler.sendMessage(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
